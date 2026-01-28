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
package com.axelixlabs.axelix.common.domain.http;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The payload of the http requests, consisting of body and headers.
 *
 * @author Mikhail Polivakha
 */
public interface HttpPayload {

    /**
     * @return the ordered collection of {@link HttpHeader http headers}.
     */
    List<HttpHeader> headers();

    default boolean hasHeaders() {
        return !headers().isEmpty();
    }

    /**
     * @return the list of {@link QueryParameter query parameters}.
     */
    List<QueryParameter<?>> queryParameters();

    /**
     * @return map of path variable names to path variable values.
     */
    Map<String, String> pathVariableValues();

    /**
     * @return the serialized body of the http request.
     */
    byte[] requestBody();

    default boolean hasBody() {
        byte[] bytes = requestBody();

        return bytes.length != 0;
    }

    static HttpPayload json(byte[] requestBody) {
        HttpHeader contentType = new HttpHeader("Content-Type", "application/json");
        return new DefaultHttpPayload(
                List.of(contentType), Collections.emptyList(), Collections.emptyMap(), requestBody);
    }

    static HttpPayload json(Map<String, String> pathVariables, byte[] requestBody) {
        HttpHeader contentType = new HttpHeader("Content-Type", "application/json");
        return new DefaultHttpPayload(List.of(contentType), Collections.emptyList(), pathVariables, requestBody);
    }
}
