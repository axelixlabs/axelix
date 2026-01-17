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
package com.nucleonforge.axelix.master.api.error.handle.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;

import com.nucleonforge.axelix.master.api.error.ApiError;
import com.nucleonforge.axelix.master.api.error.SimpleApiError;
import com.nucleonforge.axelix.master.api.error.handle.ApiErrorCodes;
import com.nucleonforge.axelix.master.api.error.handle.ExceptionHandler;

/**
 * The default {@link ExceptionHandler} where calls are forwarded when no specific
 * {@link ExceptionHandler ExceptionHandlers} are found.
 *
 * @author Mikhail Polivakha
 */
public class DefaultExceptionHandler implements ExceptionHandler<Exception> {

    public static final DefaultExceptionHandler INSTANCE = new DefaultExceptionHandler();
    private static final Logger log = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @Override
    public ApiError handle(Exception exception) {
        log.warn("Default exception handler received an exception", exception);
        return new SimpleApiError(
                ApiErrorCodes.INTERNAL_SERVER_ERROR_CODE.getErrorCode(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Override
    public Class<Exception> supported() {
        return Exception.class;
    }
}
