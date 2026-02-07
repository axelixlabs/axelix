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
package com.axelixlabs.axelix.master.api.external.response.loggers;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

/**
 * The profile of the logging system.
 *
 * @param levels     The levels supported by the current logging system.
 * @param groups     The logger groups keyed by name.
 * @param loggers    The loggers keyed by name.
 *
 * @author Sergey Cherkasov
 */
public record LoggersResponse(List<String> levels, List<Group> groups, List<Logger> loggers) {

    /**
     * The profile of a group with its loggers.
     *
     * @param name              The name of the logger group.
     * @param configuredLevel   The configured level of the logger group, if any.
     * @param members           The loggers that are part of this group.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Group(String name, @Nullable String configuredLevel, List<String> members) {}

    /**
     * Profile of the active logger.
     *
     * @param name             The name of the logger.
     * @param configuredLevel  The configured logger level, if any.
     * @param effectiveLevel   The logger's current level.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Logger(String name, @Nullable String configuredLevel, String effectiveLevel) {}
}
