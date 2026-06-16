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

import org.apache.maven.artifact.versioning.ComparableVersion;

/**
 * Decides whether a project's Thymeleaf version is below the floor required by the
 * spring-test-profiler diagnostic report renderer.
 */
final class ThymeleafVersionPolicy {

    static final String FLOOR = "3.1.5.RELEASE";

    private static final ComparableVersion FLOOR_VERSION = new ComparableVersion(FLOOR);

    private ThymeleafVersionPolicy() {}

    /**
     * Returns {@code true} when the given version is unknown (null or blank) or strictly lower than
     * {@value #FLOOR}, meaning the test classpath needs Thymeleaf to be raised to the floor.
     */
    static boolean isBelowFloor(String version) {
        if (version == null || version.trim().isEmpty()) {
            return true;
        }
        return new ComparableVersion(version.trim()).compareTo(FLOOR_VERSION) < 0;
    }
}
