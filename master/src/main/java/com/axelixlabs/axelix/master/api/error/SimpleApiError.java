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
package com.axelixlabs.axelix.master.api.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jspecify.annotations.NonNull;

/**
 * Simple implementation of the {@link ApiError} with no additional information.
 *
 * @author Mikhail Polivakha
 */
// TODO:
//  we're serializing this in json format. I am not sure that this is a good idea.
//  conceptually, this is just the metadata about the response to the UI, not its body.
public record SimpleApiError(
        @NonNull String errorCode, @JsonIgnore int statusCode) implements ApiError {}
