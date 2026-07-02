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
package com.axelixlabs.axelix.master.api.external.request.loggers;

import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * Request to change the logging level of a logger across several instances.
 *
 * @param instanceIds     the instances on which to apply the new logging level
 * @param loggerName      the logger name to update
 * @param ttlSeconds      Optional duration in seconds before reverting to the original level.
 *                        If {@code null}, the change is permanent.
 * @param configuredLevel the new logging level to apply
 *
 * @author Sergey Cherkasov
 */
public record LogLevelLoggerBulkChangeRequest(
        List<String> instanceIds,
        String loggerName,
        @Nullable Long ttlSeconds,
        String configuredLevel) {}
