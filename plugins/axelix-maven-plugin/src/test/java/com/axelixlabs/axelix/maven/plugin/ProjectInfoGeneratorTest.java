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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static com.axelixlabs.axelix.maven.plugin.ProjectInfoGenerator.AXELIX_INFO_PROPERTIES_LOCATION;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@code axelix-info.properties} half of the umbrella {@code axelix-generate-project-info}
 * goal, exercised through {@link ProjectInfoGenerator}.
 *
 * @author Nikita Kirillov
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 */
class ProjectInfoGeneratorTest {

    private static Properties loadProperties(Path propertiesFile) throws IOException {
        assertThat(propertiesFile).exists();

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(propertiesFile)) {
            properties.load(inputStream);
        }
        return properties;
    }

    // language=xml
    private static final String POM_CONTENT = """
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>

                <groupId>com.example</groupId>
                <artifactId>axelix-plugin-test</artifactId>
                <version>1.2.3</version>

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
            """.formatted(MavenTestUtils.getLocallyPublishedPluginVersion());

    @TempDir
    private Path projectDir;

    @Test
    void shouldGenerateBuildAndGitInfoTogether() throws VerificationException, IOException, InterruptedException {
        // given.
        writePom(POM_CONTENT);
        initGitRepository();

        // when.
        Verifier verifier = new Verifier(projectDir.toString());
        verifier.executeGoal("install");
        verifier.verify(true);

        // then.
        Properties axelixInfoProperties = loadProperties(infoFile());

        assertBuildProperties(axelixInfoProperties);
        assertGitPropertiesArePresent(axelixInfoProperties);
    }

    @Test
    void shouldGenerateBuildInfoOnlyWhenNotInsideAGitRepository() throws VerificationException, IOException {
        // given. no git repository initialized in the temp dir.
        writePom(POM_CONTENT);

        // when.
        Verifier verifier = new Verifier(projectDir.toString());
        verifier.executeGoal("install");
        verifier.verify(true);

        // then.
        Properties axelixInfoProperties = loadProperties(infoFile());

        assertBuildProperties(axelixInfoProperties);
        assertGitPropertiesAreAbsent(axelixInfoProperties);
    }

    private Path infoFile() {
        return projectDir.resolve("target/classes/" + AXELIX_INFO_PROPERTIES_LOCATION);
    }

    private void writePom(String content) throws IOException {
        Files.write(projectDir.resolve("pom.xml"), content.getBytes(StandardCharsets.UTF_8));
    }

    private void initGitRepository() throws IOException, InterruptedException {
        runGit("init");
        runGit("config", "commit.gpgsign", "false");
        runGit("config", "user.email", "test@example.com");
        runGit("config", "user.name", "Test User");
        Files.writeString(projectDir.resolve("README.md"), "test\n");
        runGit("add", ".");
        runGit("commit", "-m", "initial commit");
    }

    private void runGit(String... args) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("git");
        command.addAll(Arrays.asList(args));
        Process process =
                new ProcessBuilder(command).directory(projectDir.toFile()).start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String output = new String(process.getInputStream().readAllBytes());
            throw new IllegalStateException("Git command failed with exit code " + exitCode + ": " + output);
        }
    }

    private void assertBuildProperties(Properties properties) {
        assertThat(properties.getProperty("build.group")).isEqualTo("com.example");
        assertThat(properties.getProperty("build.name")).isEqualTo("axelix-plugin-test");
        assertThat(properties.getProperty("build.version")).isEqualTo("1.2.3");
        assertThat(properties.getProperty("build.time")).isNotBlank();
    }

    private void assertGitPropertiesArePresent(Properties properties) {
        assertThat(properties.getProperty("git.commit.id")).hasSize(40);
        assertThat(properties.getProperty("git.commit.id.abbrev")).hasSize(7);
        assertThat(properties.getProperty("git.branch")).isNotBlank();
        assertThat(properties.getProperty("git.commit.user.name")).isEqualTo("Test User");
        assertThat(properties.getProperty("git.commit.user.email")).isEqualTo("test@example.com");
        assertThat(properties.getProperty("git.commit.time")).isNotBlank();
    }

    private void assertGitPropertiesAreAbsent(Properties properties) {
        assertThat(properties.getProperty("git.commit.id")).isNull();
        assertThat(properties.getProperty("git.commit.id.abbrev")).isNull();
        assertThat(properties.getProperty("git.branch")).isNull();
        assertThat(properties.getProperty("git.commit.user.name")).isNull();
        assertThat(properties.getProperty("git.commit.user.email")).isNull();
        assertThat(properties.getProperty("git.commit.time")).isNull();
    }
}
