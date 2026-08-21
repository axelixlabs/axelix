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
package com.axelixlabs.axelix.common.api.registration.insights.persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single JPA association, identified by the entity that declares it and the field that maps it.
 *
 * @author Mikhail Polivakha
 */
public class Association {

    /**
     * The entity that declares the association (e.g. {@code Order}).
     */
    private final String entity;

    /**
     * The field that maps the association (e.g. {@code customer}).
     */
    private final String field;

    @JsonCreator
    public Association(@JsonProperty("entity") String entity, @JsonProperty("field") String field) {
        this.entity = entity;
        this.field = field;
    }

    public String getEntity() {
        return entity;
    }

    public String getField() {
        return field;
    }
}
