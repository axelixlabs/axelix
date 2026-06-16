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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure (Maven-free) logic for reading, merging and rendering a {@code META-INF/spring.factories}
 * file. The Axelix {@code TestExecutionListener} and {@code ApplicationContextInitializer} entries
 * are appended to any existing values for their keys without duplication, so a consumer-provided
 * {@code spring.factories} is preserved rather than overwritten.
 */
final class SpringFactoriesMerger {

    static final String TEST_EXECUTION_LISTENER_KEY = "org.springframework.test.context.TestExecutionListener";
    static final String TEST_EXECUTION_LISTENER_VALUE = "digital.pragmatech.testing.SpringTestProfilerListener";
    static final String APPLICATION_CONTEXT_INITIALIZER_KEY =
            "org.springframework.context.ApplicationContextInitializer";
    static final String APPLICATION_CONTEXT_INITIALIZER_VALUE =
            "digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer";

    private SpringFactoriesMerger() {}

    /**
     * Parses {@code spring.factories} content into an ordered key to value map, joining backslash
     * line continuations and skipping blank lines and comments.
     */
    static Map<String, String> parse(String content) {
        Map<String, String> result = new LinkedHashMap<>();
        String joined = content.replace("\\\r\n", "").replace("\\\n", "");
        for (String rawLine : joined.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int separator = line.indexOf('=');
            if (separator < 0) {
                continue;
            }
            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            result.put(key, value);
        }
        return result;
    }

    /** Appends the Axelix entries to the given map, returning the same instance for convenience. */
    static Map<String, String> merge(Map<String, String> factories) {
        appendValue(factories, TEST_EXECUTION_LISTENER_KEY, TEST_EXECUTION_LISTENER_VALUE);
        appendValue(factories, APPLICATION_CONTEXT_INITIALIZER_KEY, APPLICATION_CONTEXT_INITIALIZER_VALUE);
        return factories;
    }

    /** Renders the map back into {@code spring.factories} format, one {@code key=value} per line. */
    static String render(Map<String, String> factories) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : factories.entrySet()) {
            builder.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
        }
        return builder.toString();
    }

    private static void appendValue(Map<String, String> factories, String key, String value) {
        String current = factories.get(key);
        if (current == null || current.trim().isEmpty()) {
            factories.put(key, value);
            return;
        }
        List<String> values = new ArrayList<>();
        for (String existing : current.split(",")) {
            String trimmed = existing.trim();
            if (!trimmed.isEmpty()) {
                values.add(trimmed);
            }
        }
        if (!values.contains(value)) {
            values.add(value);
        }
        factories.put(key, String.join(",", values));
    }
}
