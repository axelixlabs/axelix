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
package com.axelixlabs.axelix.sbs.spring.core.loggers;

import java.util.Objects;

/**
 * Holds metadata about a temporary log level override scheduled for automatic reversion.
 *
 * @author Nikita Kirillov
 */
public final class ScheduledLogLevelOverride {

    /**
     * The time at which the temporary log level override was applied.
     */
    private final String appliedAt;

    /**
     * The time at which the temporary log level override will expire and revert to the original level.
     */
    private final String expiresAt;

    /**
     * A generation token that guards against stale scheduled resets overwriting a newer override.
     * Before executing, each scheduled task verifies its captured token still matches the active
     * override — if not, a newer override has taken precedence and the reset is skipped.
     */
    private final long generation;

    public ScheduledLogLevelOverride(String appliedAt, String expiresAt, long generation) {
        this.appliedAt = appliedAt;
        this.expiresAt = expiresAt;
        this.generation = generation;
    }

    public String getAppliedAt() {
        return appliedAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public long getGeneration() {
        return generation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (ScheduledLogLevelOverride) obj;
        return Objects.equals(this.appliedAt, that.appliedAt) && Objects.equals(this.expiresAt, that.expiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appliedAt, expiresAt);
    }

    @Override
    public String toString() {
        return "ScheduledLogLevelOverride{" + "appliedAt='"
                + appliedAt + '\'' + ", expiresAt='"
                + expiresAt + '\'' + '}';
    }
}
