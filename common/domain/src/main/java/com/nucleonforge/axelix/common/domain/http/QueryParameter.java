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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * The HTTP query parameter.
 *
 * @param <T> the type of the parameter value
 * @author Mikhail Polivakha
 */
public sealed interface QueryParameter<T> permits SingleValueQueryParameter, MultiValueQueryParameter {

    /**
     * @return the key under which the parameter resides
     */
    String key();

    /**
     * @return the value of the query parameter
     */
    T value();

    /**
     * @return the encoded (URL reserved and unsafe characters are escaped) {@link String}
     * representation of {@link #value()}.
     */
    String toEncodedString();

    /**
     * Encode the given part of the URL.
     *
     * @param part part of the URL that needs to be encoded.
     * @return the encoded part of the URL.
     */
    static String encodeUrlComponent(String part) {
        return URLEncoder.encode(part, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
