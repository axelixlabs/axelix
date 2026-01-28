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
package com.axelixlabs.axelix.master.api.error.handle.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;
import com.axelixlabs.axelix.master.api.error.ApiError;
import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.error.handle.ApiErrorCodes;
import com.axelixlabs.axelix.master.api.error.handle.ExceptionHandler;

/**
 * {@link ExceptionHandler} for {@link JwtProcessingException}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class JwtProcessingExceptionHandler implements ExceptionHandler<JwtProcessingException> {

    @Override
    public ApiError handle(JwtProcessingException exception) {
        return new SimpleApiError(
                ApiErrorCodes.INVALID_JWT_EXCEPTION_CODE.getErrorCode(), HttpStatus.UNAUTHORIZED.value());
    }

    @Override
    public Class<JwtProcessingException> supported() {
        return JwtProcessingException.class;
    }
}
