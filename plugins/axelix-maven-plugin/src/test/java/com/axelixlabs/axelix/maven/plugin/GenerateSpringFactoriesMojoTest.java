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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
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
 * @author Mikhail Polivakha
 */
class GenerateSpringFactoriesMojoTest {

    private static final String CURRENT_DIR = new File("").getAbsolutePath();
    private static final String SPRING_FACTORIES_PATH = "META-INF/spring.factories";
    private static final String GENERATED_RESOURCES_PATH = "target/generated-test-resources/axelix";

    private static final String TEST_EXECUTION_LISTENER_KEY = "org.springframework.test.context.TestExecutionListener";
    private static final String APPLICATION_CONTEXT_INITIALIZER_KEY =
            "org.springframework.context.ApplicationContextInitializer";
    private static final String PROFILER_LISTENER = "digital.pragmatech.testing.SpringTestProfilerListener";
    private static final String CONTEXT_DIAGNOSTIC_INITIALIZER =
            "digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer";
    private static final String PREEXISTING_LISTENER = "someTestClass";

    private String baseDir;

    @AfterEach
    void tearDown() throws IOException {
        cleanupTestFolder(baseDir);
    }

    @Test
    void should_generate_new_spring_factories_if_not_exists() throws VerificationException, IOException {
        // given.
        baseDir = CURRENT_DIR + "/src/integrationTest/generate-new-spring-factories";
        Verifier verifier = new Verifier(baseDir);

        // when.
        verifier.executeGoal("test");
        verifier.verify(true);

        // then.
        Path testClasspathSpringFactories = getTestClasspathSpringFactoriesPath();
        assertThat(testClasspathSpringFactories).exists();
        Properties properties = loadProperties(testClasspathSpringFactories);

        assertContainsExactlyClasses(properties, TEST_EXECUTION_LISTENER_KEY, Set.of(PROFILER_LISTENER));
        assertContainsExactlyClasses(
                properties, APPLICATION_CONTEXT_INITIALIZER_KEY, Set.of(CONTEXT_DIAGNOSTIC_INITIALIZER));
    }

    @Test
    void should_merge_spring_factories() throws VerificationException, IOException {
        // given.
        baseDir = CURRENT_DIR + "/src/integrationTest/merge-spring-factories";
        Verifier verifier = new Verifier(baseDir);

        // when.
        verifier.executeGoal("test");
        verifier.verify(true);

        // then.
        Path testClasspathSpringFactories = getTestClasspathSpringFactoriesPath();
        assertThat(testClasspathSpringFactories).exists();
        Properties properties = loadProperties(testClasspathSpringFactories);

        assertContainsExactlyClasses(
                properties, TEST_EXECUTION_LISTENER_KEY, Set.of(PREEXISTING_LISTENER, PROFILER_LISTENER));
        assertContainsExactlyClasses(
                properties, APPLICATION_CONTEXT_INITIALIZER_KEY, Set.of(CONTEXT_DIAGNOSTIC_INITIALIZER));
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

    private Path getTestClasspathSpringFactoriesPath() {
        return Paths.get(baseDir, "target/test-classes", SPRING_FACTORIES_PATH);
    }

    private static Properties loadProperties(Path path) throws IOException {
        Properties properties = new Properties();
        properties.load(Files.newInputStream(path));
        return properties;
    }

    private static void assertContainsExactlyClasses(Properties properties, String key, Set<String> expectedClasses) {
        assertThat(properties).containsKey(key);
        Set<String> actualClasses = Arrays.stream(((String) properties.get(key)).split(","))
                .map(String::trim)
                .filter(className -> !className.isEmpty())
                .collect(Collectors.toSet());
        assertThat(actualClasses).containsExactlyInAnyOrderElementsOf(expectedClasses);
    }
}
