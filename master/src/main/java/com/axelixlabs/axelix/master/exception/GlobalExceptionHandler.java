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
package com.axelixlabs.axelix.master.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.axelixlabs.axelix.master.api.error.ApiError;
import com.axelixlabs.axelix.master.api.error.handle.ApiExceptionTranslator;

/**
 * Global exception handler. Handles exceptions that occurred in standard spring-web
 * endpoints like {@link org.springframework.web.bind.annotation.GetMapping @GetMapping} or
 * similar. It does not however handle exceptions in filter chain, which is where auth is
 * implemented - {@link ExceptionHandlingFilter} is responsible for that.
 *
 * @see ExceptionHandlingFilter
 *
 * @since 29.08.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ApiExceptionTranslator apiExceptionTranslator;

    public GlobalExceptionHandler(ApiExceptionTranslator apiExceptionTranslator) {
        this.apiExceptionTranslator = apiExceptionTranslator;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleEndpointException(Exception ex) {
        ApiError apiError = apiExceptionTranslator.translateException(ex);
        return ResponseEntity.status(apiError.statusCode()).body(apiError);
    }
}
