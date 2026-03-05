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
package com.axelixlabs.axelix.master.api.external.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a request to update the currently active Spring profiles
 * in a specific application instance.
 *
 * @param effectiveProfiles the list of profiles to activate in the targeted instance. Passing an empty
 * list will deactivate all currently active profiles.
 *
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
public record ProfileUpdatedRequest(
        @ArraySchema(
                schema = @Schema(example = "[\"profile1\", \"profile2\"]", type = "array"),
                arraySchema =
                        @Schema(
                                description =
                                        "Array of effective profiles. To remove all active profiles in instance, the array must be empty"))
        List<String> effectiveProfiles) {}
