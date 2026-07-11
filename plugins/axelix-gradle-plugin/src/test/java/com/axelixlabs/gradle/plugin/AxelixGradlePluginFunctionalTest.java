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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class AxelixGradlePluginFunctionalTest {

    /**
     * Gradle versions exercised by the enclosing test task, supplied as a comma-separated list via the
     * {@code axelix.test.gradle.versions} system property. No single JVM can launch the whole supported
     * range (Gradle 5-7 require Java &lt;= 11, Gradle 9 requires Java &gt;= 17), so the {@code test} and
     * {@code legacyGradleTest} tasks each pass the subset valid for their toolchain. Falls back to a single
     * modern version for IDE runs.
     */
    static List<String> gradleVersionsUnderTest() {
        String versions = System.getProperty("axelix.test.gradle.versions");
        if (versions == null || versions.trim().isEmpty()) {
            return List.of("9.5.1");
        }
        return Arrays.stream(versions.split(","))
                .map(String::trim)
                .filter(version -> !version.isEmpty())
                .collect(Collectors.toList());
    }

    private static final String PREEXISTING_SPRING_FACTORIES_CONTENT =
            "com.example.CustomFactory=com.example.CustomFactoryImpl\n";

    @TempDir
    Path projectDir;

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void addsBothProfilerAndThymeleafDependenciesToTestRuntimeClasspathWhenNothingPresent(String gradleVersion)
            throws IOException {
        // given.
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        writeFile("build.gradle", GradleProjectFixtures.loadContent("no-thymeleaf-and-profiler.gradle"));

        // when.
        BuildResult result = createRunner(gradleVersion, "printTestRuntimeClasspath", "--stacktrace", "-i")
                .build();

        // then.
        assertThat(result.task(":printTestRuntimeClasspath").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.task(":generateAxelixSpringFactories").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertGeneratedSpringFactoriesContainOnlyProfilerRegistration();
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void appendsProfilerRegistrationWithoutOverridingPreexistingSpringFactories(String gradleVersion)
            throws IOException {
        // given.
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        writeFile("build.gradle", GradleProjectFixtures.loadContent("preexisting-spring-factories.gradle"));

        // when.
        BuildResult result =
                createRunner(gradleVersion, "generateAxelixSpringFactories", "--stacktrace").build();

        // then.
        assertThat(result.task(":generateAxelixSpringFactories").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        Path springFactories = projectDir.resolve("build/generated/axelix/META-INF/spring.factories");
        String actual = new String(Files.readAllBytes(springFactories), UTF_8);
        assertThat(actual).isEqualTo(PREEXISTING_SPRING_FACTORIES_CONTENT + SpringFactoriesGenerator.SPRING_FACTORIES_CONTENT);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void doesNotContributeAnythingWhenProfilerAndThymeleafAreAlreadyInClassPath(String gradleVersion)
            throws IOException {
        // given.
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        writeFile("build.gradle", GradleProjectFixtures.loadContent("preexisting-thymeleaf-and-profiler.gradle"));

        // when.
        BuildResult result =
                createRunner(gradleVersion, "printDeclaredDeps", "--stacktrace").build();

        // then.
        assertThat(result.task(":printDeclaredDeps").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput())
                .contains(AxelixGradlePlugin.PROFILER_NAME + ":0.1.1")
                .contains(AxelixGradlePlugin.THYMELEAF_NAME + ":3.1.5.RELEASE")
                // dependency that we want to add
                .doesNotContain(AxelixGradlePlugin.PROFILER_DEPENDENCY);
        assertSpringFactoriesAreNotGenerated(result);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void noOpWhenDeclaredThymeleafVersionIsBelowMinimumAndProfilerIsNotPresent(String gradleVersion)
            throws IOException {
        // given.
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        writeFile("build.gradle", GradleProjectFixtures.loadContent("preexisitng-incompatible-thymeleaf.gradle"));

        // when.
        BuildResult result =
                createRunner(gradleVersion, "printDeclaredDeps", "--stacktrace").build();

        // then.
        assertThat(result.task(":printDeclaredDeps").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput())
                .contains(AxelixGradlePlugin.THYMELEAF_NAME + ":3.0.15.RELEASE")
                .doesNotContain(AxelixGradlePlugin.THYMELEAF_DEPENDENCY)
                .doesNotContain(AxelixGradlePlugin.PROFILER_NAME);
        assertSpringFactoriesAreNotGenerated(result);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void shouldOnlyAddProfilerAndNotAlterTheThymeleafWhenCompatibleThymeleafIsInTheClassPath(String gradleVersion)
            throws IOException {
        // given.
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        writeFile("build.gradle", GradleProjectFixtures.loadContent("preexisitng-compatible-thymeleaf.gradle"));

        // when.
        BuildResult result = createRunner(
                        gradleVersion, "printDeclaredDeps", "generateAxelixSpringFactories", "--stacktrace")
                .build();

        // then.
        assertThat(result.task(":printDeclaredDeps").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput())
                .contains(AxelixGradlePlugin.THYMELEAF_NAME + ":3.1.4.RELEASE")
                .doesNotContain(AxelixGradlePlugin.THYMELEAF_DEPENDENCY)
                .contains(AxelixGradlePlugin.PROFILER_DEPENDENCY);
        assertThat(result.task(":generateAxelixSpringFactories").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertGeneratedSpringFactoriesContainOnlyProfilerRegistration();
    }

    private void assertGeneratedSpringFactoriesContainOnlyProfilerRegistration() throws IOException {
        Path springFactories = projectDir.resolve("build/generated/axelix/META-INF/spring.factories");
        assertThat(springFactories).exists();
        assertThat(new String(Files.readAllBytes(springFactories), UTF_8))
                .isEqualTo(SpringFactoriesGenerator.SPRING_FACTORIES_CONTENT);
    }

    private void assertSpringFactoriesAreNotGenerated(BuildResult result) {
        assertThat(result.task(":generateAxelixSpringFactories")).isNull();
        assertThat(projectDir.resolve("build/generated/axelix/META-INF/spring.factories"))
                .doesNotExist();
    }

    private GradleRunner createRunner(String gradleVersion, String... arguments) {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withGradleVersion(gradleVersion)
                .withArguments(arguments);
    }

    private void writeFile(String relativePath, String content) throws IOException {
        Path file = projectDir.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(UTF_8));
    }
}
