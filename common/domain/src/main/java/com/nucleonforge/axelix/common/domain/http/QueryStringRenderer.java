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
import java.util.stream.Collectors;

/**
 * Utility class for working with HTTP query parameters.
 *
 * @author Mikhail Polivakha
 */
public class QueryStringRenderer {

    /**
     * Renders the array of {@link QueryParameter query paramters} to the query parameter string.
     *
     * @return the rendered query parameters string, e.g. {@code ?key=v1&key2=v2}
     */
    public static String renderQueryString(List<QueryParameter<?>> queryParameters) {
        if (queryParameters.isEmpty()) {
            return "";
        }

        return queryParameters.stream().map(QueryParameter::toEncodedString).collect(Collectors.joining("&", "?", ""));
    }
}
