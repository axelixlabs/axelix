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
package com.axelixlabs.axelix.master.api.external.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.axelixlabs.axelix.common.api.ProfileMutationResult;

/**
 * The response of a profile update operation in the application.
 *
 * @param updated indicates whether the profiles were successfully updated
 * @param reason  the reason describing why the update was applied or skipped
 *
 * @see ProfileMutationResult
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
public record ProfileUpdateResponse(
        @Schema(description = "Indicates whether the profiles were successfully updated", example = "true")
        boolean updated,

        @Schema(
                description = "The reason describing why the update was applied or skipped (may be null)",
                nullable = true,
                example = "Profiles replaced successfully")
        String reason) {}
