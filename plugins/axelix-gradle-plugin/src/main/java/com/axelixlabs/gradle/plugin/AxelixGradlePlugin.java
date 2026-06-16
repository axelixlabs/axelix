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

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Axelix Gradle plugin entry point. The configuration it performs is built up incrementally; this
 * is the module skeleton and is currently a no-op once the {@code java} plugin is applied.
 *
 * <p>Compatible with Gradle 4.0 through 9.x. Only APIs present in BOTH Gradle 4.0 and 9.x may be
 * used here: no {@code tasks.register} (added in 4.9), no lazy {@code Provider}/{@code Property}
 * wiring, no sourceSets/conventions access ({@code JavaPluginConvention} was removed in Gradle 9,
 * {@code JavaPluginExtension.getSourceSets()} was only added in 7.1).
 *
 * <p>The plugin defers all work until the {@code java} plugin is applied, because the
 * {@code testRuntimeOnly} configuration only exists once it is.
 */
public class AxelixGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {
        project.getPluginManager().withPlugin("java", appliedPlugin -> configure(project));
    }

    private void configure(Project project) {
        // Configuration is added incrementally in subsequent changes.
    }
}
