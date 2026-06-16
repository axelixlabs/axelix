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
package com.axelixlabs.gradle.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import static java.nio.charset.StandardCharsets.UTF_8;

/** Loads build-script and Java-source fixtures bundled as test resources alongside this package. */
final class GradleProjectFixtures {

    private GradleProjectFixtures() {}

    static String buildScript(String fixtureName) {
        return load(fixtureName);
    }

    /**
     * Loads a Java-source fixture. The sources are stored with a {@code .java.txt} suffix so that
     * Spotless (which targets {@code src/**}/{@code *.java}) does not reformat them or inject a
     * license header into the generated test project.
     */
    static String javaSource(String fixtureName) {
        return load(fixtureName);
    }

    private static String load(String fixtureName) {
        try (InputStream in = GradleProjectFixtures.class.getResourceAsStream(fixtureName)) {
            if (in == null) {
                throw new IllegalStateException("Missing test fixture: " + fixtureName);
            }
            return new String(in.readAllBytes(), UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
