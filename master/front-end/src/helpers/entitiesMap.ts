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
import { EAssociationProblem, type IMappedEntity } from "@/models";

export const ENTITY_CHECK_LABELS = new Map<string, string>([
    [EAssociationProblem.EAGER_FETCHING, "eager"],
    [EAssociationProblem.INEFFECTIVE_LAZY_ONE_TO_ONE, "ineffectiveLazyOneToOne"],
    [EAssociationProblem.LIST_BACKED_MANY_TO_MANY, "listManyToMany"],
    [EAssociationProblem.CASCADE_REMOVE_OR_ALL, "cascade"],
    [EAssociationProblem.UNIDIRECTIONAL_ONE_TO_MANY, "unidirectional"],
    [EAssociationProblem.ACCESSED_IN_TO_STRING, "toString"],
    [EAssociationProblem.ACCESSED_IN_EQUALS_OR_HASH_CODE, "equalsOrHashCode"],
    ["ORDINAL_ENUM", "ordinalEnum"],
]);

export type TEntityFindingLocation = "association" | "attribute";

export interface IEntityFinding {
    /** The check identifier reported by the starter or derived from its metadata. */
    check: string;
    /** The kind of member where the check found a problem. */
    location: TEntityFindingLocation;
    /** The name of the affected member. */
    member: string;
}

/**
 * A mapped entity and the findings currently shown for it.
 */
export interface IEntityResult {
    entity: IMappedEntity;
    findings: IEntityFinding[];
}

/**
 * Converts the available metadata into one finding per affected member and check.
 */
export const getEntityFindings = (entity: IMappedEntity): IEntityFinding[] => [
    ...(entity.ordinalEnumAttributes ?? []).map((member): IEntityFinding => ({
        check: "ORDINAL_ENUM",
        location: "attribute",
        member,
    })),
    ...entity.flaggedAssociations.flatMap(({ association, problems }) =>
        problems.map((check): IEntityFinding => ({
            check,
            location: "association",
            member: association.field,
        })),
    ),
];
