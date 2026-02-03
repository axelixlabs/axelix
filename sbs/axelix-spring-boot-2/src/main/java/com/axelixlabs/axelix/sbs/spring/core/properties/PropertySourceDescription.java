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
package com.axelixlabs.axelix.sbs.spring.core.properties;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * Description of a given {@link org.springframework.core.env.PropertySource}.
 *
 * @since 04.07.25
 * @author Mikhail Polivakha
 */
public final class PropertySourceDescription {

    private final String name;
    private final PropertySourceOrigin origin;
    private final Class<?> clazz;

    @Nullable
    private final String fileName;

    public PropertySourceDescription(
            String name, PropertySourceOrigin origin, Class<?> clazz, @Nullable String fileName) {
        this.name = name;
        this.origin = origin;
        this.clazz = clazz;
        this.fileName = fileName;
    }

    public String name() {
        return name;
    }

    public PropertySourceOrigin origin() {
        return origin;
    }

    public Class<?> clazz() {
        return clazz;
    }

    @Nullable
    public String fileName() {
        return fileName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PropertySourceDescription) obj;
        return Objects.equals(this.name, that.name)
                && Objects.equals(this.origin, that.origin)
                && Objects.equals(this.clazz, that.clazz)
                && Objects.equals(this.fileName, that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, origin, clazz, fileName);
    }

    @Override
    public String toString() {
        return "PropertySourceDescription[" + "name="
                + name + ", " + "origin="
                + origin + ", " + "clazz="
                + clazz + ", " + "fileName="
                + fileName + ']';
    }
}
