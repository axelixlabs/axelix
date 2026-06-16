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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Drives a real Maven build against bundled sample Spring Boot 2 and Spring Boot 4 projects and
 * asserts that the plugin injects the spring-test-profiler dependency, raises Thymeleaf to the floor
 * and writes the merged {@code META-INF/spring.factories} onto the test classpath.
 *
 * <p>The samples are built only up to {@code process-test-resources} plus {@code build-classpath}, so
 * the assertions cover dependency resolution and resource generation without needing to run the
 * Spring context (and therefore without a specific JDK requirement). Each test is skipped when no
 * Maven installation can be located.
 */
class AxelixMavenPluginIntegrationTest {

    private static final String MAVEN_HOME = System.getProperty("axelix.it.maven.home", "");
    private static final String PLUGIN_VERSION = System.getProperty("axelix.it.plugin.version", "");
    private static final String PROJECTS_DIR = System.getProperty("axelix.it.projects.dir", "");

    @BeforeEach
    void mavenInstallationMustBeAvailable() {
        assumeTrue(
                !MAVEN_HOME.isEmpty() && new File(MAVEN_HOME).isDirectory(), "No Maven installation found; skipping.");
        assumeTrue(
                !PROJECTS_DIR.isEmpty() && new File(PROJECTS_DIR).isDirectory(), "Sample projects missing; skipping.");
    }

    @Test
    void springBoot2SampleMergesFactoriesAndBumpsThymeleaf(@TempDir Path workspace) throws Exception {
        // given
        Path project = copyProject("spring-boot-2-sample", workspace);

        // when
        build(project);

        // then
        String factories = read(project.resolve("target/test-classes/META-INF/spring.factories"));
        assertThat(factories)
                .contains("com.example.sb2.NoOpTestExecutionListener,"
                        + "digital.pragmatech.testing.SpringTestProfilerListener")
                .contains("org.springframework.context.ApplicationContextInitializer="
                        + "digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer");

        String classpath = read(project.resolve("cp.txt"));
        assertThat(classpath)
                .contains("spring-test-profiler")
                .contains("0.1.2")
                .contains("thymeleaf")
                .contains("3.1.5.RELEASE");
    }

    @Test
    void springBoot4SampleCreatesFactoriesAndAddsThymeleaf(@TempDir Path workspace) throws Exception {
        // given
        Path project = copyProject("spring-boot-4-sample", workspace);

        // when
        build(project);

        // then
        String factories = read(project.resolve("target/test-classes/META-INF/spring.factories"));
        assertThat(factories)
                .contains("org.springframework.test.context.TestExecutionListener="
                        + "digital.pragmatech.testing.SpringTestProfilerListener")
                .contains("org.springframework.context.ApplicationContextInitializer="
                        + "digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer");

        String classpath = read(project.resolve("cp.txt"));
        assertThat(classpath)
                .contains("spring-test-profiler")
                .contains("0.1.2")
                .contains("thymeleaf")
                .contains("3.1.5.RELEASE");
    }

    private static void build(Path project) throws MavenInvocationException {
        List<String> log = new ArrayList<>();

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(project.resolve("pom.xml").toFile());
        request.setGoals(Arrays.asList("process-test-resources", "dependency:build-classpath"));
        request.setBatchMode(true);
        request.setShowErrors(true);
        request.setJavaHome(new File(System.getProperty("java.home")));
        request.setProperties(buildProperties());
        request.setOutputHandler(log::add);
        request.setErrorHandler(log::add);

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(MAVEN_HOME));

        InvocationResult result = invoker.execute(request);
        assertThat(result.getExitCode())
                .as("Maven build failed:%n%s", String.join("\n", log))
                .isZero();
    }

    private static Properties buildProperties() {
        Properties properties = new Properties();
        properties.setProperty("axelix.plugin.version", PLUGIN_VERSION);
        properties.setProperty("mdep.includeScope", "test");
        properties.setProperty("mdep.outputFile", "cp.txt");
        return properties;
    }

    private static Path copyProject(String name, Path workspace) throws IOException {
        Path source = Paths.get(PROJECTS_DIR, name);
        Path destination = workspace.resolve(name);
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : (Iterable<Path>) paths::iterator) {
                Path target = destination.resolve(source.relativize(path).toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(path, target);
                }
            }
        }
        return destination;
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
