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
package com.axelixlabs.axelix.sbs.spring.master;

import java.util.Optional;

/**
 * Interface that is capable to discover the given library in the classpath.
 *
 * @author Mikhail Polivakha
 */
public interface LibraryDiscoverer {

    /**
     * Detect library version.
     *
     * @param artifactId the artifact id of the dependency
     * @param groupId the groupId of the dependency
     * @return the library version, or {@link Optional#empty()} if none were detected
     */
    Optional<String> getLibraryVersion(String artifactId, String groupId);

    /**
     * Detect library version.
     *
     * @param artifactId the artifact id of the dependency
     * @param groupId the groupId of the dependency
     * @return the library version, or {@link Optional#empty()} if none were detected
     */
    default String getRequiredLibraryVersion(String artifactId, String groupId) {
        return getLibraryVersion(artifactId, groupId)
                .orElseThrow(() ->
                        new IllegalStateException("Expected %s:%s to be available in the classpath, but there was no"));
    }
}
