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
package com.axelixlabs.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * In-process integration tests driving the real Mojo (looked up through its generated plugin
 * descriptor) with the maven-plugin-testing-harness against representative Spring Boot 2 and Spring
 * Boot 4 application POMs.
 *
 * <p>The harness does not perform real transitive dependency resolution, so each scenario's resolved
 * classpath is simulated by setting the project's artifacts before execution; the rest of the wiring
 * (descriptor lookup, parameter-default injection, model mutation) is exercised end-to-end.
 */
public class AddTestDependenciesMojoIntegrationTest {

    private static final String GOAL = "add-test-dependencies";

    private static final String PROFILER_GROUP_ID = "digital.pragmatech.testing";
    private static final String PROFILER_ARTIFACT_ID = "spring-test-profiler";
    private static final String THYMELEAF_GROUP_ID = "org.thymeleaf";
    private static final String THYMELEAF_ARTIFACT_ID = "thymeleaf";

    private static final String SPRING_FACTORIES_PATH = "META-INF/spring.factories";
    private static final String FRESH_SPRING_FACTORIES = "org.springframework.test.context.TestExecutionListener=\\\n"
            + "digital.pragmatech.testing.SpringTestProfilerListener\n"
            + "org.springframework.context.ApplicationContextInitializer=\\\n"
            + "digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer\n";

    @Rule
    public final MojoRule rule = new MojoRule();

    @Test
    public void springBoot2AppGetsProfilerAndThymeleafUpgrade() throws Exception {
        // given
        // A Spring Boot 2 app whose resolved classpath has an outdated thymeleaf and no profiler.
        MavenProject project = readProject("/sb2-app");
        project.setArtifacts(artifacts(thymeleaf("3.0.15")));

        // when
        execute(project);

        // then
        List<Dependency> added = addedDependencies(project);
        assertThat(added).hasSize(2);
        assertDependency(
                findByArtifactId(added, PROFILER_ARTIFACT_ID), PROFILER_GROUP_ID, PROFILER_ARTIFACT_ID, "0.1.2");
        assertDependency(
                findByArtifactId(added, THYMELEAF_ARTIFACT_ID), THYMELEAF_GROUP_ID, THYMELEAF_ARTIFACT_ID, "3.1.5");
        assertThat(readGeneratedSpringFactories(project)).isEqualTo(FRESH_SPRING_FACTORIES);
    }

    @Test
    public void springBoot4AppWithUpToDateClasspathGetsNothingButStillRegistersSpringFactories() throws Exception {
        // given
        // A Spring Boot 4 app that already has the profiler and a current thymeleaf on the classpath.
        MavenProject project = readProject("/sb4-app");
        project.setArtifacts(artifacts(thymeleaf("3.1.6"), profiler("0.1.2")));

        // when
        execute(project);

        // then
        assertThat(addedDependencies(project)).isEmpty();
        assertThat(readGeneratedSpringFactories(project)).isEqualTo(FRESH_SPRING_FACTORIES);
    }

    @Test
    public void mergesExistingProjectSpringFactories() throws Exception {
        // given
        // A Spring Boot 4 app that already declares its own spring.factories on a test-resource root.
        MavenProject project = readProject("/sb4-app");
        project.setArtifacts(artifacts(thymeleaf("3.1.6"), profiler("0.1.2")));
        project.addTestResource(existingSpringFactoriesResource());

        // when
        execute(project);

        // then
        Properties merged = parse(readGeneratedSpringFactories(project));
        assertThat(valuesOf(merged, "org.springframework.test.context.TestExecutionListener"))
                .containsExactlyInAnyOrder(
                        "com.app.AppListener", "digital.pragmatech.testing.SpringTestProfilerListener");
        assertThat(valuesOf(merged, "org.springframework.context.ApplicationContextInitializer"))
                .containsExactly("digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer");
        assertThat(valuesOf(merged, "org.springframework.boot.SpringApplicationRunListener"))
                .containsExactly("com.app.AppRunListener");
    }

    @Test
    public void springBoot4AppMissingOnlyProfilerGetsProfilerOnly() throws Exception {
        // given
        // Current thymeleaf is on the classpath, but the profiler is missing.
        MavenProject project = readProject("/sb4-app");
        project.setArtifacts(artifacts(thymeleaf("3.1.6")));

        // when
        execute(project);

        // then
        List<Dependency> added = addedDependencies(project);
        assertThat(added).hasSize(1);
        assertDependency(
                findByArtifactId(added, PROFILER_ARTIFACT_ID), PROFILER_GROUP_ID, PROFILER_ARTIFACT_ID, "0.1.2");
    }

    /**
     * Reads the {@code META-INF/spring.factories} the Mojo generated, located through the test resource
     * it registered on the project (whose directory is the generated {@code .../axelix} root).
     */
    private static String readGeneratedSpringFactories(MavenProject project) throws IOException {
        List<Resource> generated = project.getTestResources().stream()
                .filter(resource -> resource.getDirectory() != null
                        && resource.getDirectory().replace('\\', '/').endsWith("generated-test-resources/axelix"))
                .collect(Collectors.toList());
        assertThat(generated).hasSize(1);

        File file = new File(generated.get(0).getDirectory(), SPRING_FACTORIES_PATH);
        assertThat(file).isFile();
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    private Resource existingSpringFactoriesResource() throws Exception {
        Resource resource = new Resource();
        resource.setDirectory(
                new File(getClass().getResource("/existing-spring-factories").toURI()).getAbsolutePath());
        return resource;
    }

    private static Properties parse(String content) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = new StringReader(content)) {
            properties.load(reader);
        }
        return properties;
    }

    private static List<String> valuesOf(Properties properties, String key) {
        return List.of(properties.getProperty(key, "").split(","));
    }

    private MavenProject readProject(String classpathDir) throws Exception {
        File baseDir = new File(getClass().getResource(classpathDir).toURI());
        return rule.readMavenProject(baseDir);
    }

    private void execute(MavenProject project) throws Exception {
        AddTestDependenciesMojo mojo = rule.lookupConfiguredMojo(project, GOAL);
        mojo.execute();
    }

    /**
     * Returns the dependencies our Mojo injects (identified by their well-known artifactIds), so the
     * assertions are independent of whatever the fixture POM itself declares.
     */
    private static List<Dependency> addedDependencies(MavenProject project) {
        return project.getDependencies().stream()
                .filter(dependency -> PROFILER_ARTIFACT_ID.equals(dependency.getArtifactId())
                        || THYMELEAF_ARTIFACT_ID.equals(dependency.getArtifactId()))
                .collect(Collectors.toList());
    }

    private static Dependency findByArtifactId(List<Dependency> dependencies, String artifactId) {
        return dependencies.stream()
                .filter(dependency -> artifactId.equals(dependency.getArtifactId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected an added dependency with artifactId " + artifactId));
    }

    private static void assertDependency(Dependency dependency, String groupId, String artifactId, String version) {
        assertThat(dependency.getGroupId()).isEqualTo(groupId);
        assertThat(dependency.getArtifactId()).isEqualTo(artifactId);
        assertThat(dependency.getVersion()).isEqualTo(version);
        assertThat(dependency.getScope()).isEqualTo("test");
    }

    private static Set<Artifact> artifacts(Artifact... artifacts) {
        return new LinkedHashSet<>(List.of(artifacts));
    }

    private static Artifact profiler(String version) {
        return artifact(PROFILER_GROUP_ID, PROFILER_ARTIFACT_ID, version);
    }

    private static Artifact thymeleaf(String version) {
        return artifact(THYMELEAF_GROUP_ID, THYMELEAF_ARTIFACT_ID, version);
    }

    private static Artifact artifact(String groupId, String artifactId, String version) {
        return new DefaultArtifact(
                groupId, artifactId, version, "test", "jar", null, new DefaultArtifactHandler("jar"));
    }
}
