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
package com.axelixlabs.axelix.master.api.error.handle;

/**
 * The error codes that are be returned from the HTTP API.
 *
 * @author Mikhail Polivakha
 */
public enum ApiErrorCodes {

    // Server fault error codes
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR"),
    BAD_GATEWAY("BAD_GATEWAY"),

    // Auth related error codes
    INVALID_CREDENTIALS("INVALID_CREDENTIALS"),
    INVALID_JWT_EXCEPTION("INVALID_JWT_EXCEPTION"),
    AUTHORIZATION_FAILURE("AUTHORIZATION_FAILURE"),
    OAUTH2_AUTHENTICATION_FAILURE("OIDC_AUTHENTICATION_FAILURE"),

    // Bad request related error codes
    BAD_REQUEST("BAD_REQUEST"),
    INSTANCE_NOT_FOUND("INSTANCE_NOT_FOUND"),
    INVALID_CRON_EXPRESSION("INVALID_CRON_EXPRESSION"),
    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS"),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS"),
    PARTIALLY_UPDATED("PARTIALLY_UPDATED");

    /**
     * actual code that is sent from the master backend.
     */
    private final String errorCode;

    ApiErrorCodes(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
