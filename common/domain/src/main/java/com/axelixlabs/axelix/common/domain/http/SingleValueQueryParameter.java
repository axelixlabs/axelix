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

/**
 * The most regular type of the parameter - single parameter http query
 * parameter, for instance {@code ?first=value} or {@code ?second=123}.
 *
 * @author Mikhail Polivakha
 */
public record SingleValueQueryParameter(String key, String value) implements QueryParameter<String> {

    @Override
    public String key() {
        return key;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String toEncodedString() {
        String encodedKey = QueryParameter.encodeUrlComponent(key());

        String encodedValue = QueryParameter.encodeUrlComponent(value());

        return encodedKey + "=" + encodedValue;
    }
}
