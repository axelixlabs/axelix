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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.io.TempDir;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Shared TestKit scaffolding for the plugin's functional tests: an isolated project directory per
 * test, run against a chosen Gradle version.
 *
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
public abstract class AbstractAxelixPluginFunctionalTest {

    /**
     * Gradle versions exercised by the enclosing test task, supplied as a comma-separated list via the
     * {@code axelix.test.gradle.versions} system property. No single JVM can launch the whole supported
     * range (Gradle 5-7 require Java &lt;= 11, Gradle 8.10.2's bundled Groovy can't compile scripts on
     * JDKs newer than it supports, Gradle 9 requires Java &gt;= 17), so the {@code test}, {@code
     * gradle810Test} and {@code legacyGradleTest} tasks each pass the subset valid for their toolchain.
     */
    protected static List<String> gradleVersionsUnderTest() {
        String versions = System.getProperty("axelix.test.gradle.versions");

        return Arrays.stream(versions.split(","))
                .map(String::trim)
                .filter(version -> !version.isEmpty())
                .collect(Collectors.toList());
    }

    @TempDir
    protected Path projectDir;

    protected GradleRunner createRunner(String gradleVersion, String... arguments) {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withGradleVersion(gradleVersion)
                .withArguments(arguments);
    }

    protected void writeFile(String relativePath, String content) throws IOException {
        Path file = projectDir.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(UTF_8));
    }

    /**
     * Initializes a throwaway git repository in {@link #projectDir} with a single commit.
     */
    protected void initGitRepository() throws IOException, InterruptedException {
        runGit("init");
        runGit("config", "commit.gpgsign", "false");
        runGit("config", "user.email", "test@example.com");
        runGit("config", "user.name", "Test User");
        writeFile(".gitignore", "build/\n.gradle/\n");
        writeFile("README.md", "test\n");
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
            throw new IllegalStateException("Git command failed with exit code: " + exitCode);
        }
    }
}
