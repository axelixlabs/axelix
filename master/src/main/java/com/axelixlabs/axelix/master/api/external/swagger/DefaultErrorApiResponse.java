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
package com.axelixlabs.axelix.master.api.external.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.axelixlabs.axelix.master.api.error.SimpleApiError;

/**
 * Default composed-annotation providing unified access to common {@link ApiResponse API Responses}
 * for error codes 400 (Bad Request) and 500 (Internal Server Error).
 *
 *
 * @author Sergey Cherkasov
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
    @ApiResponse(
            description = "Bad Request",
            responseCode = "400",
            content =
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SimpleApiError.class))),
    @ApiResponse(
            description = "Internal Server Error",
            responseCode = "500",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SimpleApiError.class)))
})
public @interface DefaultErrorApiResponse {}
