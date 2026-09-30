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
package com.axelixlabs.axelix.maven.plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GenerateDependenciesSbomMojo}.
 *
 * @author Mikhail Polivakha
 */
class GenerateDependenciesSbomMojoTest {

    /**
     * The version of the plugin the enclosing Gradle build just published to the local repository,
     * so the fixture poms always exercise the workspace code rather than a stale release.
     */
    private static final String PLUGIN_VERSION = System.getProperty("axelix.plugin.version");

    @TempDir
    private Path projectDir;

    @Test
    void shouldGenerateSbomOfTheRuntimeDependencyGraphAndPackageIt() throws VerificationException, IOException {
        // given.
        writePom(pomWith("""
                <dependencies>
                    <dependency>
                        <groupId>org.slf4j</groupId>
                        <artifactId>slf4j-simple</artifactId>
                        <version>2.0.9</version>
                    </dependency>
                </dependencies>
                """));

        // when.
        runPackage();

        // then. the complete document: the application in the metadata, the direct and the
        // transitive dependency as components, and the resolution path root -> slf4j-simple ->
        // slf4j-api in the dependency graph, so Master can rebuild it.
        assertThatJson(readGenerated()).isEqualTo(sbomOfTheSlf4jSimpleGraph());

        // ...and the packaged jar carries it.
        assertSBOMActuallyGetPackaged();
    }

    @Test
    void shouldExcludeScopesThatDoNotShip() throws VerificationException, IOException {
        // given. only slf4j-api ships: commons-lang3 is provided by the container and opentest4j
        // is test-only.
        writePom(pomWith("""
                <dependencies>
                    <dependency>
                        <groupId>org.slf4j</groupId>
                        <artifactId>slf4j-api</artifactId>
                        <version>2.0.9</version>
                        <scope>runtime</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.apache.commons</groupId>
                        <artifactId>commons-lang3</artifactId>
                        <version>3.14.0</version>
                        <scope>provided</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.opentest4j</groupId>
                        <artifactId>opentest4j</artifactId>
                        <version>1.3.0</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
                """));

        // when.
        runPackage();

        // then. the graph is slf4j-api alone.
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
                """);

        // ...and the packaged jar carries it.
        assertSBOMActuallyGetPackaged();
    }

    @Test
    void shouldRecordOnlyTheResolvedVersionWhenVersionsConflict() throws VerificationException, IOException {
        // given. jcl-over-slf4j pulls slf4j-api:1.7.36, the direct dependency asks for 2.0.9;
        // Maven's nearest-wins mediation selects 2.0.9.
        writePom(pomWith("""
                <dependencies>
                    <dependency>
                        <groupId>org.slf4j</groupId>
                        <artifactId>jcl-over-slf4j</artifactId>
                        <version>1.7.36</version>
                    </dependency>
                    <dependency>
                        <groupId>org.slf4j</groupId>
                        <artifactId>slf4j-api</artifactId>
                        <version>2.0.9</version>
                    </dependency>
                </dependencies>
                """));

        // when.
        runPackage();

        // then. only the selected version (2.0.9) appears. Unlike Gradle, Maven's mediated tree
        // prunes the losing branch entirely, so jcl-over-slf4j carries no dependsOn edge at all -
        // the same shape `mvn dependency:tree` prints.
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
                    }
                  ]
                }
                """);

        // ...and the packaged jar carries it.
        assertSBOMActuallyGetPackaged();
    }

    @Test
    void shouldApplyVersionsPinnedByAnImportedBom() throws VerificationException, IOException {
        // given. junit-bom supplies the version junit-jupiter-api is resolved with. Unlike Gradle,
        // an import-scoped BOM is folded in at model-building time, so it never appears in the
        // graph itself.
        writePom(pomWith("""
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>org.junit</groupId>
                            <artifactId>junit-bom</artifactId>
                            <version>5.10.0</version>
                            <type>pom</type>
                            <scope>import</scope>
                        </dependency>
                    </dependencies>
                </dependencyManagement>

                <dependencies>
                    <dependency>
                        <groupId>org.junit.jupiter</groupId>
                        <artifactId>junit-jupiter-api</artifactId>
                    </dependency>
                </dependencies>
                """));

        // when.
        runPackage();

        // then. the pinned versions are what ships, and junit-bom itself is nowhere in the
        // document.
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
                    },
                    {
                      "type": "library",
                      "bom-ref": "pkg:maven/org.apiguardian/apiguardian-api@1.1.2?type=jar",
                      "group": "org.apiguardian",
                      "name": "apiguardian-api",
                      "version": "1.1.2",
                      "purl": "pkg:maven/org.apiguardian/apiguardian-api@1.1.2?type=jar"
                    }
                  ],
                  "dependencies": [
                    {
                      "ref": "pkg:maven/com.example/axelix-plugin-test@1.2.3?type=jar",
                      "dependsOn": [
                        "pkg:maven/org.junit.jupiter/junit-jupiter-api@5.10.0?type=jar"
                      ]
                    },
                    {
                      "ref": "pkg:maven/org.junit.jupiter/junit-jupiter-api@5.10.0?type=jar",
                      "dependsOn": [
                        "pkg:maven/org.opentest4j/opentest4j@1.3.0?type=jar",
                        "pkg:maven/org.junit.platform/junit-platform-commons@1.10.0?type=jar",
                        "pkg:maven/org.apiguardian/apiguardian-api@1.1.2?type=jar"
                      ]
                    }
                  ]
                }
                """);

        // ...and the packaged jar carries it.
        assertSBOMActuallyGetPackaged();
    }

    @Test
    void shouldCollapseReactorModulesOntoTheApplication() throws VerificationException, IOException {
        // given. a reactor where the app depends on its own 'lib' module, which is the only thing
        // pulling in slf4j-simple.
        writeFile("pom.xml", """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>

                    <groupId>com.example</groupId>
                    <artifactId>axelix-plugin-reactor</artifactId>
                    <version>1.2.3</version>
                    <packaging>pom</packaging>

                    <modules>
                        <module>lib</module>
                        <module>app</module>
                    </modules>
                </project>
                """);
        writeFile("lib/pom.xml", """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>

                    <parent>
                        <groupId>com.example</groupId>
                        <artifactId>axelix-plugin-reactor</artifactId>
                        <version>1.2.3</version>
                    </parent>
                    <artifactId>lib</artifactId>

                    <dependencies>
                        <dependency>
                            <groupId>org.slf4j</groupId>
                            <artifactId>slf4j-simple</artifactId>
                            <version>2.0.9</version>
                        </dependency>
                    </dependencies>
                </project>
                """);
        writeFile("app/pom.xml", """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>

                    <parent>
                        <groupId>com.example</groupId>
                        <artifactId>axelix-plugin-reactor</artifactId>
                        <version>1.2.3</version>
                    </parent>
                    <artifactId>axelix-plugin-test</artifactId>

                    <dependencies>
                        <dependency>
                            <groupId>com.example</groupId>
                            <artifactId>lib</artifactId>
                            <version>1.2.3</version>
                        </dependency>
                    </dependencies>

                    <build>
                        <plugins>
                            <plugin>
                                <groupId>com.axelixlabs</groupId>
                                <artifactId>axelix-maven-plugin</artifactId>
                                <version>%s</version>
                                <executions>
                                    <execution>
                                        <goals>
                                            <goal>axelix-generate-dependencies-sbom</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """.formatted(PLUGIN_VERSION));

        // when.
        runPackage();

        // then. the document is indistinguishable from the app declaring slf4j-simple directly:
        // the 'lib' module never appears, and the library it pulled in is attributed to the app.
        assertThatJson(readGeneratedIn("app")).isEqualTo(sbomOfTheSlf4jSimpleGraph());
    }

    @Test
    void shouldGenerateValidSbomWithoutDependencies() throws VerificationException, IOException {
        // given.
        writePom(pomWith(""));

        // when.
        runPackage();

        // then. still a valid document: the application metadata is there, just no graph. Note
        // that cyclonedx-core-java drops the empty components list entirely while keeping the
        // empty dependencies one - Master's parser must accept the components key being absent.
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

    private static String pomWith(String dependenciesBlock) {
        return """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>

                    <groupId>com.example</groupId>
                    <artifactId>axelix-plugin-test</artifactId>
                    <version>1.2.3</version>

                    %s

                    <build>
                        <plugins>
                            <plugin>
                                <groupId>com.axelixlabs</groupId>
                                <artifactId>axelix-maven-plugin</artifactId>
                                <version>%s</version>
                                <executions>
                                    <execution>
                                        <goals>
                                            <goal>axelix-generate-dependencies-sbom</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """.formatted(dependenciesBlock, PLUGIN_VERSION);
    }

    /**
     * The complete SBOM of an application whose runtime graph is {@code app -> slf4j-simple ->
     * slf4j-api}: the single-module fixture, and the exact document the reactor scenario must
     * collapse onto.
     */
    private static String sbomOfTheSlf4jSimpleGraph() {
        return """
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
                      "bom-ref": "pkg:maven/org.slf4j/slf4j-simple@2.0.9?type=jar",
                      "group": "org.slf4j",
                      "name": "slf4j-simple",
                      "version": "2.0.9",
                      "purl": "pkg:maven/org.slf4j/slf4j-simple@2.0.9?type=jar"
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
                        "pkg:maven/org.slf4j/slf4j-simple@2.0.9?type=jar"
                      ]
                    },
                    {
                      "ref": "pkg:maven/org.slf4j/slf4j-simple@2.0.9?type=jar",
                      "dependsOn": [
                        "pkg:maven/org.slf4j/slf4j-api@2.0.9?type=jar"
                      ]
                    }
                  ]
                }
                """;
    }

    private void assertSBOMActuallyGetPackaged() throws IOException {
        try (ZipFile jar = new ZipFile(
                projectDir.resolve("target/axelix-plugin-test-1.2.3.jar").toFile())) {
            assertThat(jar.getEntry(GenerateDependenciesSbomMojo.SBOM_RESOURCE_PATH))
                    .isNotNull();
        }
    }

    private void runPackage() throws VerificationException {
        Verifier verifier = new Verifier(projectDir.toString());
        verifier.executeGoal("package");
        verifier.verify(true);
    }

    private void writePom(String content) throws IOException {
        writeFile("pom.xml", content);
    }

    private void writeFile(String relativePath, String content) throws IOException {
        Path file = projectDir.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }

    private String readGenerated() throws IOException {
        return readGeneratedIn("");
    }

    private String readGeneratedIn(String module) throws IOException {
        Path sbom = projectDir
                .resolve(module)
                .resolve("target/classes")
                .resolve(GenerateDependenciesSbomMojo.SBOM_RESOURCE_PATH);
        assertThat(sbom).exists();
        return new String(Files.readAllBytes(sbom), StandardCharsets.UTF_8);
    }
}
