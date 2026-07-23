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

import static com.axelixlabs.axelix.maven.plugin.AxelixLifecycleParticipant.PROFILER_DETECTED_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GenerateProjectInfoMojo}
 *
 * <p>Uses an isolated temp directory rather than the fixed fixtures under {@code
 * src/integrationTest}, because those live inside this very repository's git working tree: the
 * mojo would always find axelix's own {@code .git} instead of the scenario under test.
 *
 * @author Nikita Kirillov
 */
class GenerateProjectInfoMojoTest {

    private static final String POM =
            // language=xml
            """
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
                            <extensions>true</extensions>
                            <version>1.0.0-SNAPSHOT</version>
                        </plugin>
                    </plugins>
                </build>
            </project>
            """;

    @TempDir
    private Path projectDir;

    @Test
    void shouldGenerateBuildAndGitInfoTogether() throws VerificationException, IOException, InterruptedException {
        // given.
        writePom();
        initGitRepository();

        // when.
        Verifier verifier = new Verifier(projectDir.toString());
        verifier.executeGoal("install");
        verifier.verify(true);

        // then.
        assertGitBuildProperties(loadProperties(), true);
    }

    @Test
    void shouldGenerateBuildInfoOnlyWhenNotInsideAGitRepository() throws VerificationException, IOException {
        // given. no git repository initialized in the temp dir.
        writePom();

        // when.
        Verifier verifier = new Verifier(projectDir.toString());
        verifier.executeGoal("install");
        verifier.verify(true);

        // then.
        assertGitBuildProperties(loadProperties(), false);
    }

    private void writePom() throws IOException {
        Files.writeString(projectDir.resolve("pom.xml"), POM);
    }

    private Properties loadProperties() throws IOException {
        Path infoFile = projectDir.resolve("target/classes/META-INF/axelix-info.properties");
        assertThat(infoFile).exists();

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(infoFile)) {
            properties.load(inputStream);
        }
        return properties;
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

    private void assertGitBuildProperties(Properties properties, boolean hasGitInfo) {
        assertThat(properties.getProperty("build.group")).isEqualTo("com.example");
        assertThat(properties.getProperty("build.name")).isEqualTo("axelix-plugin-test");
        assertThat(properties.getProperty("build.version")).isEqualTo("1.2.3");
        assertThat(properties.getProperty("build.time")).isNotBlank();

        assertThat(properties.getProperty(PROFILER_DETECTED_PROPERTY)).isEqualTo("false");

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
