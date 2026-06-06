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
package com.axelixlabs.axelix.master.filter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SpaStaticResourcesServingFilterPathSanitizationTest {

    @ParameterizedTest
    @CsvSource({
        "/assets/main.js,assets/main.js",
        "/assets/main-ABC.123.js,assets/main-ABC.123.js",
        "/wallboard,wallboard",
    })
    void shouldKeepSafeRelativePaths(String contextPath, String expectedRelativePath) {
        // given.
        String inputPath = contextPath;

        // when.
        String sanitizedPath = SpaStaticResourcesServingFilter.sanitizeRelativePath(inputPath);

        // then.
        assertThat(sanitizedPath).isEqualTo(expectedRelativePath);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "",
                "/",
                "/../secrets.txt",
                "/assets/../../secrets.txt",
                "/assets/./main.js",
                "/assets//main.js",
                "/assets\\main.js",
                "/assets:main.js",
            })
    void shouldFallbackToIndexHtmlForUnsafePaths(String contextPath) {
        // given.
        String inputPath = contextPath;

        // when.
        String sanitizedPath = SpaStaticResourcesServingFilter.sanitizeRelativePath(inputPath);

        // then.
        assertThat(sanitizedPath).isEqualTo("index.html");
    }
}
