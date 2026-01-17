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
package com.nucleonforge.axelix.common.api.gclog;

import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * Response DTO representing the current status of garbage collection logging.
 *
 * @param enabled indicates whether GC logging is currently enabled (true) or disabled (false).
 * @param level The verbosity level of GC logging (e.g., "info", "debug", "trace").
 *              May be null if logging is disabled.
 * @param availableLevels list of available GC log levels supported by the JVM
 *
 * @since 10.01.2026
 * @author Nikita Kirillov
 */
public record GcLogStatusResponse(boolean enabled, @Nullable String level, List<String> availableLevels) {}
