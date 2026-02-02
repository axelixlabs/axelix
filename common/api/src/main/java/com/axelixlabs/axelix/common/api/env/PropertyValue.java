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
package com.axelixlabs.axelix.common.api.env;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * The value of a property within a property source.
 *
 * @see EnvironmentProperty
 * @since 03.09.2025
 * @author Nikita Kirillov
 */
public final class PropertyValue {

    private final String value;

    @Nullable
    private final String origin;

    /**
     * Creates a new PropertyValue.
     *
     * @param value  the string representation of the property's value
     * @param origin the origin of the property if available (e.g. location in a file), may be {@code null}
     */
    @JsonCreator
    public PropertyValue(@JsonProperty("value") String value, @JsonProperty("origin") @Nullable String origin) {
        this.value = value;
        this.origin = origin;
    }

    public String value() {
        return value;
    }

    @Nullable
    public String origin() {
        return origin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PropertyValue that = (PropertyValue) o;
        return Objects.equals(value, that.value) && Objects.equals(origin, that.origin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, origin);
    }

    @Override
    public String toString() {
        return "PropertyValue{" + "value='" + value + '\'' + ", origin='" + origin + '\'' + '}';
    }
}
