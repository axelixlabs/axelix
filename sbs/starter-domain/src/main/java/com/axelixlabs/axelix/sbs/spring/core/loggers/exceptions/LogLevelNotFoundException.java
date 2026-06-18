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
package com.axelixlabs.axelix.sbs.spring.core.loggers.exceptions;

import java.util.List;

/**
 * Exception thrown when a log level is missing or invalid.
 *
 * @author Nikita Kirillov
 */
public class LogLevelNotFoundException extends RuntimeException {

    public static final String LOG_LEVEL_REQUIRED_MESSAGE = "Log level cannot be null or blank";

    public static final String INVALID_LOG_LEVEL_MESSAGE = "Log level '%s' is invalid. Supported levels are: %s";

    public LogLevelNotFoundException(String message) {
        super(message);
    }

    public LogLevelNotFoundException(String invalidLevel, List<String> supportedLevels, Throwable throwable) {
        super(String.format(INVALID_LOG_LEVEL_MESSAGE, invalidLevel, String.join(", ", supportedLevels)), throwable);
    }
}
