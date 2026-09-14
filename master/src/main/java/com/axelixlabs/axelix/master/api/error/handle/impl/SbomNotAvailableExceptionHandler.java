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

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.master.api.error.ApiError;
import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.error.handle.ApiErrorCodes;
import com.axelixlabs.axelix.master.api.error.handle.ExceptionHandler;
import com.axelixlabs.axelix.master.exception.SbomNotAvailableException;

/**
 * {@link ExceptionHandler} for {@link SbomNotAvailableException}. Responds with {@code 404}, distinct from the
 * {@code 400 INSTANCE_NOT_FOUND} of an unknown instance, so the UI can render a dedicated "rebuild with the Axelix
 * build plugin" empty state.
 *
 * @author Mikhail Polivakha
 */
@Component
public class SbomNotAvailableExceptionHandler implements ExceptionHandler<SbomNotAvailableException> {

    @Override
    public ApiError handle(HttpServletRequest request, SbomNotAvailableException exception) {
        return new SimpleApiError(ApiErrorCodes.SBOM_NOT_AVAILABLE.getErrorCode(), HttpStatus.NOT_FOUND.value());
    }

    @Override
    public Class<SbomNotAvailableException> supported() {
        return SbomNotAvailableException.class;
    }
}
