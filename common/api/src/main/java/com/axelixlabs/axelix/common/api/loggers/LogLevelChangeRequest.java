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

/**
 * Request to change the logging level of a logger or a logger group.
 *
 * @author Sergey Cherkasov
 */
public class LogLevelChangeRequest {

    private final String configuredLevel;

    /**
     * Creates a new LogLevelChangeRequest.
     *
     * @param configuredLevel   The new logging level to apply.
     */
    @JsonCreator
    public LogLevelChangeRequest(@JsonProperty("configuredLevel") String configuredLevel) {
        this.configuredLevel = configuredLevel;
    }

    public String getConfiguredLevel() {
        return configuredLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogLevelChangeRequest that = (LogLevelChangeRequest) o;
        return Objects.equals(configuredLevel, that.configuredLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(configuredLevel);
    }

    @Override
    public String toString() {
        return "LogLevelChangeRequest{" + "configuredLevel='" + configuredLevel + '\'' + '}';
    }
}
