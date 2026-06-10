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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The response to loggers actuator endpoint.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class LoggersFeed {

    private final List<String> levels;
    private final List<SingleLoggerProfile> loggers;
    private final List<GroupLoggerProfile> groups;

    public LoggersFeed(
            @JsonProperty("levels") List<String> levels,
            @JsonProperty("loggers") List<SingleLoggerProfile> loggers,
            @JsonProperty("groups") List<GroupLoggerProfile> groups) {
        this.levels = levels;
        this.loggers = loggers;
        this.groups = groups;
    }

    public List<String> getLevels() {
        return levels;
    }

    public List<SingleLoggerProfile> getLoggers() {
        return loggers;
    }

    public List<GroupLoggerProfile> getGroups() {
        return groups;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LoggersFeed)) {
            return false;
        }
        LoggersFeed that = (LoggersFeed) o;
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
        return "LoggersFeed{" + "levels=" + levels + ", loggers=" + loggers + ", groups=" + groups + '}';
    }
}
