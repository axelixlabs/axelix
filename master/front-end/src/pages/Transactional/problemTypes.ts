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
import { EProblemType, ETransactionOrigin } from "models";

/**
 * The problem types in the order they should be presented across the page.
 */
export const PROBLEM_TYPE_ORDER: EProblemType[] = [
    EProblemType.BLOCKING,
    EProblemType.N_PLUS_ONE,
    EProblemType.IN_MEMORY_PAGINATION,
];

/**
 * The i18n key of the short label of a problem type.
 */
export const problemLabelKey: Record<EProblemType, string> = {
    [EProblemType.BLOCKING]: "Transactional.problems.blocking",
    [EProblemType.N_PLUS_ONE]: "Transactional.problems.nPlusOne",
    [EProblemType.IN_MEMORY_PAGINATION]: "Transactional.problems.inMemoryPagination",
};

/**
 * The i18n key of the "why this matters" description of a problem type.
 */
export const problemDescriptionKey: Record<EProblemType, string> = {
    [EProblemType.BLOCKING]: "Transactional.problemDescriptions.blocking",
    [EProblemType.N_PLUS_ONE]: "Transactional.problemDescriptions.nPlusOne",
    [EProblemType.IN_MEMORY_PAGINATION]: "Transactional.problemDescriptions.inMemoryPagination",
};

/**
 * The i18n key of the label describing how a transaction was created.
 */
export const originLabelKey: Record<ETransactionOrigin, string> = {
    [ETransactionOrigin.SPRING_INFRASTRUCTURE]: "Transactional.origin.SPRING_INFRASTRUCTURE",
    [ETransactionOrigin.APPLICATION_DECLARATIVE]: "Transactional.origin.APPLICATION_DECLARATIVE",
    [ETransactionOrigin.APPLICATION_IMPERATIVE]: "Transactional.origin.APPLICATION_IMPERATIVE",
};

/**
 * The CSS-module class token (from problemChip / detail styles) used to color a problem type.
 */
export const problemClassToken: Record<EProblemType, "Blocking" | "NPlusOne" | "Pagination"> = {
    [EProblemType.BLOCKING]: "Blocking",
    [EProblemType.N_PLUS_ONE]: "NPlusOne",
    [EProblemType.IN_MEMORY_PAGINATION]: "Pagination",
};
