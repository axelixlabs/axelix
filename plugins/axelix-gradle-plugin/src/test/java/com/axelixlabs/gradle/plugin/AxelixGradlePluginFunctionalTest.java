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

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class AxelixGradlePluginFunctionalTest {

    private static final String MIN_GRADLE_VERSION = "5.0";
    private static final String MAX_GRADLE_VERSION = "9.5.1";

    @TempDir
    Path projectDir;

    @ParameterizedTest
    @ValueSource(strings = {MIN_GRADLE_VERSION, MAX_GRADLE_VERSION})
    void addsProfilerDependencyToTestRuntimeClasspath(String gradleVersion) throws IOException {
        // given.
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        writeFile("build.gradle", GradleProjectFixtures.buildScript("profiler-dependency.gradle"));

        // when.
        BuildResult result = createRunner(gradleVersion, "printTestRuntimeClasspath", "--stacktrace", "-i")
                .build();

        // then.
        assertThat(result.task(":printTestRuntimeClasspath").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput()).contains("spring-test-profiler-0.1.2.jar");
    }

    @ParameterizedTest
    @ValueSource(strings = {MIN_GRADLE_VERSION, MAX_GRADLE_VERSION})
    void doesNotContributeProfilerOrThymeleafWhenAlreadyDeclared(String gradleVersion) throws IOException {
        // given.
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        writeFile("build.gradle", GradleProjectFixtures.buildScript("preexisting-versions.gradle"));

        // when.
        BuildResult result =
                createRunner(gradleVersion, "printDeclaredDeps", "--stacktrace").build();

        // then.
        assertThat(result.task(":printDeclaredDeps").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput())
                .contains("DEP>> digital.pragmatech.testing:spring-test-profiler:0.1.1")
                .doesNotContain("digital.pragmatech.testing:spring-test-profiler:0.1.2")
                .contains("DEP>> org.thymeleaf:thymeleaf:3.1.5.RELEASE")
                .doesNotContain("org.thymeleaf:thymeleaf:3.1.3.RELEASE");
    }

    @ParameterizedTest
    @ValueSource(strings = {MIN_GRADLE_VERSION, MAX_GRADLE_VERSION})
    void noOpWhenDeclaredThymeleafVersionIsBelowMinimum(String gradleVersion) throws IOException {
        // given.
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        writeFile("build.gradle", GradleProjectFixtures.buildScript("outdated-thymeleaf.gradle"));

        // when.
        BuildResult result =
                createRunner(gradleVersion, "printDeclaredDeps", "--stacktrace").build();

        // then.
        assertThat(result.task(":printDeclaredDeps").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput())
                .contains("DEP>> org.thymeleaf:thymeleaf:3.0.15.RELEASE")
                .doesNotContain("org.thymeleaf:thymeleaf:3.1.3.RELEASE")
                .doesNotContain("digital.pragmatech.testing:spring-test-profiler");
    }

    private GradleRunner createRunner(String gradleVersion, String... arguments) {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withGradleVersion(gradleVersion)
                .withArguments(arguments)
                .withDebug(true);
    }

    private void writeFile(String relativePath, String content) throws IOException {
        Path file = projectDir.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(UTF_8));
    }
}
