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
package com.nucleonforge.axelix.common.domain.http;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link HttpPayload}.
 *
 * @since 02.09.2025
 * @author Nikita Kirillov
 */
public record DefaultHttpPayload(
        List<HttpHeader> headers,
        List<QueryParameter<?>> queryParameters,
        Map<String, String> pathVariableValues,
        byte[] requestBody)
        implements HttpPayload {

    public DefaultHttpPayload(List<HttpHeader> headers) {
        this(headers, Collections.emptyList(), Collections.emptyMap(), new byte[0]);
    }

    public DefaultHttpPayload(Map<String, String> pathVariableValues) {
        this(Collections.emptyList(), Collections.emptyList(), pathVariableValues, new byte[0]);
    }

    public DefaultHttpPayload(Map<String, String> pathVariableValues, byte[] requestBody) {
        this(Collections.emptyList(), Collections.emptyList(), pathVariableValues, requestBody);
    }

    public DefaultHttpPayload(List<HttpHeader> headers, byte[] requestBody) {
        this(headers, Collections.emptyList(), Collections.emptyMap(), requestBody);
    }

    public DefaultHttpPayload(List<QueryParameter<?>> queryParameters, Map<String, String> pathVariableValues) {
        this(Collections.emptyList(), queryParameters, pathVariableValues, new byte[0]);
    }
}
