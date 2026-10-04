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
package com.axelixlabs.axelix.master.api.external.response.dependencies;

import java.time.Instant;
import java.util.List;

/**
 * The dependency analysis of a single instance: its framework maintenance window and every dependency resolved onto
 * its runtime classpath, joined with the curated catalog.
 *
 * @param rootCoordinates the {@code groupId:artifactId:version} libraries of the analyzed application itself, or
 *                        the raw name the build reported when the build declares no group or version.
 * @param analyzedAt      when the analysis was taken.
 * @param framework       the framework that underpins the given application. Right now, always Spring Boot.
 * @param dependencies    every dependency resolved onto the runtime classpath.
 *
 * @author Mikhail Polivakha
 */
public record DependencyAnalysisResponse(
        String rootCoordinates,
        Instant analyzedAt,
        AppFrameworkInfo framework,
        List<AnalyzedDependency> dependencies) {}
