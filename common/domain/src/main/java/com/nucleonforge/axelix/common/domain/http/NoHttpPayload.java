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

import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link HttpPayload} that does not add any
 * additional headers into the HTTP request and also that does not
 * provide any request body.
 *
 * @author Mikhail Polivakha
 */
public class NoHttpPayload implements HttpPayload {

    public static NoHttpPayload INSTANCE = new NoHttpPayload();

    private NoHttpPayload() {}

    @Override
    public List<HttpHeader> headers() {
        return List.of();
    }

    @Override
    public List<QueryParameter<?>> queryParameters() {
        return List.of();
    }

    @Override
    public Map<String, String> pathVariableValues() {
        return Map.of();
    }

    @Override
    public byte[] requestBody() {
        return new byte[0];
    }
}
