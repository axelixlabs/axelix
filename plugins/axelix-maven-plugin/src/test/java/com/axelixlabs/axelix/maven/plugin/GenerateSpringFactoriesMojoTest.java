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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GenerateSpringFactoriesMojo}
 *
 * @author Artemiy Degtyarev
 */
class GenerateSpringFactoriesMojoTest {
    public static final String CURRENT_DIR = new File("").getAbsolutePath();
    public static final String SPRING_FACTORIES_PATH = "/META-INF/spring.factories";
    public static final String GENERATED_RESOURCES_PATH = "/target/generated-test-resources/axelix";

    private String baseDir;

    @AfterEach
    void tearDown() throws IOException {
        cleanupTestFolder(baseDir);
    }

    @Test
    void should_generate_new_spring_factories_if_not_exists() throws VerificationException {
        // given
        baseDir = CURRENT_DIR + "/src/integrationTest/generate-new-spring-factories";
        Verifier verifier = new Verifier(baseDir);

        // when
        verifier.executeGoal("test");
        verifier.verify(true);

        // then
        Path path = getGeneratedSpringFactoriesDirectory();
        assertThat(path).exists();

        Path finalResourcePath = getTestClasspathSpringFactoriesDirectory();
        assertThat(finalResourcePath).exists();
    }

    @Test
    void should_merge_spring_factories() throws VerificationException, IOException {
        // given
        baseDir = CURRENT_DIR + "/src/integrationTest/merge-spring-factories";
        Verifier verifier = new Verifier(baseDir);

        // when
        verifier.executeGoal("test");
        verifier.verify(true);

        // then
        Path generatedSpringFactoriesDirectory = getGeneratedSpringFactoriesDirectory();
        assertThat(generatedSpringFactoriesDirectory).exists();

        Properties generatedSpringFactories = new Properties();
        generatedSpringFactories.load(Files.newInputStream(generatedSpringFactoriesDirectory));

        String springFactoriesKey = "org.springframework.test.context.TestExecutionListener";
        String springFactoriesValue = "someTestClass,digital.pragmatech.testing.SpringTestProfilerListener";

        assertThat(generatedSpringFactories).containsEntry(springFactoriesKey, springFactoriesValue);

        Path testClasspathSpringFactoriesDirectory = getTestClasspathSpringFactoriesDirectory();
        assertThat(testClasspathSpringFactoriesDirectory).exists();

        Properties testClasspathSpringFactories = new Properties();
        testClasspathSpringFactories.load(Files.newInputStream(testClasspathSpringFactoriesDirectory));

        assertThat(testClasspathSpringFactories).containsEntry(springFactoriesKey, springFactoriesValue);
    }

    /**
     * Cleanup test result folder after tests pass
     * @param baseDir base tests directory
     * @throws IOException
     */
    private static void cleanupTestFolder(String baseDir) throws IOException {
        Path targetDir = Paths.get(baseDir + "/target");
        if (!Files.exists(targetDir)) {
            return;
        }

        try (Stream<Path> walk = Files.walk(targetDir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * Get path of {@code spring.factories} in test classpath
     * @return Path of {@code spring.factories} in test classpath
     */
    private Path getTestClasspathSpringFactoriesDirectory() {
        return Paths.get(baseDir, "/target/test-classes", SPRING_FACTORIES_PATH);
    }

    /**
     * Get path of generated {@code spring.factories}
     * @return Path of generated {@code spring.factories}
     */
    private Path getGeneratedSpringFactoriesDirectory() {
        return Paths.get(baseDir, GENERATED_RESOURCES_PATH, SPRING_FACTORIES_PATH);
    }
}
