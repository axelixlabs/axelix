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
import org.jspecify.annotations.Nullable;

/**
 * A single JPA entity that Axelix mapped while scanning an instance, together with the association
 * problems detected inside it. An entity with an empty {@link #flaggedAssociations} list is clean.
 *
 * @author Mikhail Polivakha
 */
public class MappedEntity {

    /**
     * The entity name (e.g. {@code Order}).
     */
    private final String name;

    /**
     * The mapped table name (e.g. {@code orders}).
     */
    private final String table;

    /**
     * The total number of associations mapped by the entity, or {@code null} when it could not be
     * determined (e.g. an older agent that did not report it).
     */
    private final @Nullable Integer associationsCount;

    /**
     * The associations of this entity in which a problem was detected.
     */
    private final List<FlaggedAssociation> flaggedAssociations;

    @JsonCreator
    public MappedEntity(
            @JsonProperty("name") String name,
            @JsonProperty("table") String table,
            @JsonProperty("associationsCount") @Nullable Integer associationsCount,
            @JsonProperty("flaggedAssociations") List<FlaggedAssociation> flaggedAssociations) {
        this.name = name;
        this.table = table;
        this.associationsCount = associationsCount;
        this.flaggedAssociations = flaggedAssociations;
    }

    public String getName() {
        return name;
    }

    public String getTable() {
        return table;
    }

    public @Nullable Integer getAssociationsCount() {
        return associationsCount;
    }

    public List<FlaggedAssociation> getFlaggedAssociations() {
        return flaggedAssociations;
    }
}
