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

import static com.axelixlabs.axelix.maven.plugin.ProjectInfoGenerator.AXELIX_INFO_PROPERTIES_LOCATION;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the umbrella {@code axelix-generate-project-info} goal end to end: a single declared goal
 * must produce, and package, both resources Master needs. The content of each is covered in detail
 * by {@link ProjectInfoGeneratorTest} and {@link DependenciesSbomGeneratorTest} respectively; here
 * we only assert that the one goal wires both generators.
 *
 * @author Mikhail Polivakha
 */
class GenerateProjectInfoMojoTest {

    @TempDir
    private Path projectDir;

    @Test
    void shouldGenerateAndPackageBothProjectInfoAndDependenciesSbomFromASingleGoal()
            throws VerificationException, IOException {
        // given. a plain project declaring the single umbrella goal and one runtime dependency, so
        // the SBOM has something to describe.
        writePom("""
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>

                    <groupId>com.example</groupId>
                    <artifactId>axelix-plugin-test</artifactId>
                    <version>1.2.3</version>

                    <dependencies>
                        <dependency>
                            <groupId>org.slf4j</groupId>
                            <artifactId>slf4j-simple</artifactId>
                            <version>2.0.9</version>
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
                                            <goal>axelix-generate-project-info</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """.formatted(MavenTestUtils.getLocallyPublishedPluginVersion()));

        // when.
        runPackage();

        // then. both resources are written into the output directory...
        assertThat(projectDir.resolve("target/classes").resolve(AXELIX_INFO_PROPERTIES_LOCATION))
                .exists();
        assertThat(projectDir.resolve("target/classes").resolve(DependenciesSbomGenerator.SBOM_RESOURCE_PATH))
                .exists();

        // ...and both are carried by the packaged jar.
        try (ZipFile jar = new ZipFile(
                projectDir.resolve("target/axelix-plugin-test-1.2.3.jar").toFile())) {
            assertThat(jar.getEntry(AXELIX_INFO_PROPERTIES_LOCATION)).isNotNull();
            assertThat(jar.getEntry(DependenciesSbomGenerator.SBOM_RESOURCE_PATH))
                    .isNotNull();
        }
    }

    private void runPackage() throws VerificationException {
        Verifier verifier = new Verifier(projectDir.toString());
        verifier.executeGoal("package");
        verifier.verify(true);
    }

    private void writePom(String content) throws IOException {
        Files.write(projectDir.resolve("pom.xml"), content.getBytes(StandardCharsets.UTF_8));
    }
}
