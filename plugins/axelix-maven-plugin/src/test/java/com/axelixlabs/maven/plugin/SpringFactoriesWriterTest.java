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
package com.axelixlabs.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class SpringFactoriesWriterTest {

    private static final String TEST_EXECUTION_LISTENER_KEY = "org.springframework.test.context.TestExecutionListener";
    private static final String CONTEXT_INITIALIZER_KEY = "org.springframework.context.ApplicationContextInitializer";
    private static final String PROFILER_LISTENER = "digital.pragmatech.testing.SpringTestProfilerListener";
    private static final String DIAGNOSTIC_INITIALIZER =
            "digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer";

    private final SpringFactoriesWriter subject = new SpringFactoriesWriter();

    @Nested
    class Greenfield {

        @Test
        void writesVerbatimContentAtMetaInfPath(@TempDir Path tempDir) throws IOException {
            // given
            File rootDir = tempDir.toFile();

            // when
            File written = subject.write(rootDir, List.of());

            // then
            assertThat(written).isEqualTo(new File(rootDir, "META-INF/spring.factories"));
            assertThat(contentOf(written))
                    .isEqualTo("org.springframework.test.context.TestExecutionListener=\\\n"
                            + "digital.pragmatech.testing.SpringTestProfilerListener\n"
                            + "org.springframework.context.ApplicationContextInitializer=\\\n"
                            + "digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer\n");
        }

        @Test
        void createsParentDirectoriesWhenAbsent(@TempDir Path tempDir) throws IOException {
            // given
            File rootDir = tempDir.resolve("nested/generated/axelix").toFile();

            // when
            File written = subject.write(rootDir, List.of());

            // then
            assertThat(written).exists();
        }
    }

    @Nested
    class Merge {

        @Test
        void unionsExistingDeclarationsWithAxelixEntries(@TempDir Path tempDir) throws IOException {
            // given
            File existing = writeExisting(
                    tempDir,
                    TEST_EXECUTION_LISTENER_KEY + "=com.app.AppListener\n"
                            + "org.springframework.boot.SpringApplicationRunListener=com.app.AppRunListener\n");

            // when
            File written = subject.write(tempDir.resolve("out").toFile(), List.of(existing));

            // then
            Properties merged = load(written);
            assertThat(valuesOf(merged, TEST_EXECUTION_LISTENER_KEY))
                    .containsExactlyInAnyOrder("com.app.AppListener", PROFILER_LISTENER);
            assertThat(valuesOf(merged, CONTEXT_INITIALIZER_KEY)).containsExactly(DIAGNOSTIC_INITIALIZER);
            assertThat(valuesOf(merged, "org.springframework.boot.SpringApplicationRunListener"))
                    .containsExactly("com.app.AppRunListener");
        }

        @Test
        void doesNotDuplicateAlreadyDeclaredAxelixEntry(@TempDir Path tempDir) throws IOException {
            // given
            File existing = writeExisting(tempDir, TEST_EXECUTION_LISTENER_KEY + "=" + PROFILER_LISTENER + "\n");

            // when
            File written = subject.write(tempDir.resolve("out").toFile(), List.of(existing));

            // then
            Properties merged = load(written);
            assertThat(valuesOf(merged, TEST_EXECUTION_LISTENER_KEY)).containsExactly(PROFILER_LISTENER);
        }

        private File writeExisting(Path tempDir, String content) throws IOException {
            Path file = tempDir.resolve("existing.factories");
            Files.write(file, content.getBytes(StandardCharsets.UTF_8));
            return file.toFile();
        }
    }

    private static String contentOf(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    private static Properties load(File file) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    private static List<String> valuesOf(Properties properties, String key) {
        return List.of(properties.getProperty(key, "").split(","));
    }
}
