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
package com.axelixlabs.axelix.master.service.auth;

import org.jspecify.annotations.Nullable;

import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;

/**
 * First-class representation of a single Axelix Master web API endpoint — i.e. one operation a user can
 * invoke from the UI. It is the master-side analogue of {@link com.axelixlabs.axelix.common.domain.ActuatorEndpoint}.
 *
 * @param operationCode stable, human-readable identifier of the operation, e.g. {@code "environment:read"}.
 * @param method        the HTTP method the endpoint is reached by.
 * @param path          the relative path pattern of the endpoint (the same prefix-stripped form the
 * @param authority     the authority required to access the endpoint, or {@code null} if none is required.
 *
 * @author Mikhail Polivakha
 */
public record MasterWebEndpoint(
        String operationCode,
        HttpMethod method,
        PathPattern path,
        @Nullable Authority authority) {

    private static final PathPatternParser PATH_PATTERN_PARSER = new PathPatternParser();

    /**
     * Builds an endpoint from a raw path pattern, parsing it into a {@link PathPattern}.
     *
     * @param operationCode stable, human-readable identifier of the operation.
     * @param method        the HTTP method the endpoint is reached by.
     * @param pathPattern   the relative path pattern (e.g. {@code /env/feed/{instanceId}}).
     * @param authority     the authority required to access the endpoint, or {@code null} if none is required.
     */
    public static MasterWebEndpoint of(
            String operationCode, HttpMethod method, String pathPattern, @Nullable Authority authority) {
        return new MasterWebEndpoint(operationCode, method, PATH_PATTERN_PARSER.parse(pathPattern), authority);
    }
}
