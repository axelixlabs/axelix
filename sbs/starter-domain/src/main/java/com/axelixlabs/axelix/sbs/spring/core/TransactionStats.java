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
package com.axelixlabs.axelix.sbs.spring.core;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.NonNull;

import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.LazyLoadingTarget;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionExecutionProfile;

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

    private Map<LazyLoadingTarget, Integer> nPlusOneOccasions;
    private Map<String, Integer> inMemoryPaginatedEntities;

    public void put(TransactionExecutionProfile transaction) {
        var incomingNPlusOneMap = new HashMap<LazyLoadingTarget, Integer>();

        for (TransactionExecutionProfile.AnalyzedSqlQueryRecord recordedQuery : transaction.getRecordedQueries()) {

            if (recordedQuery.getLazyLoadingTarget() != null) {

                incomingNPlusOneMap.compute(recordedQuery.getLazyLoadingTarget(), (target, records) -> {
                    if (records == null) {
                        records = 0;
                    }

                    return records + 1;
                });
            }

            if (recordedQuery.isInMemoryPaginated()) {
                int index = recordedQuery.getSql().indexOf(" from ");

                if (index == -1) {
                    index = recordedQuery.getSql().indexOf(" FROM ");
                }

                if (index == -1) {
                    // o_0, how can it be? warning and skip
                }
                String trimmedSql = recordedQuery.getSql().substring(index + 6).trim();
                int tailingWhitespace = trimmedSql.indexOf("\\s+"); // whitespace
                String selectionTarget = trimmedSql.substring(0, tailingWhitespace);

                inMemoryPaginatedEntities.compute(selectionTarget, (s, integer) -> {
                    if (integer == null) {
                        return 1;
                    }

                    return integer + 1;
                });
            }
        }

        incomingNPlusOneMap.forEach((lazyLoadingTarget, incomingCounter) -> {
            nPlusOneOccasions.compute(lazyLoadingTarget, (key, oldValue) -> {
                if (oldValue == null) {
                    return incomingCounter;
                }
                return Math.max(oldValue, incomingCounter);
            });
        });
    }

    private static @NonNull HashMap<LazyLoadingTarget, Integer> assembleNPlusOneMap(
            TransactionExecutionProfile transaction) {
        var result = new HashMap<LazyLoadingTarget, Integer>();

        for (TransactionExecutionProfile.AnalyzedSqlQueryRecord recordedQuery : transaction.getRecordedQueries()) {

            if (recordedQuery.getLazyLoadingTarget() != null) {
                result.compute(recordedQuery.getLazyLoadingTarget(), (target, records) -> {
                    if (records == null) {
                        records = 0;
                    }

                    return records + 1;
                });
            }
        }
        return result;
    }
}
