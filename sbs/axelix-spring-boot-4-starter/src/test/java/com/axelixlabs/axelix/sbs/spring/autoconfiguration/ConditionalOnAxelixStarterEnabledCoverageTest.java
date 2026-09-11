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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.core.annotation.AnnotatedElementUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that every auto-configuration in {@code AutoConfiguration.imports} is annotated with
 * {@link ConditionalOnAxelixStarterEnabled}.
 *
 * @author Nikita Kirillov
 */
class ConditionalOnAxelixStarterEnabledCoverageTest {

    private static final String IMPORTS_RESOURCE =
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    @Test
    void everyAutoConfigurationCarriesTheKillSwitchAnnotation() throws IOException, ClassNotFoundException {
        List<String> autoConfigurationClassNames = readAutoConfigurationClassNames();
        assertThat(autoConfigurationClassNames).isNotEmpty();

        List<String> uncovered = new ArrayList<>();
        for (String className : autoConfigurationClassNames) {
            Class<?> autoConfigurationClass = Class.forName(className);
            if (!AnnotatedElementUtils.hasAnnotation(autoConfigurationClass, ConditionalOnAxelixStarterEnabled.class)) {
                uncovered.add(className);
            }
        }

        assertThat(uncovered)
                .as(
                        "every class listed in %s must be annotated with @%s, or axelix.sbs.enabled=false "
                                + "won't actually disable it",
                        IMPORTS_RESOURCE, ConditionalOnAxelixStarterEnabled.class.getSimpleName())
                .isEmpty();
    }

    private List<String> readAutoConfigurationClassNames() throws IOException {
        List<String> classNames = new ArrayList<>();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(IMPORTS_RESOURCE)) {
            assertThat(stream)
                    .as("%s must exist on the test classpath", IMPORTS_RESOURCE)
                    .isNotNull();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                        classNames.add(trimmed);
                    }
                }
            }
        }
        return classNames;
    }
}
