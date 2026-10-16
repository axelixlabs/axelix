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

import com.axelixlabs.axelix.master.service.BulkRequestValidator;

/**
 * Request to reset the logging level of a logger across several instances.
 *
 * @param instanceIds the instances on which to reset the logging level
 * @param loggerName the logger name to reset
 *
 * @author Sergey Cherkasov
 */
public record LogLevelLoggerBulkResetRequest(List<String> instanceIds, String loggerName) {

    /**
     * Validates the required fields when creating the request.
     *
     * @throws IllegalArgumentException if the list is null or has no non-blank ID, or the logger name is blank
     */
    public LogLevelLoggerBulkResetRequest {
        BulkRequestValidator.validate(instanceIds, loggerName);
    }
}
