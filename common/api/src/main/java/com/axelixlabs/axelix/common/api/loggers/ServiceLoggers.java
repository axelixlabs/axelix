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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The response to loggers actuator endpoint.
 *
 * @author Sergey Cherkasov
 */
public final class ServiceLoggers {

    private final List<String> levels;
    private final Map<String, LoggerLevels> loggers;
    private final Map<String, LoggerGroup> groups;

    public ServiceLoggers(
            @JsonProperty("levels") List<String> levels,
            @JsonProperty("loggers") Map<String, LoggerLevels> loggers,
            @JsonProperty("groups") Map<String, LoggerGroup> groups) {
        this.levels = levels;
        this.loggers = loggers;
        this.groups = groups;
    }

    public List<String> levels() {
        return levels;
    }

    public Map<String, LoggerLevels> loggers() {
        return loggers;
    }

    public Map<String, LoggerGroup> groups() {
        return groups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceLoggers that = (ServiceLoggers) o;
        return Objects.equals(levels, that.levels)
                && Objects.equals(loggers, that.loggers)
                && Objects.equals(groups, that.groups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(levels, loggers, groups);
    }

    @Override
    public String toString() {
        return "ServiceLoggers{" + "levels=" + levels + ", loggers=" + loggers + ", groups=" + groups + '}';
    }
}
