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
import type { EAssociationProblem, IMappedEntity } from "@/models";
import { ASSOCIATION_PROBLEM_ORDER } from "@/utils";

/**
 * A problem type together with how many of the entity's associations exhibit it.
 */
export interface IGroupedAssociationProblem {
    /**
     * The problem type.
     */
    type: EAssociationProblem;

    /**
     * How many of the entity's flagged associations exhibit the problem.
     */
    count: number;
}

/**
 * Whether the entity has at least one flagged association.
 */
export const isEntityProblematic = (entity: IMappedEntity): boolean => {
    return entity.flaggedAssociations.length > 0;
};

/**
 * Groups the problems of an entity by type, counting how many associations exhibit each, in the canonical order.
 */
export const groupEntityProblems = (entity: IMappedEntity): IGroupedAssociationProblem[] => {
    const counts = new Map<EAssociationProblem, number>();

    for (const flagged of entity.flaggedAssociations) {
        for (const problem of flagged.problems) {
            counts.set(problem, (counts.get(problem) ?? 0) + 1);
        }
    }

    return ASSOCIATION_PROBLEM_ORDER.filter((type) => counts.has(type)).map((type) => ({
        type,
        count: counts.get(type) ?? 0,
    }));
};

/**
 * Reorders the problems of a single association into the canonical presentation order.
 */
export const orderAssociationProblems = (problems: EAssociationProblem[]): EAssociationProblem[] => {
    return ASSOCIATION_PROBLEM_ORDER.filter((type) => problems.includes(type));
};

/**
 * Filters entities by a free-text search over the entity name and its flagged {@code Entity.field} associations and,
 * when at least one problem type is active, keeps only the entities that exhibit a problem of one of those types.
 */
export const filterEntities = (
    entities: IMappedEntity[],
    search: string,
    activeProblemTypes: EAssociationProblem[],
): IMappedEntity[] => {
    const formattedSearch = search.toLowerCase().trim();

    return entities.filter((entity) => {
        if (formattedSearch) {
            const matchesName = entity.name.toLowerCase().includes(formattedSearch);

            if (!matchesName) {
                return false;
            }
        }

        if (activeProblemTypes.length > 0) {
            const entityProblemTypes = entity.flaggedAssociations.flatMap((flagged) => flagged.problems);
            return activeProblemTypes.every((type) => entityProblemTypes.includes(type));
        }

        return true;
    });
};
