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

/**
 * A neutral category of a problem detected in a JPA association mapping while Axelix scanned the entities of an
 * instance. The categories are not ranked against each other, they merely describe the kind of mapping smell found.
 */
export enum EAssociationProblem {
    EAGER_FETCHING = "EAGER_FETCHING",
    LIST_BACKED_MANY_TO_MANY = "LIST_BACKED_MANY_TO_MANY",
    CASCADE_REMOVE_OR_ALL = "CASCADE_REMOVE_OR_ALL",
    UNIDIRECTIONAL_ONE_TO_MANY = "UNIDIRECTIONAL_ONE_TO_MANY",
    ACCESSED_IN_TO_STRING = "ACCESSED_IN_TO_STRING",
}
