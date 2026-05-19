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

import com.axelixlabs.axelix.master.api.error.ApiError;
import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.error.handle.ApiErrorCodes;
import com.axelixlabs.axelix.master.api.error.handle.ExceptionHandler;
import com.axelixlabs.axelix.master.exception.auth.OAuth2AuthenticationException;
import com.axelixlabs.axelix.master.exception.auth.OidcMetadataUnavailableException;

/**
 * {@link ExceptionHandler} for {@link OAuth2AuthenticationException}.
 *
 * @author Nikita Kirillov
 */
@Component
public class OidcAuthenticationExceptionHandler implements ExceptionHandler<OAuth2AuthenticationException> {

    @Override
    public ApiError handle(OAuth2AuthenticationException exception) {

        if (exception instanceof OidcMetadataUnavailableException) {
            return new SimpleApiError(ApiErrorCodes.BAD_GATEWAY.getErrorCode(), HttpStatus.BAD_GATEWAY.value());
        }

        return new SimpleApiError(
                ApiErrorCodes.OAUTH2_AUTHENTICATION_FAILURE.getErrorCode(), HttpStatus.UNAUTHORIZED.value());
    }

    @Override
    public Class<OAuth2AuthenticationException> supported() {
        return OAuth2AuthenticationException.class;
    }
}
