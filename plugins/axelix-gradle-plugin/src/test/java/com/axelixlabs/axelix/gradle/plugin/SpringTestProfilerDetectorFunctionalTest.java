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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.axelixlabs.axelix.gradle.plugin.SpringTestProfilerDetector.PROFILER_ARTIFACT_ID;
import static com.axelixlabs.axelix.gradle.plugin.SpringTestProfilerDetector.PROFILER_GROUP_ID;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Functional tests for {@link SpringTestProfilerDetector}.
 *
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
class SpringTestProfilerDetectorFunctionalTest extends AbstractAxelixPluginFunctionalTest {

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void shouldNotContributeProfilerDependencyWhenAbsent(String gradleVersion) throws IOException {
        // given.
        setupProject("no-profiler.gradle.kts");

        // when. generateAxelixProjectInfo is what actually runs detection, at task-execution time.
        BuildResult result = createRunner(
                        gradleVersion, "printDeclaredDeps", "generateAxelixProjectInfo", "--stacktrace", "-i")
                .build();

        // then. the plugin only detects and logs; it never adds the dependency itself.
        assertThat(result.getOutput()).doesNotContain(PROFILER_GROUP_ID);
        assertThat(result.getOutput()).contains("Spring Test Profiler was not detected in this build");
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void shouldDetectPresentProfilerWithoutAlteringIt(String gradleVersion) throws IOException {
        // given.
        setupProject("preexisting-profiler.gradle");

        // when. generateAxelixProjectInfo is what actually runs detection, at task-execution time.
        BuildResult result = createRunner(
                        gradleVersion, "printDeclaredDeps", "generateAxelixProjectInfo", "--stacktrace", "-i")
                .build();

        // then. the pre-declared version is left untouched, and detection is logged.
        assertThat(result.getOutput())
                .contains("DEP>> " + PROFILER_GROUP_ID + ":" + PROFILER_ARTIFACT_ID + ":0.1.1")
                .contains("Spring Test Profiler detected in this build");
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void shouldTreatUnresolvableDetectionAsProfilerNotPresentInsteadOfFailingTheBuild(String gradleVersion)
            throws IOException {
        // given. no repositories are configured at all, but a dependency IS declared, so
        // detection's own resolution attempt genuinely fails - simulating e.g. a closed network
        // with no access to the profiler's repository.
        setupProject("no-repositories.gradle.kts");

        // when.
        BuildResult result = createRunner(gradleVersion, "generateAxelixProjectInfo", "--stacktrace", "-i")
                .build();

        // then. the build succeeds without failing, treating that project as "not present".
        assertThat(result.getOutput()).contains("Spring Test Profiler was not detected in this build");
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void shouldDetectProfilerPresentAcrossSubprojectsWhenOnlySharedCommonModuleDeclaresIt(String gradleVersion)
            throws IOException {
        // given. a multi-project build with a shared "common" module that depends on the profiler,
        // and two sibling modules ("module-a", "module-b") that only depend on common - the
        // profiler is testRuntimeOnly on common, so it's never visible on the siblings' own
        // dependency graph.
        writeFile("settings.gradle", GradleProjectFixtures.loadContent("reactor-shared-common/settings.gradle"));
        writeFile("common/build.gradle", GradleProjectFixtures.loadContent("reactor-shared-common/common.gradle"));
        writeFile("module-a/build.gradle", GradleProjectFixtures.loadContent("reactor-shared-common/module-a.gradle"));
        writeFile("module-b/build.gradle", GradleProjectFixtures.loadContent("reactor-shared-common/module-b.gradle"));

        // when.
        BuildResult result = createRunner(gradleVersion, "generateAxelixProjectInfo", "--stacktrace", "-i")
                .build();

        // then. the build-wide detection is driven by "common" alone.
        assertThat(result.getOutput()).contains("Spring Test Profiler detected in this build");

        // and. project-info is generated for every subproject, all reporting the same build-wide
        // "detected" outcome - even module-a/module-b, which never resolve the profiler themselves.
        for (String module : List.of("common", "module-a", "module-b")) {
            Properties properties = loadGeneratedProperties(module);
            assertThat(properties.getProperty(SpringTestProfilerDetector.PROFILER_DETECTED_PROPERTY))
                    .isEqualTo("true");
        }
    }

    private void setupProject(String buildGradleFixture) throws IOException {
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        String buildFileName = buildGradleFixture.endsWith(".kts") ? "build.gradle.kts" : "build.gradle";
        writeFile(buildFileName, GradleProjectFixtures.loadContent(buildGradleFixture));
    }

    private Properties loadGeneratedProperties(String module) throws IOException {
        Path infoFile = projectDir.resolve(module + "/build/generated/axelix-info/META-INF/axelix-info.properties");
        assertThat(infoFile).exists();

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(infoFile)) {
            properties.load(inputStream);
        }
        return properties;
    }
}
