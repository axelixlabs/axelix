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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The scan-first registry of every JPA entity Axelix mapped in an instance, together with the
 * association-mapping problems detected inside each. The number of entities analyzed is the size of
 * {@link #entities}; the problematic ones are those with a non-empty flagged-associations list.
 *
 * @author Mikhail Polivakha
 */
public class JpaEntities {

    private final List<MappedEntity> entities;

    @JsonCreator
    public JpaEntities(@JsonProperty("entities") List<MappedEntity> entities) {
        this.entities = entities;
    }

    public List<MappedEntity> getEntities() {
        return entities;
    }
}
