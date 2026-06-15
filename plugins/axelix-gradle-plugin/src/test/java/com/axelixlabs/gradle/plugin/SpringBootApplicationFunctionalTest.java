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
package com.axelixlabs.gradle.plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the plugin end-to-end on empty Spring Boot applications: after running their tests,
 * the Spring Test Profiler must have produced its HTML report. The profiler requires Java 17+,
 * so these builds run on the current JVM.
 */
class SpringBootApplicationFunctionalTest {

    /** Boot-2-era Gradle; the io.spring.dependency-management plugin predates Gradle 9. */
    private static final String SPRING_BOOT_2_GRADLE_VERSION = "8.14";

    @TempDir
    Path projectDir;

    /**
     * The realistic Spring Boot 2 setup: the io.spring.dependency-management plugin applies the
     * Boot BOM with Maven semantics, managing Thymeleaf at 3.0.x — without the plugin's explicit
     * Thymeleaf 3.1 test dependency the report silently fails to render.
     */
    @Test
    void springBoot2WithDependencyManagementGeneratesProfilerReport() throws IOException {
        // given.
        writeSpringBootApplicationSources();
        writeFile("build.gradle", GradleProjectFixtures.buildScript("spring-boot-2.gradle"));

        // when.
        BuildResult result = createRunner("test", "--stacktrace")
                .withGradleVersion(SPRING_BOOT_2_GRADLE_VERSION)
                .build();

        // then.
        assertProfilerReportGenerated(result);
    }

    @Test
    void springBoot4GeneratesProfilerReport() throws IOException {
        // given.
        writeSpringBootApplicationSources();
        writeFile("build.gradle", springBoot4BuildScript());

        // when.
        BuildResult result = createRunner("test", "--stacktrace").build();

        // then.
        assertProfilerReportGenerated(result);
    }

    @Test
    void buildCopiesProfilerReportOntoApplicationClasspathAndIntoJar() throws IOException {
        // given.
        writeSpringBootApplicationSources();
        writeFile("build.gradle", springBoot4BuildScript());

        // when.
        BuildResult result = createRunner("build", "--stacktrace").build();

        // then.
        assertThat(result.task(":test").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.task(":copyAxelixTestProfilerReport").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(projectDir.resolve("build/resources/main/spring-test-profiler/latest.html"))
                .exists();

        Path jar = projectDir.resolve("build/libs/axelix-spring-boot-test.jar");
        assertThat(jar).exists();
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            assertThat(jarFile.getEntry("spring-test-profiler/latest.html")).isNotNull();
        }
    }

    private GradleRunner createRunner(String... arguments) {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments(arguments);
    }

    private void assertProfilerReportGenerated(BuildResult result) {
        assertThat(result.task(":test").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.task(":generateAxelixSpringFactories").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(projectDir.resolve("build/spring-test-profiler/latest.html")).exists();
    }

    private static String springBoot4BuildScript() {
        return GradleProjectFixtures.buildScript("spring-boot-4.gradle");
    }

    private void writeSpringBootApplicationSources() throws IOException {
        writeFile("settings.gradle", "rootProject.name = 'axelix-spring-boot-test'\n");
        writeFile(
                "src/main/java/com/example/DemoApplication.java",
                GradleProjectFixtures.javaSource("DemoApplication.java"));
        writeFile(
                "src/test/java/com/example/DemoApplicationTest.java",
                GradleProjectFixtures.javaSource("DemoApplicationTest.java"));
    }

    private void writeFile(String relativePath, String content) throws IOException {
        Path file = projectDir.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(UTF_8));
    }
}
