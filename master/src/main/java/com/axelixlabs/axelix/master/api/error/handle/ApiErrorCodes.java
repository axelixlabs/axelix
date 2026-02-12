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
    INTERNAL_SERVER_ERROR_CODE("INTERNAL_SERVER_ERROR"),
    INSTANCE_NOT_FOUND_CODE("INSTANCE_NOT_FOUND"),
    INVALID_CREDENTIALS_CODE("INVALID_CREDENTIALS"),
    INVALID_JWT_EXCEPTION_CODE("INVALID_JWT_EXCEPTION"),
    BAD_REQUEST("BAD_REQUEST"),
    INVALID_CRON_EXPRESSION("INVALID_CRON_EXPRESSION");

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
