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
import { EAssociationProblem } from "@/models";

/**
 * The association problem types in the order they should be presented across the page.
 */
export const ASSOCIATION_PROBLEM_ORDER: EAssociationProblem[] = [
    EAssociationProblem.EAGER_FETCHING,
    EAssociationProblem.LIST_BACKED_MANY_TO_MANY,
    EAssociationProblem.CASCADE_REMOVE_OR_ALL,
    EAssociationProblem.UNIDIRECTIONAL_ONE_TO_MANY,
];

/**
 * The i18n key of the short label of an association problem type.
 */
export const associationProblemLabelKey: Record<EAssociationProblem, string> = {
    [EAssociationProblem.EAGER_FETCHING]: "EntitiesMap.problems.eager",
    [EAssociationProblem.LIST_BACKED_MANY_TO_MANY]: "EntitiesMap.problems.listManyToMany",
    [EAssociationProblem.CASCADE_REMOVE_OR_ALL]: "EntitiesMap.problems.cascade",
    [EAssociationProblem.UNIDIRECTIONAL_ONE_TO_MANY]: "EntitiesMap.problems.unidirectional",
};

/**
 * The i18n key of the "why this matters" explanation of an association problem type.
 */
export const associationProblemFixKey: Record<EAssociationProblem, string> = {
    [EAssociationProblem.EAGER_FETCHING]: "EntitiesMap.problemFixes.eager",
    [EAssociationProblem.LIST_BACKED_MANY_TO_MANY]: "EntitiesMap.problemFixes.listManyToMany",
    [EAssociationProblem.CASCADE_REMOVE_OR_ALL]: "EntitiesMap.problemFixes.cascade",
    [EAssociationProblem.UNIDIRECTIONAL_ONE_TO_MANY]: "EntitiesMap.problemFixes.unidirectional",
};

/**
 * The CSS-module class token (from the chip / detail styles) used to color an association problem type.
 */
export const associationProblemClassToken: Record<
    EAssociationProblem,
    "Eager" | "ListManyToMany" | "Cascade" | "Unidirectional"
> = {
    [EAssociationProblem.EAGER_FETCHING]: "Eager",
    [EAssociationProblem.LIST_BACKED_MANY_TO_MANY]: "ListManyToMany",
    [EAssociationProblem.CASCADE_REMOVE_OR_ALL]: "Cascade",
    [EAssociationProblem.UNIDIRECTIONAL_ONE_TO_MANY]: "Unidirectional",
};

/**
 * The accent color of an association problem type. Mirrors the {@code --problem-*} CSS variables and is used where
 * those variables are out of scope, e.g. inside a tooltip that is portaled outside the page wrapper.
 */
export const associationProblemColor: Record<EAssociationProblem, string> = {
    [EAssociationProblem.EAGER_FETCHING]: "#ea580c",
    [EAssociationProblem.LIST_BACKED_MANY_TO_MANY]: "#7c3aed",
    [EAssociationProblem.CASCADE_REMOVE_OR_ALL]: "#dc2626",
    [EAssociationProblem.UNIDIRECTIONAL_ONE_TO_MANY]: "#0891b2",
};
