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
import { EProblemType, type IDetectedProblem, type ITransactionAggregatedProfile } from "models";

/**
 * Extracts the simple (unqualified) name from a fully qualified class name.
 */
export const simpleClassName = (fullyQualifiedName: string): string => {
    const parts = fullyQualifiedName.split(".");
    return parts[parts.length - 1] || fullyQualifiedName;
};

/**
 * Normalizes the raw insights of a transaction into a flat list of detected problems, ready for display.
 *
 * <p>An association counts as an N+1 problem only when it was lazily loaded more than once ({@code count > 1}),
 * mirroring how the Axelix Master itself defines the problem.
 */
export const deriveProblems = (transaction: ITransactionAggregatedProfile): IDetectedProblem[] => {
    const problems: IDetectedProblem[] = [];

    for (const call of transaction.externalCalls ?? []) {
        problems.push({ type: EProblemType.BLOCKING, detail: call.target });
    }

    for (const lazyLoading of transaction.lazyLoadingTargets ?? []) {
        if (lazyLoading.count > 1) {
            problems.push({
                type: EProblemType.N_PLUS_ONE,
                detail: `${simpleClassName(lazyLoading.target.ownerEntityClass)}.${lazyLoading.target.associationPropertyName}`,
                count: lazyLoading.count,
            });
        }
    }

    for (const [entity, count] of Object.entries(transaction.inMemoryPagination ?? {})) {
        problems.push({ type: EProblemType.IN_MEMORY_PAGINATION, detail: simpleClassName(entity), count });
    }

    return problems;
};

/**
 * Whether the transaction has at least one detected problem.
 */
export const isProblematic = (transaction: ITransactionAggregatedProfile): boolean => {
    return deriveProblems(transaction).length > 0;
};

/**
 * Filters transactions by a free-text search over {@code class#method} and, when at least one problem type is
 * active, keeps only the transactions that exhibit a problem of one of those types.
 */
export const filterTransactions = (
    transactions: ITransactionAggregatedProfile[],
    search: string,
    activeProblemTypes: EProblemType[],
): ITransactionAggregatedProfile[] => {
    const formattedSearch = search.toLowerCase().trim();

    return transactions.filter((transaction) => {
        const { className, methodName } = transaction.transactionalKey;
        const signature = `${className}#${methodName}`.toLowerCase();

        if (formattedSearch && !signature.includes(formattedSearch)) {
            return false;
        }

        if (activeProblemTypes.length > 0) {
            const problemTypes = deriveProblems(transaction).map((problem) => problem.type);
            return activeProblemTypes.some((type) => problemTypes.includes(type));
        }

        return true;
    });
};

const formatTransactionalDuration = (value: number): string => {
    const seconds = value / 1000;
    const formatted = Number.isInteger(seconds) ? seconds.toString() : seconds.toFixed(1);

    return `${formatted}s`;
};

export const formatTransactionDuration = (value: number) => {
    return value < 1000 ? `${value} ms` : formatTransactionalDuration(value);
};
