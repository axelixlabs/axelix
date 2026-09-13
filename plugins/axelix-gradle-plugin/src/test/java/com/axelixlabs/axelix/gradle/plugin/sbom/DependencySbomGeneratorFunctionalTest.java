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
package com.axelixlabs.axelix.gradle.plugin.sbom;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.axelixlabs.axelix.gradle.plugin.AbstractAxelixPluginFunctionalTest;
import com.axelixlabs.axelix.gradle.plugin.GradleProjectFixtures;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.axelixlabs.axelix.gradle.plugin.sbom.DependencySbomGenerator.GENERATE_TASK_NAME;
import static com.axelixlabs.axelix.gradle.plugin.sbom.DependencySbomGenerator.SBOM_RESOURCE_PATH;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Functional tests for {@link DependencySbomGenerator}, asserting a valid CycloneDX SBOM of the
 * runtime graph is generated and packaged.
 *
 * @author Mikhail Polivakha
 */
class DependencySbomGeneratorFunctionalTest extends AbstractAxelixPluginFunctionalTest {

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void generatesSbomOfTheRuntimeGraph(String gradleVersion) throws IOException {
        // given.
        setupProject();

        // when.
        BuildResult result =
                createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // then.
        assertThat(result.task(":" + GENERATE_TASK_NAME).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);

        String sbom = readGenerated();
        assertThat(sbom).contains("\"bomFormat\" : \"CycloneDX\"").contains("\"specVersion\" : \"1.6\"");
        // The root application component, and both the direct and the transitive dependency.
        assertThat(sbom).contains("pkg:maven/com.example/axelix-plugin-test@1.2.3");
        assertThat(sbom).contains("pkg:maven/org.slf4j/slf4j-simple@2.0.9?type=jar");
        assertThat(sbom).contains("pkg:maven/org.slf4j/slf4j-api@2.0.9?type=jar");
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void recordsTheResolutionEdgeFromDirectToTransitiveDependency(String gradleVersion) throws IOException {
        // given.
        setupProject();

        // when.
        createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // then. slf4j-simple must declare a dependsOn edge to slf4j-api, so Master can rebuild the
        // resolution path root -> slf4j-simple -> slf4j-api.
        String sbom = readGenerated();
        int simpleRef = sbom.indexOf("\"ref\" : \"pkg:maven/org.slf4j/slf4j-simple@2.0.9?type=jar\"");
        assertThat(simpleRef).isGreaterThan(-1);
        String simpleEntry = sbom.substring(simpleRef, sbom.indexOf(']', simpleRef));
        assertThat(simpleEntry).contains("pkg:maven/org.slf4j/slf4j-api@2.0.9?type=jar");
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void packagesSbomIntoBootJar(String gradleVersion) throws IOException {
        // given.
        setupProject();

        // when.
        BuildResult result =
                createRunner(gradleVersion, "bootJar", "--stacktrace").build();

        // then.
        assertThat(result.task(":bootJar").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(readFromJar()).contains("pkg:maven/org.slf4j/slf4j-api@2.0.9?type=jar");
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void omitsTheApplicationsOwnModulesButKeepsTheLibrariesTheyPullIn(String gradleVersion) throws IOException {
        // given. The app depends on its own sub-module, which is the only thing pulling in guava.
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\ninclude('sub')\n");
        writeFile(
                "build.gradle.kts",
                "plugins { id(\"com.axelixlabs.axelix\") }\n"
                        + "apply(plugin = \"java\")\n"
                        + "group = \"com.example\"\n"
                        + "version = \"1.2.3\"\n"
                        + "repositories { mavenCentral() }\n"
                        + "dependencies { \"implementation\"(project(\":sub\")) }\n");
        writeFile(
                "sub/build.gradle.kts",
                "plugins { `java-library` }\n"
                        + "repositories { mavenCentral() }\n"
                        + "dependencies { \"api\"(\"org.slf4j:slf4j-simple:2.0.9\") }\n");

        // when.
        BuildResult result =
                createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // then.
        assertThat(result.task(":" + GENERATE_TASK_NAME).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);

        String sbom = readGenerated();
        // The sub-module is the app's own code, not a third-party library: it must not appear.
        assertThat(sbom).doesNotContain("axelix-plugin-test-sub").doesNotContain("project :sub");
        // The library it pulled in must appear...
        assertThat(sbom).contains("pkg:maven/org.slf4j/slf4j-simple@2.0.9?type=jar");
        // ...collapsed onto the root, since the intermediate project node is transparent.
        int rootRef = sbom.indexOf("\"ref\" : \"pkg:maven/com.example/axelix-plugin-test@1.2.3");
        assertThat(rootRef).isGreaterThan(-1);
        String rootEntry = sbom.substring(rootRef, sbom.indexOf(']', rootRef));
        assertThat(rootEntry).contains("pkg:maven/org.slf4j/slf4j-simple@2.0.9?type=jar");
    }

    private void setupProject() throws IOException {
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        writeFile("build.gradle.kts", GradleProjectFixtures.loadContent("sbom/sbom.gradle.kts"));
    }

    private String readGenerated() throws IOException {
        Path sbom = projectDir.resolve("build/generated/axelix-sbom/" + SBOM_RESOURCE_PATH);
        assertThat(sbom).exists();
        return new String(Files.readAllBytes(sbom));
    }

    private String readFromJar() throws IOException {
        Path jar = projectDir.resolve("build/libs/axelix-plugin-test-1.2.3.jar");
        assertThat(jar).exists();
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry entry = zip.getEntry(SBOM_RESOURCE_PATH);
            assertThat(entry).isNotNull();
            try (InputStream in = zip.getInputStream(entry)) {
                return new String(in.readAllBytes());
            }
        }
    }
}
