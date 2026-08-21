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
     * The fully qualified name of the entity on which the association was lazy loaded. Deliberately kept as a
     * plain {@link String} rather than a {@link Class}: the class belongs to the monitored application and is not
     * present on the Axelix Master classpath, so deserializing it into a {@link Class} on the master side would fail.
     */
    private final String ownerEntityClass;

    /**
     * The association
     */
    private final String associationPropertyName;

    @JsonCreator
    public LazyLoadingTarget(
            @JsonProperty("ownerEntityClass") String ownerEntityClass,
            @JsonProperty("associationPropertyName") String associationPropertyName) {
        this.ownerEntityClass = ownerEntityClass;
        this.associationPropertyName = associationPropertyName;
    }

    public String getOwnerEntityClass() {
        return ownerEntityClass;
    }

    public String getAssociationPropertyName() {
        return associationPropertyName;
    }

    public String ownerEntityClass() {
        return ownerEntityClass;
    }

    public String associationPropertyName() {
        return associationPropertyName;
    }
}
