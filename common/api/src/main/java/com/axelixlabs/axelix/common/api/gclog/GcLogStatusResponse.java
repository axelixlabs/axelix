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
package com.axelixlabs.axelix.common.api.gclog;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * Response DTO representing the current status of garbage collection logging.
 *
 * @since 10.01.2026
 * @author Nikita Kirillov
 */
public final class GcLogStatusResponse {

    private final boolean enabled;

    @Nullable
    private final String level;

    private final List<String> availableLevels;

    /**
     * Creates a new GcLogStatusResponse.
     *
     * @param enabled         indicates whether GC logging is currently enabled (true) or disabled (false).
     * @param level           The verbosity level of GC logging (e.g., "info", "debug", "trace").
     *                        May be null if logging is disabled.
     * @param availableLevels list of available GC log levels supported by the JVM
     */
    @JsonCreator
    public GcLogStatusResponse(
            @JsonProperty("enabled") boolean enabled,
            @JsonProperty("level") @Nullable String level,
            @JsonProperty("availableLevels") List<String> availableLevels) {
        this.enabled = enabled;
        this.level = level;
        this.availableLevels = availableLevels;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Nullable
    public String getLevel() {
        return level;
    }

    public List<String> getAvailableLevels() {
        return availableLevels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GcLogStatusResponse that = (GcLogStatusResponse) o;
        return enabled == that.enabled
                && Objects.equals(level, that.level)
                && Objects.equals(availableLevels, that.availableLevels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, level, availableLevels);
    }

    @Override
    public String toString() {
        return "GcLogStatusResponse{"
                + "enabled="
                + enabled
                + ", level='"
                + level
                + '\''
                + ", availableLevels="
                + availableLevels
                + '}';
    }
}
