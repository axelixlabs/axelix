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

import java.io.File;

import org.gradle.api.Project;
import org.gradle.util.GradleVersion;

/**
 * Resolves the project build directory across Gradle 4.0 through 9.x: {@code getBuildDir()} on
 * old versions, {@code layout.buildDirectory} on 5.0+ where the former triggers deprecation
 * warnings (8.3+).
 */
final class BuildDirCompat {

    private static final GradleVersion MODERN = GradleVersion.version("5.0");

    private BuildDirCompat() {}

    static File buildDir(Project project) {
        if (GradleVersion.current().getBaseVersion().compareTo(MODERN) >= 0) {
            return Modern.buildDir(project);
        }
        return legacyBuildDir(project);
    }

    @SuppressWarnings("deprecation")
    private static File legacyBuildDir(Project project) {
        return project.getBuildDir();
    }

    /**
     * Separate class so that Gradle 4.0 daemons never resolve
     * {@code ProjectLayout.getBuildDirectory()}, which did not exist back then.
     */
    private static final class Modern {

        private Modern() {}

        static File buildDir(Project project) {
            return project.getLayout().getBuildDirectory().get().getAsFile();
        }
    }
}
