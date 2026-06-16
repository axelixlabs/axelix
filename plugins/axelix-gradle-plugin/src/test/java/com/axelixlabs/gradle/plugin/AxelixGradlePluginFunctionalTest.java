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

    private static final String MIN_GRADLE_VERSION = "4.0";
    private static final String MAX_GRADLE_VERSION = "9.5.1";

    private static final String EXPECTED_SPRING_FACTORIES =
            "org.springframework.test.context.TestExecutionListener=\\\n"
                    + "digital.pragmatech.testing.SpringTestProfilerListener\n"
                    + "org.springframework.context.ApplicationContextInitializer=\\\n"
                    + "digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer\n";

    @TempDir
    Path projectDir;

    @ParameterizedTest
    @ValueSource(strings = {MIN_GRADLE_VERSION, MAX_GRADLE_VERSION})
    void addsProfilerDependencyAndGeneratesSpringFactories(String gradleVersion) throws IOException {
        // given.
        writeCommonProjectFiles(gradleVersion);
        writeFile("build.gradle", GradleProjectFixtures.buildScript("profiler-dependency.gradle"));

        // when.
        BuildResult result = createRunner(gradleVersion, "printTestRuntimeClasspath", "--stacktrace")
                .build();

        // then.
        assertThat(result.task(":printTestRuntimeClasspath").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.task(":generateAxelixSpringFactories").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput()).contains("spring-test-profiler-0.1.2.jar");
        assertThat(result.getOutput()
                        .lines()
                        .filter(line -> line.startsWith("TRC>> "))
                        .map(line -> line.replace('\\', '/')))
                .anySatisfy(line -> assertThat(line).endsWith("build/generated/axelix"));

        Path springFactories = projectDir.resolve("build/generated/axelix/META-INF/spring.factories");
        assertThat(springFactories).exists();
        assertThat(new String(Files.readAllBytes(springFactories), UTF_8)).isEqualTo(EXPECTED_SPRING_FACTORIES);
    }

    @ParameterizedTest
    @ValueSource(strings = {MIN_GRADLE_VERSION, MAX_GRADLE_VERSION})
    void doesNotContributeProfilerOrThymeleafWhenAlreadyDeclared(String gradleVersion) throws IOException {
        // given.
        writeCommonProjectFiles(gradleVersion);
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
    void bumpsThymeleafWhenDeclaredVersionIsBelowMinimum(String gradleVersion) throws IOException {
        // given.
        writeCommonProjectFiles(gradleVersion);
        writeFile("build.gradle", GradleProjectFixtures.buildScript("outdated-thymeleaf.gradle"));

        // when.
        BuildResult result =
                createRunner(gradleVersion, "printDeclaredDeps", "--stacktrace").build();

        // then.
        assertThat(result.task(":printDeclaredDeps").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput())
                .contains("DEP>> org.thymeleaf:thymeleaf:3.0.15.RELEASE")
                .contains("DEP>> org.thymeleaf:thymeleaf:3.1.3.RELEASE");
    }

    @ParameterizedTest
    @ValueSource(strings = {MIN_GRADLE_VERSION, MAX_GRADLE_VERSION})
    void springFactoriesIsVisibleToTestsAtRuntime(String gradleVersion) throws IOException {
        // given.
        writeCommonProjectFiles(gradleVersion);
        writeFile("build.gradle", GradleProjectFixtures.buildScript("spring-factories-visible.gradle"));
        // The test source must stay Java-8 compatible: on Gradle 4.0 it is compiled by JDK 8.
        writeFile(
                "src/test/java/FactoriesVisibleTest.java",
                GradleProjectFixtures.javaSource("FactoriesVisibleTest.java"));

        // when.
        BuildResult result = createRunner(gradleVersion, "test", "--stacktrace").build();

        // then.
        assertThat(result.task(":test").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    }

    private GradleRunner createRunner(String gradleVersion, String... arguments) {
        // Never call withDebug(true) here: a debug run executes the build in-process on the
        // current (modern) JVM, bypassing the JDK 8 daemon required by Gradle 4.0.
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withGradleVersion(gradleVersion)
                .withArguments(arguments);
    }

    private void writeCommonProjectFiles(String gradleVersion) throws IOException {
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        if (gradleVersion.startsWith("4.")) {
            // Gradle 4.0 daemons cannot run on Java 9+, so fork them on a JDK 8.
            writeFile(
                    "gradle.properties",
                    "org.gradle.java.home=" + locateJdk8Home() + "\n" + "org.gradle.jvmargs=-Xmx512m\n");
        }
    }

    private void writeFile(String relativePath, String content) throws IOException {
        Path file = projectDir.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(UTF_8));
    }

    private static String locateJdk8Home() {
        String override = System.getenv("AXELIX_TEST_JDK8_HOME");
        if (override != null && !override.isEmpty()) {
            return override;
        }
        throw new IllegalStateException("No JDK 8 found for the Gradle "
                + MIN_GRADLE_VERSION
                + " functional tests. Install Liberica JDK 8 via sdkman:\n"
                + "  source ~/.sdkman/bin/sdkman-init.sh && echo n | sdk install java"
                + " 8.0.492-librca\n"
                + "and point AXELIX_TEST_JDK8_HOME at the JDK 8 installation.");
    }
}
