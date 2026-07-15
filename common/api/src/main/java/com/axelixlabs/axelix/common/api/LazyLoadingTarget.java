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
package com.axelixlabs.axelix.common.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The particular association that presumably was lazy loaded.
 *
 * @author Mikhail Polivakha
 */
public class LazyLoadingTarget {

    /**
     * The entity on which the assassination was lazy loaded
     */
    private final Class<?> ownerEntityClass;

    /**
     * The association
     */
    private final String associationPropertyName;

    @JsonCreator
    public LazyLoadingTarget(
            @JsonProperty("ownerEntityClass") Class<?> ownerEntityClass,
            @JsonProperty("associationPropertyName") String associationPropertyName) {
        this.ownerEntityClass = ownerEntityClass;
        this.associationPropertyName = associationPropertyName;
    }

    public Class<?> getOwnerEntityClass() {
        return ownerEntityClass;
    }

    public String getAssociationPropertyName() {
        return associationPropertyName;
    }

    public Class<?> ownerEntityClass() {
        return ownerEntityClass;
    }

    public String associationPropertyName() {
        return associationPropertyName;
    }
}
