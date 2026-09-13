/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.gradle.plugin.sbom;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.axelixlabs.axelix.gradle.plugin.properties.ProjectInfoGenerator;
import org.cyclonedx.Version;
import org.cyclonedx.exception.GeneratorException;
import org.cyclonedx.generators.BomGeneratorFactory;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.Dependency;
import org.cyclonedx.model.Metadata;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.gradle.plugin.AxelixGradlePlugin;
import com.axelixlabs.axelix.gradle.plugin.BuildDirAccessor;
import com.axelixlabs.axelix.gradle.plugin.GeneratedResourcesPackager;

/**
 * Generates a CycloneDX SBOM of the project's runtime dependency graph and packages it into the
 * archive at {@code META-INF/axelix/dependencies.cdx.json}, alongside the build-info written by
 * {@link ProjectInfoGenerator}.
 *
 * <p>The SBOM is built from the fully resolved {@code runtimeClasspath} - the exact set of
 * libraries and versions that ship and run - so Axelix Master can tell which known libraries an
 * application actually depends on and along which path each was pulled in. The graph edges
 * ({@code dependsOn}) are what let Master reconstruct that resolution path.
 *
 * <p>Only APIs present in both Gradle 5.0 and 9.x are used here, matching {@link AxelixGradlePlugin}.
 *
 * @author Mikhail Polivakha
 */
public final class DependencySbomGenerator {

    public static final String GENERATE_TASK_NAME = "generateAxelixDependenciesSbom";

    public static final String SBOM_RESOURCE_PATH = "META-INF/axelix/dependencies.cdx.json";

    private static final String RUNTIME_CLASSPATH = "runtimeClasspath";

    // DependencyResult.isConstraint() only appeared after Gradle 5.0. Probing the running Gradle's
    // API once lets the generator skip constraint edges where it can and stay loadable on 5.0, which
    // predates both the method and the separate constraint edges it would have filtered.
    private static final boolean CONSTRAINT_API_AVAILABLE = isConstraintApiAvailable();

    private DependencySbomGenerator() {}

    private static boolean isConstraintApiAvailable() {
        try {
            DependencyResult.class.getMethod("isConstraint");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static void configure(final Project project) {
        Configuration runtimeClasspath = project.getConfigurations().findByName(RUNTIME_CLASSPATH);
        if (runtimeClasspath == null) {
            // No runtime classpath to describe (e.g. a project that applies 'java' transitively but
            // declares no runtime configuration). Nothing to generate.
            return;
        }

        File generatedDir = new File(BuildDirAccessor.buildDir(project), "generated/axelix-sbom");

        Task generateTask = project.getTasks().create(GENERATE_TASK_NAME);
        generateTask.setGroup("build");
        generateTask.setDescription(
                "Generates META-INF/axelix/dependencies.cdx.json - a CycloneDX SBOM of the runtime dependency graph.");

        // Tracking the resolved classpath as an input invalidates the SBOM whenever a dependency is
        // added, removed or its resolved version changes. Resolution is deferred to input
        // snapshotting (execution time), so this does not resolve the graph at configuration time.
        generateTask.getInputs().files(runtimeClasspath).withPropertyName(RUNTIME_CLASSPATH);
        generateTask.getOutputs().dir(generatedDir);
        generateTask.doLast(task -> writeSbom(project, generatedDir));

        GeneratedResourcesPackager.packageIntoArchives(project, generateTask, generatedDir);
    }

    private static void writeSbom(Project project, File generatedDir) {
        Configuration runtimeClasspath = project.getConfigurations().getByName(RUNTIME_CLASSPATH);
        ResolvedComponentResult root =
                runtimeClasspath.getIncoming().getResolutionResult().getRoot();

        Map<ComponentIdentifier, ResolvedComponentResult> libraries = new LinkedHashMap<>();
        collectLibraries(root, libraries, new HashSet<>());

        Map<String, Set<String>> edges = new LinkedHashMap<>();
        addEdges(root, edges);
        for (ResolvedComponentResult library : libraries.values()) {
            addEdges(library, edges);
        }

        writeToFile(generatedDir, serialize(bomOf(root, libraries.values(), edges)));
    }

    /**
     * Depth-first walk collecting every external module reachable from the root. Only components
     * whose identity is a {@link ModuleComponentIdentifier} are kept -those are the third-party
     * artifacts resolved from a repository. If the components do not have the {@link ModuleComponentIdentifier},
     * which is quite rare, we assume that this is the component that is bundled into classpath like a just a
     * separate file or by some other means.
     * <p>
     * Again, technically, in real SBOM, ideally, we would want to show it, but in our case it is not going make much sense,
     * since Axelix Master will just not recognize such component as the "well-known".
     * <p>
     * Keyed by identity so a diamond is kept once, and the {@code visited} set breaks any cycles Gradle may report.
     */
    private static void collectLibraries(
            ResolvedComponentResult node,
            Map<ComponentIdentifier, ResolvedComponentResult> libraries,
            Set<ComponentIdentifier> visited) {

        if (!visited.add(node.getId())) {
            return;
        }
        if (node.getId() instanceof ModuleComponentIdentifier) {
            libraries.put(node.getId(), node);
        }
        for (ResolvedComponentResult child : resolvedChildren(node)) {
            collectLibraries(child, libraries, visited);
        }
    }

    /**
     * Records the outgoing edges of an emitted node (the root application or a library) into the
     * dependency graph. A project component sitting between two libraries - e.g. {@code app ->
     * :sub -> guava} - is transparent: its nearest library descendants are attributed to the
     * emitted node instead, so the path stays {@code app -> guava} without leaking the internal
     * module structure that Master has no use for.
     */
    private static void addEdges(ResolvedComponentResult from, Map<String, Set<String>> edges) {
        Set<String> targets = new LinkedHashSet<>();
        Set<ComponentIdentifier> visited = new HashSet<>();
        for (ResolvedComponentResult child : resolvedChildren(from)) {
            collectNearestLibraries(child, targets, visited);
        }
        if (!targets.isEmpty()) {
            edges.put(referenceOf(from), targets);
        }
    }

    private static void collectNearestLibraries(
            ResolvedComponentResult node, Set<String> targets, Set<ComponentIdentifier> visited) {

        if (!visited.add(node.getId())) {
            return;
        }
        // direct outgoing edge to other component
        if (node.getId() instanceof ModuleComponentIdentifier) {
            targets.add(referenceOf(node));
            return;
        }
        // A "project" component on the path, e.g. something like that
        //
        // dependencies {
        //     implementation(project(":sub-compnent"))
        // }
        //
        // So we need it to go through it to the libraries beneath.
        for (ResolvedComponentResult child : resolvedChildren(node)) {
            collectNearestLibraries(child, targets, visited);
        }
    }

    private static List<ResolvedComponentResult> resolvedChildren(ResolvedComponentResult node) {
        List<ResolvedComponentResult> children = new ArrayList<>();
        // The application's own project components (root and sub-modules) are not
        // libraries and are deliberately excluded here.
        for (DependencyResult dependency : node.getDependencies()) {
            // A constraint (e.g. from a platform/BOM) pins a version without pulling the artifact in
            // on its own, so it is not a real edge in the shipped graph. isConstraint() is declared
            // on DependencyResult, but only on Gradle versions after 5.0, hence the guard.
            if (CONSTRAINT_API_AVAILABLE && dependency.isConstraint()) {
                continue;
            }
            if (!(dependency instanceof ResolvedDependencyResult)) {
                // An unresolved dependency never made it onto the runtime classpath (e.g. Gradle
                // selected a different version), so it is not part of what actually ships.
                continue;
            }
            children.add(((ResolvedDependencyResult) dependency).getSelected());
        }
        return children;
    }

    private static String referenceOf(ResolvedComponentResult component) {
        return Coordinates.of(component).reference();
    }

    /**
     * Assembles a CycloneDX 1.6 document from the collected graph: a metadata component for the
     * application, a flat list of library components, and the {@code dependsOn} edges.
     */
    private static Bom bomOf(
            ResolvedComponentResult root,
            Collection<ResolvedComponentResult> libraries,
            Map<String, Set<String>> edges) {

        Bom bom = new Bom();

        Metadata metadata = new Metadata();
        metadata.setComponent(componentOf(root, Component.Type.APPLICATION));
        bom.setMetadata(metadata);

        List<Component> components = new ArrayList<>();
        for (ResolvedComponentResult library : libraries) {
            components.add(componentOf(library, Component.Type.LIBRARY));
        }
        bom.setComponents(components);

        List<Dependency> dependencies = new ArrayList<>();
        for (Map.Entry<String, Set<String>> edge : edges.entrySet()) {
            Dependency dependency = new Dependency(edge.getKey());
            for (String target : edge.getValue()) {
                dependency.addDependency(new Dependency(target));
            }
            dependencies.add(dependency);
        }
        bom.setDependencies(dependencies);

        return bom;
    }

    private static Component componentOf(ResolvedComponentResult component, Component.Type type) {
        Coordinates coordinates = Coordinates.of(component);
        String reference = coordinates.reference();

        Component result = new Component();
        result.setType(type);
        result.setBomRef(reference);
        if (coordinates.group != null) {
            result.setGroup(coordinates.group);
        }
        result.setName(coordinates.name);
        if (coordinates.version != null) {
            result.setVersion(coordinates.version);
        }
        result.setPurl(reference);
        return result;
    }

    private static String serialize(Bom bom) {
        try {
            return BomGeneratorFactory.createJson(Version.VERSION_16, bom).toJsonString(true);
        } catch (GeneratorException e) {
            throw new GradleException("Failed to serialize the Axelix dependency SBOM", e);
        }
    }

    /**
     * The Maven coordinates of a resolved component. Libraries take them from the module identifier
     * (always complete); the root application falls back to its project coordinates, which Gradle
     * populates even when unset (an unset version resolves to {@code unspecified}), or to the
     * identifier's display name in the rare case none are available.
     */
    private static final class Coordinates {

        private final @Nullable String group;
        private final String name;
        private final @Nullable String version;

        private Coordinates(@Nullable String group, String name, @Nullable String version) {
            this.group = group;
            this.name = name;
            this.version = version;
        }

        static Coordinates of(ResolvedComponentResult component) {
            ComponentIdentifier id = component.getId();
            if (id instanceof ModuleComponentIdentifier) {
                ModuleComponentIdentifier module = (ModuleComponentIdentifier) id;
                return new Coordinates(module.getGroup(), module.getModule(), module.getVersion());
            }
            ModuleVersionIdentifier moduleVersion = component.getModuleVersion();
            if (moduleVersion == null) {
                return new Coordinates(null, id.getDisplayName(), null);
            }
            return new Coordinates(moduleVersion.getGroup(), moduleVersion.getName(), moduleVersion.getVersion());
        }

        /**
         * The package URL Master parses back into coordinates, so the {@code group/name@version}
         * form must be exact.
         */
        String reference() {
            if (group == null || version == null) {
                return name;
            }
            return "pkg:maven/" + group + "/" + name + "@" + version + "?type=jar";
        }
    }

    private static void writeToFile(File generatedDir, String json) {
        File target = new File(generatedDir, SBOM_RESOURCE_PATH);
        File parent = target.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new GradleException("Cannot create directory " + parent);
        }
        try {
            Files.write(target.toPath(), json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new GradleException("Failed to write the Axelix dependency SBOM to " + target, e);
        }
    }
}
