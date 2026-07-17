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
package com.axelixlabs.axelix.sbs.spring.core.persistence.transaction;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.LazyLoadingTarget;

/**
 * These are transactional stats that we have collected for the given transactional method
 * within the lifetime of the application.
 * <p>
 * There are a couple of very important nuances that we need to take seriously here. First of all, the
 * {@link TransactionStats#put(TransactionExecutionProfile)} is supposedly going to be relatively hot,
 * since the given transactional method will be executed quite often.
 *
 * @author Mikhail Polivakha
 */
@SuppressWarnings("NullAway")
public class TransactionStats {

    private final Map<LazyLoadingTarget, Integer> nPlusOneOccasions;
    private final Map<String, Integer> inMemoryPaginatedEntities;
    private final PerformanceStats performanceStats;

    public TransactionStats() {
        this.nPlusOneOccasions = new HashMap<>(1);
        this.inMemoryPaginatedEntities = new HashMap<>(1);
        this.performanceStats = new PerformanceStats();
    }

    public void put(TransactionExecutionProfile transaction) {
        updateProblemsProfilers(transaction);
        performanceStats.recordTransaction(transaction);
    }

    private void updateProblemsProfilers(TransactionExecutionProfile transaction) {
        var incomingNPlusOneMap = new HashMap<LazyLoadingTarget, Integer>();
        var incomingInMemoryPaginationMap = new HashMap<String, Integer>();

        for (var recordedQuery : transaction.getRecordedQueries()) {

            if (recordedQuery.getLazyLoadingTarget() != null) {
                incomingNPlusOneMap.compute(
                        recordedQuery.getLazyLoadingTarget(), (target, records) -> records == null ? 1 : records + 1);
            }

            if (recordedQuery.isInMemoryPaginated()) {
                String selectionTarget = parseSelectionTarget(recordedQuery);

                if (selectionTarget != null) {
                    incomingInMemoryPaginationMap.compute(
                            selectionTarget, (target, counter) -> counter == null ? 1 : counter + 1);
                }
            }
        }

        incomingNPlusOneMap.forEach((lazyLoadingTarget, incoming) -> {
            nPlusOneOccasions.compute(lazyLoadingTarget, (key, old) -> max(incoming, old));
        });

        incomingInMemoryPaginationMap.forEach((target, incoming) -> {
            inMemoryPaginatedEntities.compute(target, (s, old) -> max(incoming, old));
        });
    }

    private static int max(Integer incoming, @Nullable Integer old) {
        return old == null ? incoming : Math.max(old, incoming);
    }

    /**
     * Extracts the primary selection target (table / entity name) that follows the {@code FROM} clause.
     *
     * @return the first token after {@code FROM}, or {@code null} when the SQL has no recognizable {@code FROM} clause
     */
    private static @Nullable String parseSelectionTarget(
            TransactionExecutionProfile.AnalyzedSqlQueryRecord recordedQuery) {
        String sql = recordedQuery.getSql();
        int index = sql.indexOf(" from ");

        if (index == -1) {
            index = sql.indexOf(" FROM ");
        }

        if (index == -1) {
            return null;
        }

        String afterFrom = sql.substring(index + " from ".length()).trim();
        int whitespaceIndex = indexOfFirstWhitespace(afterFrom);

        if (whitespaceIndex == -1) {
            return afterFrom;
        }

        return afterFrom.substring(0, whitespaceIndex);
    }

    private static int indexOfFirstWhitespace(@NonNull String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    public PerformanceStats getPerformanceStats() {
        return performanceStats;
    }

    public Map<LazyLoadingTarget, Integer> getNPlusOneOccasions() {
        return nPlusOneOccasions;
    }

    public Map<String, Integer> getInMemoryPaginatedEntities() {
        return inMemoryPaginatedEntities;
    }
}
