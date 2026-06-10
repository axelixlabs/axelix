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
public final class LoggersGroupProfile {

    private final String name;

    @Nullable
    private final String configuredLevel;

    private final List<String> members;

    public LoggersGroupProfile(
            @JsonProperty("name") String name,
            @JsonProperty("configuredLevel") @Nullable String configuredLevel,
            @JsonProperty("members") List<String> members) {
        this.name = name;
        this.configuredLevel = configuredLevel;
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public @Nullable String getConfiguredLevel() {
        return configuredLevel;
    }

    public List<String> getMembers() {
        return members;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LoggersGroupProfile)) {
            return false;
        }
        LoggersGroupProfile that = (LoggersGroupProfile) o;
        return Objects.equals(name, that.name)
                && Objects.equals(configuredLevel, that.configuredLevel)
                && Objects.equals(members, that.members);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, configuredLevel, members);
    }

    @Override
    public String toString() {
        return "GroupLoggerProfile{" + "name='"
                + name + '\'' + ", configuredLevel='"
                + configuredLevel + '\'' + ", members="
                + members + '}';
    }
}
