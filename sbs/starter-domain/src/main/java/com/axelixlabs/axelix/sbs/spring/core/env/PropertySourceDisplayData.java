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
package com.axelixlabs.axelix.sbs.spring.core.env;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * DTO, used to decouple the raw Spring property source name from its user-friendly representation.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public final class PropertySourceDisplayData {
    private final String displayName;

    @Nullable
    private final String description;

    /**
     * Create a new PropertySourceDisplayData.
     *
     * @param displayName the Name of the property source.
     * @param description the custom description of this property source, if any.
     */
    public PropertySourceDisplayData(String displayName, @Nullable String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PropertySourceDisplayData that = (PropertySourceDisplayData) o;
        return Objects.equals(displayName, that.displayName) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, description);
    }

    @Override
    public String toString() {
        return "PropertySourceDisplayData{" + "displayName='"
                + displayName + '\'' + ", description='"
                + description + '\'' + '}';
    }
}
