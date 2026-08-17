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
import type { EAssociationProblem } from "@/models";

/**
 * A single JPA association, identified by the entity that declares it and the field that maps it.
 */
export interface IAssociation {
    /**
     * The entity that declares the association (e.g. {@code Order}).
     */
    entity: string;

    /**
     * The field that maps the association (e.g. {@code customer}).
     */
    field: string;
}

/**
 * A single association of a mapped entity in which at least one problem was detected.
 */
export interface IFlaggedAssociation {
    /**
     * The association in which the problems were detected.
     */
    association: IAssociation;

    /**
     * A human-readable rendering of the offending mapping (e.g. {@code @ManyToOne(fetch = FetchType.EAGER)}).
     */
    mapping: string;

    /**
     * The categories of the problems detected in the association.
     */
    problems: EAssociationProblem[];
}

/**
 * A single JPA entity that Axelix mapped while scanning an instance, together with the problems detected inside it.
 * An entity with an empty {@link IMappedEntity.flaggedAssociations} list is clean.
 */
export interface IMappedEntity {
    /**
     * The entity name (e.g. {@code Order}).
     */
    name: string;

    /**
     * The mapped table name (e.g. {@code orders}).
     */
    table: string;

    /**
     * The total number of associations mapped by the entity, or null when it could not be determined.
     */
    associationsCount: number | null;

    /**
     * The associations of this entity in which a problem was detected.
     */
    flaggedAssociations: IFlaggedAssociation[];
}

/**
 * The scan-first registry of every JPA entity Axelix mapped in an instance, together with the association-mapping
 * problems detected inside each.
 */
export interface IJpaEntities {
    /**
     * Every mapped entity, ordered by name.
     */
    entities: IMappedEntity[];
}
