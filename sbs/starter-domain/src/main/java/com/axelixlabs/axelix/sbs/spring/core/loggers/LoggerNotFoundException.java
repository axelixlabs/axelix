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
package com.axelixlabs.axelix.sbs.spring.core.loggers;

/**
 * Exception thrown when a logger or a logger group is not found.
 *
 * @author Nikita Kirillov
 */
public class LoggerNotFoundException extends RuntimeException {

    public static final String LOGGER_NOT_FOUND_MESSAGE = "Logger '%s' not found in Logging System";

    public static final String LOGGER_GROUP_NOT_FOUND_MESSAGE = "Logger group '%s' not found in Logger Groups";

    public LoggerNotFoundException(String message) {
        super(message);
    }
}
