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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * Request to change the logging level of a logger or a logger group.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
public class LogLevelChangeRequest {

    private final String configuredLevel;
    private final @Nullable Long ttlSeconds;

    /**
     * Creates a new LogLevelChangeRequest.
     *
     * @param configuredLevel The new logging level to apply.
     * @param ttlSeconds      Optional duration in minutes before reverting to the original level.
     *                        If {@code null}, the change is permanent.
     */
    @JsonCreator
    public LogLevelChangeRequest(
            @JsonProperty("configuredLevel") String configuredLevel,
            @JsonProperty("ttlSeconds") @Nullable Long ttlSeconds) {
        if (ttlSeconds != null && ttlSeconds <= 0) {
            throw new IllegalArgumentException("ttlSeconds must be positive, got: " + ttlSeconds);
        }
        this.configuredLevel = configuredLevel;
        this.ttlSeconds = ttlSeconds;
    }

    public String getConfiguredLevel() {
        return configuredLevel;
    }

    public @Nullable Long getTtlSeconds() {
        return ttlSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LogLevelChangeRequest)) {
            return false;
        }
        LogLevelChangeRequest that = (LogLevelChangeRequest) o;
        return Objects.equals(configuredLevel, that.configuredLevel) && Objects.equals(ttlSeconds, that.ttlSeconds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuredLevel, ttlSeconds);
    }

    @Override
    public String toString() {
        return "LogLevelChangeRequest{" + "configuredLevel='"
                + configuredLevel + '\'' + ", ttlSeconds="
                + ttlSeconds + '}';
    }
}
