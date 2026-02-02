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
package com.axelixlabs.axelix.common.api.loggers;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * DTO that encapsulates the logging level information of the single logger.
 *
 * @author Sergey Cherkasov
 */
public final class LoggerLevels {

    @Nullable
    private final String configuredLevel;

    private final String effectiveLevel;

    public LoggerLevels(
            @JsonProperty("configuredLevel") @Nullable String configuredLevel,
            @JsonProperty("effectiveLevel") String effectiveLevel) {
        this.configuredLevel = configuredLevel;
        this.effectiveLevel = effectiveLevel;
    }

    @Nullable
    public String configuredLevel() {
        return configuredLevel;
    }

    public String effectiveLevel() {
        return effectiveLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LoggerLevels that = (LoggerLevels) o;
        return Objects.equals(configuredLevel, that.configuredLevel)
                && Objects.equals(effectiveLevel, that.effectiveLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuredLevel, effectiveLevel);
    }

    @Override
    public String toString() {
        return "LoggerLevels{"
                + "configuredLevel='"
                + configuredLevel
                + '\''
                + ", effectiveLevel='"
                + effectiveLevel
                + '\''
                + '}';
    }
}
