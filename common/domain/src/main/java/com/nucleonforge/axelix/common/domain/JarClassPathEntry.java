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
package com.nucleonforge.axelix.common.domain;

import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Represents a dependency JAR that the app relies upon.
 *
 * @since 19.07.2025
 * @author Mikhail Polivakha
 */
public class JarClassPathEntry implements ClassPathEntry {

    @NonNull
    private final GavCoordinates gavCoordinates;

    /**
     * In case this dependency is transitive, the link to dependency that brought
     * this dependency.
     * <p>
     * Might be null
     */
    @Nullable
    private JarClassPathEntry broughtBy;

    /**
     * List of dependencies upon which dependency depends upon.
     * <p>
     * Might be empty, never null
     */
    @NonNull
    private Set<JarClassPathEntry> dependsOn;

    public JarClassPathEntry(
            String groupId,
            String artifactId,
            String version,
            @Nullable JarClassPathEntry broughtBy,
            @NonNull Set<JarClassPathEntry> dependsOn) {
        this.gavCoordinates = new GavCoordinates(groupId, artifactId, version);
        this.broughtBy = broughtBy;
        this.dependsOn = dependsOn;
    }

    public String getGroupId() {
        return gavCoordinates.groupId();
    }

    public String getArtifactId() {
        return gavCoordinates.artifactId();
    }

    public String getVersion() {
        return gavCoordinates.version();
    }

    public @Nullable JarClassPathEntry getBroughtBy() {
        return broughtBy;
    }

    public JarClassPathEntry setBroughtBy(@Nullable JarClassPathEntry broughtBy) {
        this.broughtBy = broughtBy;
        return this;
    }

    public @NonNull Set<JarClassPathEntry> getDependsOn() {
        return dependsOn;
    }

    public JarClassPathEntry setDependsOn(@NonNull Set<JarClassPathEntry> dependsOn) {
        this.dependsOn = dependsOn;
        return this;
    }
}
