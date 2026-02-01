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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Single http header. Headers are potentially multivalued in http, and
 * therefore we have {@link List} of values here.
 *
 * @author Mikhail Polivakha
 */
public class HttpHeader {

    private final String name;
    private final List<String> values;

    public HttpHeader(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }

    public HttpHeader(String name, String... values) {
        this(name, Arrays.stream(values).collect(Collectors.toList()));
    }

    /**
     * @return produced the single {@link String} value for a given HTTP header.
     */
    public String valueAsString() {
        if (values.isEmpty()) {
            return "";
        }

        return String.join(", ", values);
    }

    public String name() {
        return name;
    }
}
