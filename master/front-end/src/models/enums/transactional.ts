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
 * How a transaction was created inside the monitored application.
 */
export enum ETransactionOrigin {
    SPRING_INFRASTRUCTURE = "SPRING_INFRASTRUCTURE",
    APPLICATION_DECLARATIVE = "APPLICATION_DECLARATIVE",
    APPLICATION_IMPERATIVE = "APPLICATION_IMPERATIVE",
}

/**
 * The client that performed a blocking external call from within a transaction.
 */
export enum ETypeExternalCall {
    HTTP_CLIENT = "HTTP_CLIENT",
    KAFKA = "KAFKA",
    RABBIT = "RABBIT",
}

/**
 * The kind of persistence problem detected inside a transaction. This is a front-end classification derived from
 * the raw insights (external calls, lazy-loading targets and in-memory paginated queries).
 */
export enum EProblemType {
    BLOCKING = "BLOCKING",
    N_PLUS_ONE = "N_PLUS_ONE",
    IN_MEMORY_PAGINATION = "IN_MEMORY_PAGINATION",
}
