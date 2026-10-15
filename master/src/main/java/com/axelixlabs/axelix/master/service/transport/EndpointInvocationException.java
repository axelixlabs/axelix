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
package com.axelixlabs.axelix.master.service.transport;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoint;

/**
 * The exception that occurs when Axelix Master tried to reach a particular {@link ActuatorEndpoint}
 * on the managed service, but the managed service is either not available, or responded with non 2xx status
 * code.
 *
 * @author Mikhail Polivakha
 */
public class EndpointInvocationException extends RuntimeException {

    private final @Nullable Integer statusCode;
    private final @Nullable String retryAfter;

    public EndpointInvocationException(Throwable cause) {
        super(cause);
        this.statusCode = null;
        this.retryAfter = null;
    }

    public EndpointInvocationException(String message) {
        super(message);
        this.statusCode = null;
        this.retryAfter = null;
    }

    /**
     * @param statusCode the HTTP status of the response, or {@code null} if no response was received
     *                   (connection refused, timeout, DNS failure)
     * @param retryAfter the raw value of the {@code Retry-After} response header, if present
     */
    public EndpointInvocationException(
            String message, @Nullable Throwable cause, @Nullable Integer statusCode, @Nullable String retryAfter) {
        super(message, cause);
        this.statusCode = statusCode;
        this.retryAfter = retryAfter;
    }

    public @Nullable Integer getStatusCode() {
        return statusCode;
    }

    public @Nullable String getRetryAfter() {
        return retryAfter;
    }
}
