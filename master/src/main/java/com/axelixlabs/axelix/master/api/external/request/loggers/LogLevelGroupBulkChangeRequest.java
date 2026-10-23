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
 * Request to change the logging level of a logger group across several instances.
 *
 * @param instanceIds     the instances on which to apply the new logging level
 * @param groupName       the logger group name to update
 * @param configuredLevel the new logging level to apply
 *
 * @author Mikhail Polivakha
 */
public record LogLevelGroupBulkChangeRequest(List<String> instanceIds, String groupName, String configuredLevel) {

    /**
     * Validates the required fields when creating the request.
     *
     * @throws IllegalArgumentException if the list is null or has no non-blank ID, or a required text value is blank
     */
    public LogLevelGroupBulkChangeRequest {
        BulkRequestValidator.validate(instanceIds, groupName, configuredLevel);
    }
}
