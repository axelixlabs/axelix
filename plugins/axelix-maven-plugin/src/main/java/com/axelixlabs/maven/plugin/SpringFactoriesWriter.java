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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Writes the {@code META-INF/spring.factories} that registers Axelix's spring-test-profiler listener
 * and diagnostic context initializer onto a directory destined for the test classpath.
 *
 * <p>When the project already provides its own {@code META-INF/spring.factories} on the test
 * classpath, those declarations are merged in (union per factory key, de-duplicated) so the existing
 * registrations survive alongside Axelix's.
 *
 * <p>This class deliberately depends only on the JDK (not on the plugin runtime), so it can be
 * exercised by plain unit tests.
 */
final class SpringFactoriesWriter {

    static final String RELATIVE_PATH = "META-INF/spring.factories";

    /**
     * The verbatim content written when the project does not already provide a
     * {@code META-INF/spring.factories}.
     */
    static final String SPRING_FACTORIES_CONTENT = "org.springframework.test.context.TestExecutionListener=\\\n"
            + "digital.pragmatech.testing.SpringTestProfilerListener\n"
            + "org.springframework.context.ApplicationContextInitializer=\\\n"
            + "digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer\n";

    /** The factory registrations Axelix contributes, in declaration order. */
    static final Map<String, String> REQUIRED_ENTRIES = requiredEntries();

    private static Map<String, String> requiredEntries() {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put(
                "org.springframework.test.context.TestExecutionListener",
                "digital.pragmatech.testing.SpringTestProfilerListener");
        entries.put(
                "org.springframework.context.ApplicationContextInitializer",
                "digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer");
        return entries;
    }

    /**
     * Writes {@code META-INF/spring.factories} under {@code rootDir}, creating parent directories as
     * needed. When {@code existing} is empty the verbatim {@link #SPRING_FACTORIES_CONTENT} is written;
     * otherwise the entries of the existing files are merged with Axelix's. Returns the written file.
     */
    File write(File rootDir, List<File> existing) throws IOException {
        File target = new File(rootDir, RELATIVE_PATH);
        Files.createDirectories(target.toPath().getParent());

        String content = existing.isEmpty() ? SPRING_FACTORIES_CONTENT : merge(existing);
        Files.write(target.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return target;
    }

    private static String merge(List<File> existing) throws IOException {
        Map<String, Set<String>> entries = new LinkedHashMap<>();
        for (File file : existing) {
            Properties properties = new Properties();
            try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
            for (String key : properties.stringPropertyNames()) {
                Set<String> values = entries.computeIfAbsent(key, ignored -> new LinkedHashSet<>());
                for (String value : properties.getProperty(key).split(",")) {
                    String trimmed = value.trim();
                    if (!trimmed.isEmpty()) {
                        values.add(trimmed);
                    }
                }
            }
        }

        REQUIRED_ENTRIES.forEach((key, value) ->
                entries.computeIfAbsent(key, ignored -> new LinkedHashSet<>()).add(value));

        StringBuilder builder = new StringBuilder();
        entries.forEach((key, values) ->
                builder.append(key).append('=').append(String.join(",", values)).append('\n'));
        return builder.toString();
    }
}
