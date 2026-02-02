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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * DTO that encapsulates the logging level information of the loggers group.
 *
 * @author Sergey Cherkasov
 */
public final class LoggerGroup {

    @Nullable
    private final String configuredLevel;

    private final List<String> members;

    public LoggerGroup(
            @JsonProperty("configuredLevel") @Nullable String configuredLevel,
            @JsonProperty("members") List<String> members) {
        this.configuredLevel = configuredLevel;
        this.members = members;
    }

    @Nullable
    public String configuredLevel() {
        return configuredLevel;
    }

    public List<String> members() {
        return members;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LoggerGroup that = (LoggerGroup) o;
        return Objects.equals(configuredLevel, that.configuredLevel) && Objects.equals(members, that.members);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuredLevel, members);
    }

    @Override
    public String toString() {
        return "LoggerGroup{" + "configuredLevel='" + configuredLevel + '\'' + ", members=" + members + '}';
    }
}
