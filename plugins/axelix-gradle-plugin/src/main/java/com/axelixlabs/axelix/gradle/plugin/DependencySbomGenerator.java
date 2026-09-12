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
package com.axelixlabs.axelix.gradle.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

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

    static final String SBOM_RESOURCE_PATH = "META-INF/axelix/dependencies.cdx.json";

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

        project.getTasks().configureEach(task -> {
            String taskName = task.getName();
            if ("jar".equals(taskName) || "bootJar".equals(taskName)) {
                task.dependsOn(generateTask);
                if (task instanceof AbstractArchiveTask) {
                    ((AbstractArchiveTask) task)
                            .from(generatedDir, spec -> spec.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE));
                }
            } else if ("processResources".equals(taskName)) {
                // Mirror ProjectInfoGenerator: also copy into build/resources/main so `bootRun`,
                // `test` and IDE runs - which read that directory directly - see the SBOM too.
                task.dependsOn(generateTask);
                if (task instanceof Copy) {
                    ((Copy) task).from(generatedDir);
                }
            }
        });
    }

    private static void writeSbom(Project project, File generatedDir) {
        Configuration runtimeClasspath = project.getConfigurations().getByName(RUNTIME_CLASSPATH);
        ResolvedComponentResult root =
                runtimeClasspath.getIncoming().getResolutionResult().getRoot();

        Map<ComponentIdentifier, ResolvedComponentResult> libraries = new LinkedHashMap<>();
        collectLibraries(root, libraries, new HashSet<>());

        Bom bom = new Bom();

        Metadata metadata = new Metadata();
        metadata.setComponent(componentOf(root, Component.Type.APPLICATION));
        bom.setMetadata(metadata);

        List<Component> components = new ArrayList<>();
        for (ResolvedComponentResult library : libraries.values()) {
            components.add(componentOf(library, Component.Type.LIBRARY));
        }
        bom.setComponents(components);

        Map<String, Set<String>> edges = new LinkedHashMap<>();
        addEdges(root, edges);
        for (ResolvedComponentResult library : libraries.values()) {
            addEdges(library, edges);
        }

        List<Dependency> dependencies = new ArrayList<>();
        for (Map.Entry<String, Set<String>> edge : edges.entrySet()) {
            Dependency dependency = new Dependency(edge.getKey());
            for (String child : edge.getValue()) {
                dependency.addDependency(new Dependency(child));
            }
            dependencies.add(dependency);
        }
        bom.setDependencies(dependencies);

        writeToFile(generatedDir, serialize(bom));
    }

    /**
     * Depth-first walk collecting every external module reachable from the root. Only components
     * whose identity is a {@link ModuleComponentIdentifier} are kept: those are the third-party
     * artifacts resolved from a repository, which is exactly what Master matches against its
     * curated registry. The application's own project components (root and sub-modules) are not
     * libraries and are deliberately excluded here. Keyed by identity so a diamond is kept once,
     * and the {@code visited} set breaks any cycles Gradle may report.
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
        if (node.getId() instanceof ModuleComponentIdentifier) {
            // A library boundary: this is an edge target. Its own outgoing edges are added
            // separately, when it is itself the 'from' node, so we stop descending here.
            targets.add(referenceOf(node));
            return;
        }
        // A project component on the path: see through it to the libraries beneath.
        for (ResolvedComponentResult child : resolvedChildren(node)) {
            collectNearestLibraries(child, targets, visited);
        }
    }

    private static List<ResolvedComponentResult> resolvedChildren(ResolvedComponentResult node) {
        List<ResolvedComponentResult> children = new ArrayList<>();
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

    private static Component componentOf(ResolvedComponentResult component, Component.Type type) {
        Component result = new Component();
        result.setType(type);

        ModuleVersionIdentifier moduleVersion = component.getModuleVersion();
        if (moduleVersion != null) {
            result.setGroup(moduleVersion.getGroup());
            result.setName(moduleVersion.getName());
            result.setVersion(moduleVersion.getVersion());
        } else {
            result.setName(component.getId().getDisplayName());
        }

        String reference = referenceOf(component);
        result.setBomRef(reference);
        result.setPurl(reference);
        return result;
    }

    /**
     * The stable reference a component is keyed by in the SBOM - its package URL. Master parses
     * these back into coordinates, so the {@code group:name:version} form must be exact. Built from
     * the module identifier for libraries; the root application falls back to its project
     * coordinates, which Gradle always populates (an unset version resolves to {@code unspecified}).
     */
    private static String referenceOf(ResolvedComponentResult component) {
        ComponentIdentifier id = component.getId();
        if (id instanceof ModuleComponentIdentifier) {
            ModuleComponentIdentifier module = (ModuleComponentIdentifier) id;
            return purl(module.getGroup(), module.getModule(), module.getVersion());
        }
        ModuleVersionIdentifier moduleVersion = component.getModuleVersion();
        if (moduleVersion == null) {
            return component.getId().getDisplayName();
        }
        return purl(moduleVersion.getGroup(), moduleVersion.getName(), moduleVersion.getVersion());
    }

    private static String purl(String group, String name, String version) {
        return "pkg:maven/" + group + "/" + name + "@" + version + "?type=jar";
    }

    private static String serialize(Bom bom) {
        try {
            return BomGeneratorFactory.createJson(Version.VERSION_16, bom).toJsonString(true);
        } catch (GeneratorException e) {
            throw new GradleException("Failed to serialize the Axelix dependency SBOM", e);
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
