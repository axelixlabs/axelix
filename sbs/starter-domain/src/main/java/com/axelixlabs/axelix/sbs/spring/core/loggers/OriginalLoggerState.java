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

import org.jspecify.annotations.Nullable;

/**
 * Holds the original log level state of a logger captured before any overrides were applied.
 *
 * @author Nikita Kirillov
 */
public final class OriginalLoggerState {

    private final String effectiveLevel;

    @Nullable
    private final String configuredLevel;

    public OriginalLoggerState(String effectiveLevel, @Nullable String configuredLevel) {
        this.effectiveLevel = effectiveLevel;
        this.configuredLevel = configuredLevel;
    }

    public String getEffectiveLevel() {
        return effectiveLevel;
    }

    public @Nullable String getConfiguredLevel() {
        return configuredLevel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (OriginalLoggerState) obj;
        return Objects.equals(this.effectiveLevel, that.effectiveLevel)
                && Objects.equals(this.configuredLevel, that.configuredLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(effectiveLevel, configuredLevel);
    }

    @Override
    public String toString() {
        return "OriginalLoggerState[" + "effectiveLevel="
                + effectiveLevel + ", " + "configuredLevel="
                + configuredLevel + ']';
    }
}
