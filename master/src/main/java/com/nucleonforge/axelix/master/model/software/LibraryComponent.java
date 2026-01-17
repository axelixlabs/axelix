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
package com.nucleonforge.axelix.master.model.software;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The abstract library, like Hibernate or Spring Framework. It is an abstract notion
 * i.e. it does not have a specific version or instance it is bound to.
 *
 * @see SoftwareDistribution
 * @author Mikhail Polivakha
 */
public class LibraryComponent implements SoftwareComponent {

    private final String artifactId;
    private final String groupId;
    private final String slug;

    @Nullable
    private final String description;

    boolean isCore;

    public LibraryComponent(
            @NonNull String artifactId,
            @NonNull String groupId,
            @NonNull String slug,
            @Nullable String description,
            boolean isCore) {
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.slug = slug;
        this.description = description;
        this.isCore = isCore;
    }

    @Override
    public @NonNull String getName() {
        return slug;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public boolean isCore() {
        return isCore;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getGroupId() {
        return groupId;
    }
}
