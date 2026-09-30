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
package com.axelixlabs.axelix.maven.plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultDependencyResolutionRequest;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.cyclonedx.Version;
import org.cyclonedx.exception.GeneratorException;
import org.cyclonedx.generators.BomGeneratorFactory;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.Dependency;
import org.cyclonedx.model.Metadata;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;

/**
 * Mojo that generates a CycloneDX SBOM of the project's runtime dependency graph.
 *
 * @author Mikhail Polivakha
 */
@Mojo(name = "axelix-generate-dependencies-sbom", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class GenerateDependenciesSbomMojo extends AbstractMojo {

    public static final String SBOM_RESOURCE_PATH = "META-INF/axelix/dependencies.cdx.json";

    /**
     * The dependency scopes present on the runtime classpath of the packaged application.
     * {@code test}, {@code provided} and {@code system} artifacts never ship, so they are not part
     * of the document. An empty scope is Maven's shorthand for {@code compile}.
     */
    private static final Set<String> RUNTIME_SCOPES = Set.of("compile", "runtime", "");

    @Parameter(readonly = true, defaultValue = "${project}")
    @SuppressWarnings("NullAway")
    private MavenProject mavenProject;

    @Parameter(readonly = true, defaultValue = "${repositorySystemSession}")
    @SuppressWarnings("NullAway")
    private RepositorySystemSession repositorySystemSession;

    @Parameter(readonly = true, defaultValue = "${session}")
    @SuppressWarnings("NullAway")
    private MavenSession mavenSession;

    @Inject
    @SuppressWarnings("NullAway")
    private ProjectDependenciesResolver resolver;

    @Override
    public void execute() throws MojoExecutionException {
        if ("pom".equals(mavenProject.getPackaging())) {
            getLog().info("Skipping the Axelix dependency SBOM: 'pom' packaging has no runtime classpath to describe");
            return;
        }

        DependencyNode graphRoot = resolveGraph();
        Set<String> reactorModules = reactorModules();

        Map<String, DependencyNode> libraries = new LinkedHashMap<>();
        collectLibraries(graphRoot, libraries, new HashSet<>(), reactorModules);

        Map<String, Set<String>> edges = new LinkedHashMap<>();
        addEdges(rootReference(), graphRoot, edges, reactorModules);
        for (Map.Entry<String, DependencyNode> library : libraries.entrySet()) {
            addEdges(library.getKey(), library.getValue(), edges, reactorModules);
        }

        writeToFile(serialize(bomOf(libraries, edges)));
    }

    private DependencyNode resolveGraph() throws MojoExecutionException {
        try {
            DependencyResolutionResult result =
                    resolver.resolve(new DefaultDependencyResolutionRequest(mavenProject, repositorySystemSession));
            return result.getDependencyGraph();
        } catch (DependencyResolutionException e) {
            throw new MojoExecutionException(
                    "Failed to resolve the dependency graph for the Axelix dependency SBOM", e);
        }
    }

    /**
     * The {@code groupId:artifactId:version} keys of every project in this reactor. Dependencies on
     * these are the application's own modules, which the document treats as transparent.
     */
    private Set<String> reactorModules() {
        Set<String> modules = new HashSet<>();
        for (MavenProject reactorProject : mavenSession.getProjects()) {
            modules.add(reactorProject.getGroupId() + ":" + reactorProject.getArtifactId() + ":"
                    + reactorProject.getVersion());
        }
        return modules;
    }

    /**
     * Depth-first walk collecting every third-party library reachable from the root through
     * shipped scopes. Keyed by reference so a diamond is kept once; reactor modules are walked
     * through but never emitted.
     */
    private void collectLibraries(
            DependencyNode node,
            Map<String, DependencyNode> libraries,
            Set<String> visited,
            Set<String> reactorModules) {

        for (DependencyNode child : node.getChildren()) {
            // is not shipped or is an already visited node
            if (!ships(child) || !visited.add(gavOf(child.getArtifact()))) {
                continue;
            }
            if (!reactorModules.contains(gavOf(child.getArtifact()))) {
                libraries.put(referenceOf(child.getArtifact()), child);
            }
            collectLibraries(child, libraries, visited, reactorModules);
        }
    }

    /**
     * Records the outgoing edges of an emitted node (the application or a library). A reactor
     * module sitting between two nodes — e.g. {@code app -> my-lib-module -> slf4j} — is
     * transparent: its nearest library descendants are attributed to the emitted node instead, so
     * the path stays {@code app -> slf4j} without leaking the internal module structure that
     * Master has no use for.
     */
    private void addEdges(
            String fromReference, DependencyNode from, Map<String, Set<String>> edges, Set<String> reactorModules) {

        Set<String> targets = new LinkedHashSet<>();
        collectNearestLibraries(from, targets, new HashSet<>(), reactorModules);
        if (!targets.isEmpty()) {
            edges.put(fromReference, targets);
        }
    }

    private void collectNearestLibraries(
            DependencyNode node, Set<String> targets, Set<String> visited, Set<String> reactorModules) {

        for (DependencyNode child : node.getChildren()) {
            if (!ships(child) || !visited.add(gavOf(child.getArtifact()))) {
                continue;
            }
            if (reactorModules.contains(gavOf(child.getArtifact()))) {
                collectNearestLibraries(child, targets, visited, reactorModules);
            } else {
                targets.add(referenceOf(child.getArtifact()));
            }
        }
    }

    private static boolean ships(DependencyNode node) {
        org.eclipse.aether.graph.Dependency dependency = node.getDependency();
        return dependency == null || RUNTIME_SCOPES.contains(dependency.getScope());
    }

    private static String gavOf(Artifact artifact) {
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();
    }

    /**
     * The package URL Master parses back into coordinates, so the {@code group/name@version} form
     * must be exact — it matches the reference the Gradle plugin generates.
     */
    private static String referenceOf(Artifact artifact) {
        return "pkg:maven/" + artifact.getGroupId() + "/" + artifact.getArtifactId() + "@" + artifact.getVersion()
                + "?type=jar";
    }

    private String rootReference() {
        return "pkg:maven/" + mavenProject.getGroupId() + "/" + mavenProject.getArtifactId() + "@"
                + mavenProject.getVersion() + "?type=jar";
    }

    /**
     * Assembles a CycloneDX 1.6 document from the collected graph: a metadata component for the
     * application, a flat list of library components, and the {@code dependsOn} edges.
     */
    private Bom bomOf(Map<String, DependencyNode> libraries, Map<String, Set<String>> edges) {
        Bom bom = new Bom();

        Metadata metadata = new Metadata();
        metadata.setComponent(componentOf(
                mavenProject.getGroupId(),
                mavenProject.getArtifactId(),
                mavenProject.getVersion(),
                Component.Type.APPLICATION));
        bom.setMetadata(metadata);

        List<Component> components = new ArrayList<>();
        for (DependencyNode library : libraries.values()) {
            Artifact artifact = library.getArtifact();
            components.add(componentOf(
                    artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), Component.Type.LIBRARY));
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

    private static Component componentOf(String group, String name, String version, Component.Type type) {
        String reference = "pkg:maven/" + group + "/" + name + "@" + version + "?type=jar";

        Component component = new Component();
        component.setType(type);
        component.setBomRef(reference);
        component.setGroup(group);
        component.setName(name);
        component.setVersion(version);
        component.setPurl(reference);
        return component;
    }

    private static String serialize(Bom bom) throws MojoExecutionException {
        try {
            return BomGeneratorFactory.createJson(Version.VERSION_16, bom).toJsonString(true);
        } catch (GeneratorException e) {
            throw new MojoExecutionException("Failed to serialize the Axelix dependency SBOM", e);
        }
    }

    private void writeToFile(String json) throws MojoExecutionException {
        Path target = Paths.get(mavenProject.getBuild().getOutputDirectory()).resolve(SBOM_RESOURCE_PATH);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write the Axelix dependency SBOM to " + target, e);
        }
    }
}
