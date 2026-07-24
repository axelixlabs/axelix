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
import type { EProblemType, ETransactionOrigin, ETypeExternalCall } from "models";

/**
 * Min / max / average timing aggregated across every invocation.
 */
export interface IExecutionStats {
    /**
     * The minimum duration in milliseconds.
     */
    minMs: number;

    /**
     * The maximum duration in milliseconds.
     */
    maxMs: number;

    /**
     * The average duration in milliseconds.
     */
    averageMs: number;
}

/**
 * Identifies the transactional method.
 */
export interface ITransactionalKey {
    /**
     * The fully qualified class name declaring the transactional method.
     */
    className: string;

    /**
     * The transactional method name.
     */
    methodName: string;
}

/**
 * The association that was lazily loaded.
 */
export interface ILazyLoadingTarget {
    /**
     * The fully qualified name of the entity owning the lazily loaded association.
     */
    ownerEntityClass: string;

    /**
     * The lazily loaded association property name.
     */
    associationPropertyName: string;
}

/**
 * A lazy-loading (N+1) occasion together with how many times it fired.
 */
export interface ICountedLazyLoadingTarget {
    /**
     * The lazily loaded association.
     */
    target: ILazyLoadingTarget;

    /**
     * How many times the association was lazily loaded.
     */
    count: number;
}

/**
 * A blocking external call made from within a transaction, folded across every invocation.
 */
export interface IExternalCallInsight {
    /**
     * The client that performed the call.
     */
    type: ETypeExternalCall;

    /**
     * Where the call went (request method and url, or topic / queue / exchange).
     */
    target: string;

    /**
     * The min / max / average call duration.
     */
    stats: IExecutionStats;
}

/**
 * Aggregated information about a single transactional method.
 */
export interface ITransactionAggregatedProfile {
    /**
     * How the transaction was created.
     */
    transactionOrigin: ETransactionOrigin;

    /**
     * The transactional method identity.
     */
    transactionalKey: ITransactionalKey;

    /**
     * The overall duration statistics of the transaction.
     */
    transactionOverallStats: IExecutionStats;

    /**
     * The N+1 lazy-loading occasions detected inside the transaction.
     */
    lazyLoadingTargets: ICountedLazyLoadingTarget[];

    /**
     * The in-memory paginated queries: entity / table name mapped to how many times it happened.
     */
    inMemoryPagination: Record<string, number>;

    /**
     * The blocking external calls performed while the transaction was open.
     */
    externalCalls: IExternalCallInsight[];

    /**
     * The declared propagation behavior (e.g. {@code REQUIRED}), or null when it could not be determined.
     */
    propagation: string | null;

    /**
     * The declared isolation level (e.g. {@code DEFAULT}), or null when it could not be determined.
     */
    isolation: string | null;

    /**
     * Whether the transaction was declared read-only, or null when it could not be determined.
     */
    readOnly: boolean | null;
}

/**
 * Response of the transaction monitoring endpoint.
 */
export interface IPersistenceInsights {
    /**
     * The aggregated profiles of every monitored transactional method.
     */
    transactions: ITransactionAggregatedProfile[];
}

/**
 * A single persistence problem detected inside a transaction, normalized for display. Derived on the front-end
 * from {@link ITransactionAggregatedProfile}.
 */
export interface IDetectedProblem {
    /**
     * The kind of problem.
     */
    type: EProblemType;

    /**
     * A human-readable description of the concrete occurrence (endpoint, association or paginated entity).
     */
    detail: string;

    /**
     * How many times the problem was observed, when applicable.
     */
    count?: number;
}
