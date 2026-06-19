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
package com.axelixlabs.axelix.common.utils;

import org.jspecify.annotations.Nullable;

/**
 * Builder for semantic version value object.
 *
 * @author Artemiy Degtyarev
 */
public class SemanticVersionBuilder {
    private @Nullable Integer major;
    private @Nullable Integer minor;
    private @Nullable Integer patch;
    private @Nullable String qualifier;

    public SemanticVersionBuilder setMajor(int major) {
        if (major < 0) {
            throw new IllegalArgumentException("major must be non-negative");
        }

        this.major = major;
        return this;
    }

    public SemanticVersionBuilder setMinor(int minor) {
        if (minor < 0) {
            throw new IllegalArgumentException("major must be non-negative");
        }

        this.minor = minor;
        return this;
    }

    public SemanticVersionBuilder setPatch(int patch) {
        if (patch < 0) {
            throw new IllegalArgumentException("major must be non-negative");
        }

        this.patch = patch;
        return this;
    }

    public SemanticVersionBuilder setQualifier(@Nullable String qualifier) {
        this.qualifier = qualifier;
        return this;
    }

    /**
     * Creates semantic version value object.
     *
     * @return the parsed {@link SemanticVersion}, or {@code null} if the {@code major} or {@code minor} or {@code patch} is
     *         {@code null}
     */
    public @Nullable SemanticVersion createSemanticVersion() {
        if (major == null || minor == null || patch == null) {
            throw new IllegalArgumentException("major, minor and patch fields must be non-null");
        }

        return new SemanticVersion(major, minor, patch, qualifier);
    }
}
