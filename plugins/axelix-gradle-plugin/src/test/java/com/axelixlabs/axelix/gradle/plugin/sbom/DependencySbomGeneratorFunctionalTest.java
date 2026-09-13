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

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.axelixlabs.axelix.gradle.plugin.AbstractAxelixPluginFunctionalTest;
import com.axelixlabs.axelix.gradle.plugin.GradleProjectFixtures;

import static com.axelixlabs.axelix.gradle.plugin.sbom.DependencySbomGenerator.GENERATE_TASK_NAME;
import static com.axelixlabs.axelix.gradle.plugin.sbom.DependencySbomGenerator.SBOM_RESOURCE_PATH;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
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

        // then. the complete document: the application in the metadata, the direct and the
        // transitive dependency as components, and the resolution path root -> slf4j-simple ->
        // slf4j-api in the dependency graph, so Master can rebuild it.
        assertThat(result.task(":" + GENERATE_TASK_NAME).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThatJson(readGenerated()).isEqualTo(sbomOfTheSlf4jSimpleGraph("axelix-plugin-test", "2.0.9"));
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void packagesSbomIntoBootJar(String gradleVersion) throws IOException {
        // given.
        setupProject();

        // when.
        BuildResult result =
                createRunner(gradleVersion, "bootJar", "--stacktrace").build();

        // then. the archive carries the very same document the generate task produced.
        assertThat(result.task(":bootJar").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThatJson(readFromJar()).isEqualTo(sbomOfTheSlf4jSimpleGraph("axelix-plugin-test", "2.0.9"));
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void omitsTheApplicationsOwnModulesButKeepsTheLibrariesTheyPullIn(String gradleVersion) throws IOException {
        // given. The app depends on its own sub-module, which is the only thing pulling in guava.
        writeFile("settings.gradle", """
                rootProject.name = 'axelix-plugin-test'
                include('sub')
                """);
        writeFile("build.gradle.kts", """
                plugins { id("com.axelixlabs.axelix") }
                apply(plugin = "java")
                group = "com.example"
                version = "1.2.3"
                repositories { mavenCentral() }
                dependencies { "implementation"(project(":sub")) }
                """);
        writeFile("sub/build.gradle.kts", """
                plugins { `java-library` }
                repositories { mavenCentral() }
                dependencies { "api"("org.slf4j:slf4j-simple:2.0.9") }
                """);

        // when.
        BuildResult result =
                createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // then. the document is indistinguishable from declaring slf4j-simple directly: the
        // sub-module never appears, and the library it pulled in is attributed to the root, since
        // the intermediate project node is transparent.
        assertThat(result.task(":" + GENERATE_TASK_NAME).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThatJson(readGenerated()).isEqualTo(sbomOfTheSlf4jSimpleGraph("axelix-plugin-test", "2.0.9"));
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void generatesASeparateSbomPerSubprojectApplyingThePlugin(String gradleVersion) throws IOException {
        // given. Two independent applications living in one multi-project build, each with its own
        // dependencies - the everyday shape of a real user's repository.
        writeFile("settings.gradle", """
                rootProject.name = 'axelix-plugin-test'
                include('app-a')
                include('app-b')
                """);
        writeFile("build.gradle.kts", """
                plugins { id("com.axelixlabs.axelix") apply false }
                subprojects {
                    apply(plugin = "java")
                    apply(plugin = "com.axelixlabs.axelix")
                    group = "com.example"
                    version = "1.2.3"
                    repositories { mavenCentral() }
                }
                """);
        writeFile("app-a/build.gradle.kts", """
                dependencies { "implementation"("org.slf4j:slf4j-simple:2.0.9") }
                """);
        writeFile("app-b/build.gradle.kts", """
                dependencies { "implementation"("org.apache.commons:commons-lang3:3.14.0") }
                """);

        // when.
        BuildResult result =
                createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // then. each subproject generated its own SBOM, describing only its own runtime classpath,
        // with no leakage from the sibling.
        assertThat(result.task(":app-a:" + GENERATE_TASK_NAME).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.task(":app-b:" + GENERATE_TASK_NAME).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);

        assertThatJson(readGeneratedIn("app-a")).isEqualTo(sbomOfTheSlf4jSimpleGraph("app-a", "2.0.9"));
        assertThatJson(readGeneratedIn("app-b")).isEqualTo("""
                {
                  "bomFormat": "CycloneDX",
                  "specVersion": "1.6",
                  "version": 1,
                  "metadata": {
                    "timestamp": "${json-unit.any-string}",
                    "component": {
                      "type": "application",
                      "bom-ref": "pkg:maven/com.example/app-b@1.2.3?type=jar",
                      "group": "com.example",
                      "name": "app-b",
                      "version": "1.2.3",
                      "purl": "pkg:maven/com.example/app-b@1.2.3?type=jar"
                    }
                  },
                  "components": [
                    {
                      "type": "library",
                      "bom-ref": "pkg:maven/org.apache.commons/commons-lang3@3.14.0?type=jar",
                      "group": "org.apache.commons",
                      "name": "commons-lang3",
                      "version": "3.14.0",
                      "purl": "pkg:maven/org.apache.commons/commons-lang3@3.14.0?type=jar"
                    }
                  ],
                  "dependencies": [
                    {
                      "ref": "pkg:maven/com.example/app-b@1.2.3?type=jar",
                      "dependsOn": [
                        "pkg:maven/org.apache.commons/commons-lang3@3.14.0?type=jar"
                      ]
                    }
                  ]
                }
                """);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void collapsesChainsOfProjectModulesOntoTheNearestLibraries(String gradleVersion) throws IOException {
        // given. Two project nodes between the app and the library: app -> :web -> :core -> slf4j.
        writeFile("settings.gradle", """
                rootProject.name = 'axelix-plugin-test'
                include('web')
                include('core')
                """);
        writeFile("build.gradle.kts", """
                plugins {
                    java
                    id("com.axelixlabs.axelix")
                }
                group = "com.example"
                version = "1.2.3"
                repositories { mavenCentral() }
                dependencies { implementation(project(":web")) }
                """);
        writeFile("web/build.gradle.kts", """
                plugins { `java-library` }
                repositories { mavenCentral() }
                dependencies { api(project(":core")) }
                """);
        writeFile("core/build.gradle.kts", """
                plugins { `java-library` }
                repositories { mavenCentral() }
                dependencies { api("org.slf4j:slf4j-simple:2.0.9") }
                """);

        // when.
        createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // then. neither project node appears, and the library two hops away is attributed straight
        // to the root application: the document is the same as if it declared slf4j-simple itself.
        assertThatJson(readGenerated()).isEqualTo(sbomOfTheSlf4jSimpleGraph("axelix-plugin-test", "2.0.9"));
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void keepsALibraryReachableThroughTwoProjectModulesOnlyOnce(String gradleVersion) throws IOException {
        // given. A diamond through project nodes: app -> :a -> slf4j-api and app -> :b -> slf4j-api.
        writeFile("settings.gradle", """
                rootProject.name = 'axelix-plugin-test'
                include('a')
                include('b')
                """);
        writeFile("build.gradle.kts", """
                plugins {
                    java
                    id("com.axelixlabs.axelix")
                }
                group = "com.example"
                version = "1.2.3"
                repositories { mavenCentral() }
                dependencies {
                    implementation(project(":a"))
                    implementation(project(":b"))
                }
                """);
        String library = """
                plugins { `java-library` }
                repositories { mavenCentral() }
                dependencies { api("org.slf4j:slf4j-api:2.0.9") }
                """;
        writeFile("a/build.gradle.kts", library);
        writeFile("b/build.gradle.kts", library);

        // when.
        createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // then. one component entry, and one dependsOn edge from the root - not one per path.
        assertThatJson(readGenerated()).isEqualTo(SBOM_WITH_SLF4J_API_ONLY);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void createsTheTaskOnlyInProjectsThatApplyThePlugin(String gradleVersion) throws IOException {
        // given. only the root applies the plugin; the sub-module does not.
        writeFile("settings.gradle", """
                rootProject.name = 'axelix-plugin-test'
                include('sub')
                """);
        writeFile("build.gradle.kts", """
                plugins {
                    java
                    id("com.axelixlabs.axelix")
                }
                group = "com.example"
                version = "1.2.3"
                repositories { mavenCentral() }
                """);
        writeFile("sub/build.gradle.kts", """
                plugins { `java-library` }
                repositories { mavenCentral() }
                """);

        // when.
        BuildResult result =
                createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // then. the root generated its SBOM; the sub-module has no such task at all.
        assertThat(result.task(":" + GENERATE_TASK_NAME).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.task(":sub:" + GENERATE_TASK_NAME)).isNull();
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void recordsOnlyTheResolvedVersionWhenVersionsConflict(String gradleVersion) throws IOException {
        // given. jcl-over-slf4j pulls slf4j-api:1.7.36, the direct dependency asks for 2.0.9;
        // Gradle's conflict resolution selects 2.0.9.
        setupProject("""
                dependencies {
                    implementation("org.slf4j:jcl-over-slf4j:1.7.36")
                    implementation("org.slf4j:slf4j-api:2.0.9")
                }
                """);

        // when.
        createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // then. only the selected version (2.0.9) appears as a component, and the edge from the
        // module that asked for the losing 1.7.36 points at the winner.
        assertThatJson(readGenerated()).isEqualTo("""
                {
                  "bomFormat": "CycloneDX",
                  "specVersion": "1.6",
                  "version": 1,
                  "metadata": {
                    "timestamp": "${json-unit.any-string}",
                    "component": {
                      "type": "application",
                      "bom-ref": "pkg:maven/com.example/axelix-plugin-test@1.2.3?type=jar",
                      "group": "com.example",
                      "name": "axelix-plugin-test",
                      "version": "1.2.3",
                      "purl": "pkg:maven/com.example/axelix-plugin-test@1.2.3?type=jar"
                    }
                  },
                  "components": [
                    {
                      "type": "library",
                      "bom-ref": "pkg:maven/org.slf4j/jcl-over-slf4j@1.7.36?type=jar",
                      "group": "org.slf4j",
                      "name": "jcl-over-slf4j",
                      "version": "1.7.36",
                      "purl": "pkg:maven/org.slf4j/jcl-over-slf4j@1.7.36?type=jar"
                    },
                    {
                      "type": "library",
                      "bom-ref": "pkg:maven/org.slf4j/slf4j-api@2.0.9?type=jar",
                      "group": "org.slf4j",
                      "name": "slf4j-api",
                      "version": "2.0.9",
                      "purl": "pkg:maven/org.slf4j/slf4j-api@2.0.9?type=jar"
                    }
                  ],
                  "dependencies": [
                    {
                      "ref": "pkg:maven/com.example/axelix-plugin-test@1.2.3?type=jar",
                      "dependsOn": [
                        "pkg:maven/org.slf4j/jcl-over-slf4j@1.7.36?type=jar",
                        "pkg:maven/org.slf4j/slf4j-api@2.0.9?type=jar"
                      ]
                    },
                    {
                      "ref": "pkg:maven/org.slf4j/jcl-over-slf4j@1.7.36?type=jar",
                      "dependsOn": [
                        "pkg:maven/org.slf4j/slf4j-api@2.0.9?type=jar"
                      ]
                    }
                  ]
                }
                """);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void appliesBomPinnedVersionsWithoutEmittingConstraintEdges(String gradleVersion) throws IOException {
        // given. the BOM supplies the version junit-jupiter-api is resolved with.
        setupProject("""
                dependencies {
                    implementation(platform("org.junit:junit-bom:5.10.0"))
                    implementation("org.junit.jupiter:junit-jupiter-api")
                }
                """);

        // when.
        createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // then. the pinned version is what ships, and modules the BOM merely pins but nothing
        // depends on (junit-jupiter-engine and the rest) are not dragged in. The junit-bom edges
        // below are NOT constraint leaks: JUnit's own module metadata declares a real platform
        // dependency on the BOM, so the BOM appears as a component with incoming edges - but,
        // constraint edges being skipped, never with outgoing ones.
        assertThatJson(readGenerated()).isEqualTo("""
                {
                  "bomFormat": "CycloneDX",
                  "specVersion": "1.6",
                  "version": 1,
                  "metadata": {
                    "timestamp": "${json-unit.any-string}",
                    "component": {
                      "type": "application",
                      "bom-ref": "pkg:maven/com.example/axelix-plugin-test@1.2.3?type=jar",
                      "group": "com.example",
                      "name": "axelix-plugin-test",
                      "version": "1.2.3",
                      "purl": "pkg:maven/com.example/axelix-plugin-test@1.2.3?type=jar"
                    }
                  },
                  "components": [
                    {
                      "type": "library",
                      "bom-ref": "pkg:maven/org.junit/junit-bom@5.10.0?type=jar",
                      "group": "org.junit",
                      "name": "junit-bom",
                      "version": "5.10.0",
                      "purl": "pkg:maven/org.junit/junit-bom@5.10.0?type=jar"
                    },
                    {
                      "type": "library",
                      "bom-ref": "pkg:maven/org.junit.jupiter/junit-jupiter-api@5.10.0?type=jar",
                      "group": "org.junit.jupiter",
                      "name": "junit-jupiter-api",
                      "version": "5.10.0",
                      "purl": "pkg:maven/org.junit.jupiter/junit-jupiter-api@5.10.0?type=jar"
                    },
                    {
                      "type": "library",
                      "bom-ref": "pkg:maven/org.opentest4j/opentest4j@1.3.0?type=jar",
                      "group": "org.opentest4j",
                      "name": "opentest4j",
                      "version": "1.3.0",
                      "purl": "pkg:maven/org.opentest4j/opentest4j@1.3.0?type=jar"
                    },
                    {
                      "type": "library",
                      "bom-ref": "pkg:maven/org.junit.platform/junit-platform-commons@1.10.0?type=jar",
                      "group": "org.junit.platform",
                      "name": "junit-platform-commons",
                      "version": "1.10.0",
                      "purl": "pkg:maven/org.junit.platform/junit-platform-commons@1.10.0?type=jar"
                    }
                  ],
                  "dependencies": [
                    {
                      "ref": "pkg:maven/com.example/axelix-plugin-test@1.2.3?type=jar",
                      "dependsOn": [
                        "pkg:maven/org.junit/junit-bom@5.10.0?type=jar",
                        "pkg:maven/org.junit.jupiter/junit-jupiter-api@5.10.0?type=jar"
                      ]
                    },
                    {
                      "ref": "pkg:maven/org.junit.jupiter/junit-jupiter-api@5.10.0?type=jar",
                      "dependsOn": [
                        "pkg:maven/org.junit/junit-bom@5.10.0?type=jar",
                        "pkg:maven/org.opentest4j/opentest4j@1.3.0?type=jar",
                        "pkg:maven/org.junit.platform/junit-platform-commons@1.10.0?type=jar"
                      ]
                    },
                    {
                      "ref": "pkg:maven/org.junit.platform/junit-platform-commons@1.10.0?type=jar",
                      "dependsOn": [
                        "pkg:maven/org.junit/junit-bom@5.10.0?type=jar"
                      ]
                    }
                  ]
                }
                """);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void excludesCompileOnlyButKeepsRuntimeOnlyDependencies(String gradleVersion) throws IOException {
        // given. commons-lang3 is compile-time only and never ships; slf4j-api ships without being
        // compiled against.
        setupProject("""
                dependencies {
                    compileOnly("org.apache.commons:commons-lang3:3.14.0")
                    runtimeOnly("org.slf4j:slf4j-api:2.0.9")
                }
                """);

        // when.
        createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // then. commons-lang3 is nowhere in the document; the graph is slf4j-api alone.
        assertThatJson(readGenerated()).isEqualTo(SBOM_WITH_SLF4J_API_ONLY);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void generatesAValidSbomForAProjectWithoutDependencies(String gradleVersion) throws IOException {
        // given.
        setupProject("");

        // when.
        BuildResult result =
                createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // then. still a valid document: the application metadata is there, just no graph. Note
        // that cyclonedx-core-java drops the empty components list entirely while keeping the
        // empty dependencies one - Master's parser must accept the components key being absent.
        assertThat(result.task(":" + GENERATE_TASK_NAME).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThatJson(readGenerated()).isEqualTo("""
                {
                  "bomFormat": "CycloneDX",
                  "specVersion": "1.6",
                  "version": 1,
                  "metadata": {
                    "timestamp": "${json-unit.any-string}",
                    "component": {
                      "type": "application",
                      "bom-ref": "pkg:maven/com.example/axelix-plugin-test@1.2.3?type=jar",
                      "group": "com.example",
                      "name": "axelix-plugin-test",
                      "version": "1.2.3",
                      "purl": "pkg:maven/com.example/axelix-plugin-test@1.2.3?type=jar"
                    }
                  },
                  "dependencies": []
                }
                """);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void recordsTheConcreteResolvedVersionForDynamicVersionDeclarations(String gradleVersion) throws IOException {
        // given.
        setupProject("""
                dependencies { implementation("org.slf4j:slf4j-api:2.0.+") }
                """);

        // when.
        createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // then. Master parses versions back out of the purls, so the declared range must never
        // leak into the document - only whatever concrete 2.0.x Gradle resolved, hence the regex
        // placeholders.
        assertThatJson(readGenerated()).isEqualTo("""
                {
                  "bomFormat": "CycloneDX",
                  "specVersion": "1.6",
                  "version": 1,
                  "metadata": {
                    "timestamp": "${json-unit.any-string}",
                    "component": {
                      "type": "application",
                      "bom-ref": "pkg:maven/com.example/axelix-plugin-test@1.2.3?type=jar",
                      "group": "com.example",
                      "name": "axelix-plugin-test",
                      "version": "1.2.3",
                      "purl": "pkg:maven/com.example/axelix-plugin-test@1.2.3?type=jar"
                    }
                  },
                  "components": [
                    {
                      "type": "library",
                      "bom-ref": "${json-unit.regex}pkg:maven/org[.]slf4j/slf4j-api@2[.]0[.][0-9]+[?]type=jar",
                      "group": "org.slf4j",
                      "name": "slf4j-api",
                      "version": "${json-unit.regex}2[.]0[.][0-9]+",
                      "purl": "${json-unit.regex}pkg:maven/org[.]slf4j/slf4j-api@2[.]0[.][0-9]+[?]type=jar"
                    }
                  ],
                  "dependencies": [
                    {
                      "ref": "pkg:maven/com.example/axelix-plugin-test@1.2.3?type=jar",
                      "dependsOn": [
                        "${json-unit.regex}pkg:maven/org[.]slf4j/slf4j-api@2[.]0[.][0-9]+[?]type=jar"
                      ]
                    }
                  ]
                }
                """);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void generatesTheSbomEvenWithoutGroupAndVersionSet(String gradleVersion) throws IOException {
        // given. unlike the project-info task, the SBOM task has no coordinate validation.
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        writeFile("build.gradle.kts", """
                plugins {
                    java
                    id("com.axelixlabs.axelix")
                }
                repositories { mavenCentral() }
                dependencies { implementation("org.slf4j:slf4j-api:2.0.9") }
                """);

        // when.
        BuildResult result =
                createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // then. the application component falls back to Gradle's defaults: an empty group and the
        // 'unspecified' version placeholder.
        assertThat(result.task(":" + GENERATE_TASK_NAME).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThatJson(readGenerated()).isEqualTo("""
                {
                  "bomFormat": "CycloneDX",
                  "specVersion": "1.6",
                  "version": 1,
                  "metadata": {
                    "timestamp": "${json-unit.any-string}",
                    "component": {
                      "type": "application",
                      "bom-ref": "pkg:maven//axelix-plugin-test@unspecified?type=jar",
                      "group": "",
                      "name": "axelix-plugin-test",
                      "version": "unspecified",
                      "purl": "pkg:maven//axelix-plugin-test@unspecified?type=jar"
                    }
                  },
                  "components": [
                    {
                      "type": "library",
                      "bom-ref": "pkg:maven/org.slf4j/slf4j-api@2.0.9?type=jar",
                      "group": "org.slf4j",
                      "name": "slf4j-api",
                      "version": "2.0.9",
                      "purl": "pkg:maven/org.slf4j/slf4j-api@2.0.9?type=jar"
                    }
                  ],
                  "dependencies": [
                    {
                      "ref": "pkg:maven//axelix-plugin-test@unspecified?type=jar",
                      "dependsOn": [
                        "pkg:maven/org.slf4j/slf4j-api@2.0.9?type=jar"
                      ]
                    }
                  ]
                }
                """);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void staysUpToDateUntilTheResolvedGraphChanges(String gradleVersion) throws IOException {
        // given.
        setupProject();
        createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // when. nothing changed since the first run.
        BuildResult unchanged =
                createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // then.
        assertThat(unchanged.task(":" + GENERATE_TASK_NAME).getOutcome()).isEqualTo(TaskOutcome.UP_TO_DATE);

        // when. a dependency version is bumped.
        writeFile(
                "build.gradle.kts",
                GradleProjectFixtures.loadContent("sbom/sbom.gradle.kts").replace("2.0.9", "2.0.13"));
        BuildResult bumped =
                createRunner(gradleVersion, GENERATE_TASK_NAME, "--stacktrace").build();

        // then. the task re-ran and the SBOM describes the new graph, with no trace of 2.0.9.
        assertThat(bumped.task(":" + GENERATE_TASK_NAME).getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThatJson(readGenerated()).isEqualTo(sbomOfTheSlf4jSimpleGraph("axelix-plugin-test", "2.0.13"));
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void writesSbomIntoMainResourcesSoItIsOnTheClasspathWithoutPackaging(String gradleVersion) throws IOException {
        // given. consumers like 'test' or 'bootRun' run off build/resources/main directly, never
        // touching the packaged jar.
        setupProject();

        // when.
        BuildResult result =
                createRunner(gradleVersion, "processResources", "--stacktrace").build();

        // then. the copy on the classpath is the full generated document, not a stub.
        assertThat(result.task(":processResources").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        Path sbom = projectDir.resolve("build/resources/main/" + SBOM_RESOURCE_PATH);
        assertThat(sbom).exists();
        assertThatJson(new String(Files.readAllBytes(sbom)))
                .isEqualTo(sbomOfTheSlf4jSimpleGraph("axelix-plugin-test", "2.0.9"));
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void registersNoSbomTaskWithoutTheJavaPlugin(String gradleVersion) throws IOException {
        // given. no java plugin means no runtime classpath to describe.
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        writeFile("build.gradle.kts", """
                plugins { id("com.axelixlabs.axelix") }
                """);

        // when.
        BuildResult result =
                createRunner(gradleVersion, "tasks", "--all", "--stacktrace").build();

        // then. the build works, it just has nothing to generate.
        assertThat(result.task(":tasks").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput()).doesNotContain(GENERATE_TASK_NAME);
    }

    private void setupProject() throws IOException {
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        writeFile("build.gradle.kts", GradleProjectFixtures.loadContent("sbom/sbom.gradle.kts"));
    }

    /**
     * A single-project build like {@link #setupProject()}, but with the given {@code dependencies}
     * block instead of the fixture's.
     */
    private void setupProject(String dependenciesBlock) throws IOException {
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        writeFile("build.gradle.kts", """
                plugins {
                    java
                    id("com.axelixlabs.axelix")
                }
                group = "com.example"
                version = "1.2.3"
                repositories { mavenCentral() }
                """ + dependenciesBlock);
    }

    /**
     * The complete SBOM of an application whose runtime graph is {@code application ->
     * slf4j-simple -> slf4j-api}: what the single-project fixture produces, and the exact document
     * every multi-project scenario that routes slf4j-simple through intermediate project modules
     * must collapse onto.
     */
    private static String sbomOfTheSlf4jSimpleGraph(String application, String slf4jVersion) {
        return """
                {
                  "bomFormat": "CycloneDX",
                  "specVersion": "1.6",
                  "version": 1,
                  "metadata": {
                    "timestamp": "${json-unit.any-string}",
                    "component": {
                      "type": "application",
                      "bom-ref": "pkg:maven/com.example/{application}@1.2.3?type=jar",
                      "group": "com.example",
                      "name": "{application}",
                      "version": "1.2.3",
                      "purl": "pkg:maven/com.example/{application}@1.2.3?type=jar"
                    }
                  },
                  "components": [
                    {
                      "type": "library",
                      "bom-ref": "pkg:maven/org.slf4j/slf4j-simple@{version}?type=jar",
                      "group": "org.slf4j",
                      "name": "slf4j-simple",
                      "version": "{version}",
                      "purl": "pkg:maven/org.slf4j/slf4j-simple@{version}?type=jar"
                    },
                    {
                      "type": "library",
                      "bom-ref": "pkg:maven/org.slf4j/slf4j-api@{version}?type=jar",
                      "group": "org.slf4j",
                      "name": "slf4j-api",
                      "version": "{version}",
                      "purl": "pkg:maven/org.slf4j/slf4j-api@{version}?type=jar"
                    }
                  ],
                  "dependencies": [
                    {
                      "ref": "pkg:maven/com.example/{application}@1.2.3?type=jar",
                      "dependsOn": [
                        "pkg:maven/org.slf4j/slf4j-simple@{version}?type=jar"
                      ]
                    },
                    {
                      "ref": "pkg:maven/org.slf4j/slf4j-simple@{version}?type=jar",
                      "dependsOn": [
                        "pkg:maven/org.slf4j/slf4j-api@{version}?type=jar"
                      ]
                    }
                  ]
                }
                """.replace("{application}", application).replace("{version}", slf4jVersion);
    }

    /**
     * The complete SBOM of the fixture application when its runtime graph is exactly one library,
     * slf4j-api 2.0.9 - however many declaration paths led to it.
     */
    private static final String SBOM_WITH_SLF4J_API_ONLY = """
            {
              "bomFormat": "CycloneDX",
              "specVersion": "1.6",
              "version": 1,
              "metadata": {
                "timestamp": "${json-unit.any-string}",
                "component": {
                  "type": "application",
                  "bom-ref": "pkg:maven/com.example/axelix-plugin-test@1.2.3?type=jar",
                  "group": "com.example",
                  "name": "axelix-plugin-test",
                  "version": "1.2.3",
                  "purl": "pkg:maven/com.example/axelix-plugin-test@1.2.3?type=jar"
                }
              },
              "components": [
                {
                  "type": "library",
                  "bom-ref": "pkg:maven/org.slf4j/slf4j-api@2.0.9?type=jar",
                  "group": "org.slf4j",
                  "name": "slf4j-api",
                  "version": "2.0.9",
                  "purl": "pkg:maven/org.slf4j/slf4j-api@2.0.9?type=jar"
                }
              ],
              "dependencies": [
                {
                  "ref": "pkg:maven/com.example/axelix-plugin-test@1.2.3?type=jar",
                  "dependsOn": [
                    "pkg:maven/org.slf4j/slf4j-api@2.0.9?type=jar"
                  ]
                }
              ]
            }
            """;

    private String readGeneratedIn(String subproject) throws IOException {
        Path sbom = projectDir.resolve(subproject + "/build/generated/axelix-sbom/" + SBOM_RESOURCE_PATH);
        assertThat(sbom).exists();
        return new String(Files.readAllBytes(sbom));
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
