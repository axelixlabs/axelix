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
package com.axelixlabs.axelix.gradle.plugin.projectinfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.axelixlabs.axelix.gradle.plugin.AbstractAxelixPluginFunctionalTest;
import com.axelixlabs.axelix.gradle.plugin.GradleProjectFixtures;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Functional tests for {@link ProjectInfoGenerator}.
 *
 * @author Nikita Kirillov
 */
class ProjectInfoGeneratorFunctionalTest extends AbstractAxelixPluginFunctionalTest {

    private static final String GENERATE_TASK = ProjectInfoGenerator.GENERATE_TASK_NAME;
    private static final String PROPERTIES_PATH = "META-INF/axelix-info.properties";

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void generatesBuildAndGitInfoTogether(String gradleVersion) throws IOException, InterruptedException {
        // given.
        setupProject("build-info.gradle.kts");
        initGitRepository();

        // when.
        BuildResult result =
                createRunner(gradleVersion, GENERATE_TASK, "--stacktrace").build();

        // then.
        assertThat(result.task(":" + GENERATE_TASK).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertGitBuildProperties(loadProperties(), true);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void writesBuildInfoOnlyWhenNotInsideAGitRepository(String gradleVersion) throws IOException {
        // given. group/version are set, but no git repository initialized.
        setupProject("build-info.gradle.kts");

        // when.
        BuildResult result =
                createRunner(gradleVersion, GENERATE_TASK, "--stacktrace").build();

        // then.
        assertThat(result.task(":" + GENERATE_TASK).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertGitBuildProperties(loadProperties(), false);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void failsOnlyWhenProjectInfoIsActuallyGeneratedWithoutGroupSet(String gradleVersion) throws IOException {
        // given.
        setupProject("no-group.gradle.kts");

        // when. an unrelated task must succeed even though group is unset.
        BuildResult unrelated =
                createRunner(gradleVersion, "tasks", "--stacktrace").build();

        // then.
        assertThat(unrelated.task(":tasks").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);

        // when. only generating project info itself must fail, and only then; git info is never
        // even attempted since build-info validation runs first.
        BuildResult projectInfo = createRunner(gradleVersion, GENERATE_TASK).buildAndFail();

        // then.
        assertThat(projectInfo.getOutput()).contains("Axelix requires 'group' to be set");
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void packagesProjectInfoIntoBootJarEvenWithProfilerDisabled(String gradleVersion)
            throws IOException, InterruptedException {
        // given. the fixture disables the profiler extension entirely, to prove build/git info
        // collection is unconditional and independent of it.
        setupProject("build-info.gradle.kts");
        initGitRepository();

        // when.
        BuildResult result =
                createRunner(gradleVersion, "bootJar", "--stacktrace").build();

        // then.
        assertThat(result.task(":bootJar").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertGitBuildProperties(loadPropertiesFromJar(), true);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void packagesProjectInfoIntoPlainJarWhenTheresNoBootJarTask(String gradleVersion)
            throws IOException, InterruptedException {
        // given. no Spring Boot plugin applied, so only the standard 'jar' task exists.
        setupProject("plain-jar-build-info.gradle.kts");
        initGitRepository();

        // when. the 'build' lifecycle task, not a task we name explicitly.
        BuildResult result =
                createRunner(gradleVersion, "build", "--stacktrace").build();

        // then.
        assertThat(result.task(":jar").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.task(":" + GENERATE_TASK).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertGitBuildProperties(loadPropertiesFromJar(), true);
    }

    private void setupProject(String buildGradleFixture) throws IOException {
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        String buildFileName = buildGradleFixture.endsWith(".kts") ? "build.gradle.kts" : "build.gradle";
        writeFile(buildFileName, GradleProjectFixtures.loadContent(buildGradleFixture));
    }

    private Properties loadProperties() throws IOException {
        Path infoFile = projectDir.resolve("build/generated/axelix-info/" + PROPERTIES_PATH);
        assertThat(infoFile).exists();
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(infoFile)) {
            properties.load(inputStream);
        }
        return properties;
    }

    private Properties loadPropertiesFromJar() throws IOException {
        Path jar = projectDir.resolve("build/libs/axelix-plugin-test-1.2.3.jar");
        assertThat(jar).exists();

        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry entry = zip.getEntry(PROPERTIES_PATH);
            assertThat(entry).isNotNull();

            Properties properties = new Properties();
            try (InputStream in = zip.getInputStream(entry)) {
                properties.load(in);
            }
            return properties;
        }
    }

    private void assertGitBuildProperties(Properties properties, boolean hasGitInfo) {
        assertThat(properties.getProperty("build.group")).isEqualTo("com.example");
        assertThat(properties.getProperty("build.name")).isEqualTo("axelix-plugin-test");
        assertThat(properties.getProperty("build.version")).isEqualTo("1.2.3");
        assertThat(properties.getProperty("build.time")).isNotBlank();

        if (hasGitInfo) {
            assertThat(properties.getProperty("git.commit.id")).hasSize(40);
            assertThat(properties.getProperty("git.commit.id.abbrev")).hasSize(7);
            assertThat(properties.getProperty("git.branch")).isNotBlank();
            assertThat(properties.getProperty("git.commit.user.name")).isEqualTo("Test User");
            assertThat(properties.getProperty("git.commit.user.email")).isEqualTo("test@example.com");
            assertThat(properties.getProperty("git.commit.time")).isNotBlank();

        } else {
            assertThat(properties.getProperty("git.commit.id")).isNull();
            assertThat(properties.getProperty("git.commit.id.abbrev")).isNull();
            assertThat(properties.getProperty("git.branch")).isNull();
            assertThat(properties.getProperty("git.commit.user.name")).isNull();
            assertThat(properties.getProperty("git.commit.user.email")).isNull();
            assertThat(properties.getProperty("git.commit.time")).isNull();
        }
    }
}
