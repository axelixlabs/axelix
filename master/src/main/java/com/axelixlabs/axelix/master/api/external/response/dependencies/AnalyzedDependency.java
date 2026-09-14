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

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.domain.ecosystem.libraries.Library;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProject;

/**
 * One dependency resolved onto the runtime classpath of a managed application, joined with what the curated catalog
 * states about the project behind it.
 *
 * @param dependency      the analyzed dependency itself.
 * @param resolutionPath  the chain of {@code groupId:artifactId:version} libraries leading from the root
 *                        application to this dependency, the dependency itself being the last element and the root
 *                        excluded; a chain of a single element is a direct dependency
 * @param softwareProject the curated catalog entry of the project behind the dependency, or null when Axelix Master
 *                        is not aware about this library belonging to the project.
 *
 * @author Mikhail Polivakha
 */
public record AnalyzedDependency(
        Dependency dependency,
        List<String> resolutionPath,
        @Nullable SoftwareProject softwareProject) {

    /**
     * Represents the dependency of the project. Dependency is the concrete version of the {@link Library}.
     *
     * @param library         the version-free library that represents the dependency itself
     * @param version         the version that was actually resolved
     */
    public record Dependency(Library library, String version) { }
}
