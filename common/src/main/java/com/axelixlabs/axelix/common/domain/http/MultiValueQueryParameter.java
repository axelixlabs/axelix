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

import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@link QueryParameter} that can have multiple values. Renders to a
 * comma separated values string, e.g. {@code key=value1,value2,value3}. The correct
 * way to pass multivalued params in HTTP is not defined in any kind of RFC standard,
 * so we're just choosing this option.
 *
 * @author Mikhail Polivakha
 */
public class MultiValueQueryParameter implements QueryParameter<List<String>> {

    private final String key;
    private final List<String> values;

    public MultiValueQueryParameter(String key, List<String> values) {
        this.key = key;
        this.values = values;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public List<String> value() {
        return values;
    }

    @Override
    public String toEncodedString() {
        String encodedKey = QueryParameter.encodeUrlComponent(key());

        String encodedValue =
                value().stream().map(QueryParameter::encodeUrlComponent).collect(Collectors.joining(","));

        return encodedKey + "=" + encodedValue;
    }
}
