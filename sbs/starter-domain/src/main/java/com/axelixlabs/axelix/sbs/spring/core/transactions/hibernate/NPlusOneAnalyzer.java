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
package com.axelixlabs.axelix.sbs.spring.core.transactions.hibernate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.axelixlabs.axelix.sbs.spring.core.transactions.QueriesRecorder;
import com.axelixlabs.axelix.sbs.spring.core.transactions.SqlQueryRecord;
import com.axelixlabs.axelix.sbs.spring.core.transactions.hibernate.NPlusOneHolder.LoadSnapshot;

/**
 * Analyzes transaction execution data to detect N+1 and Batch+1 fetch patterns.
 *
 * @author Nikita Kirillov
 */
public class NPlusOneAnalyzer {

    /**
     * @param context completed transaction context from {@link NPlusOneHolder}
     * @param queries SQL records from {@link QueriesRecorder} for the same transaction
     */
    public void analyzeAndMarkNPlusOneQueries(NPlusOneHolder.NPlusOneContext context, List<SqlQueryRecord> queries) {
        // Analyze Collections
        analyzeSnapshot(context.getCollectionLoadsSnapshot(), queries);
        // Analyze Entities
        analyzeSnapshot(context.getEntityLoadsSnapshot(), queries);
    }

    private void analyzeSnapshot(LoadSnapshot snapshot, List<SqlQueryRecord> queries) {
        // Analyze N+1
        markQueries(snapshot.getStandardLoads(), queries, SqlQueryRecord::markAsNPlusOne);
        // Analyze Batch+1
        markQueries(snapshot.getBatchLoads(), queries, SqlQueryRecord::markAsBatchPlusOne);
    }

    private void markQueries(
            Map<String, Map<Object, Integer>> loads, List<SqlQueryRecord> queries, Consumer<SqlQueryRecord> marker) {
        loads.forEach((role, idToSqlIndex) -> {

            // Deduplicate SQL execution indices to prevent false-positives during batch fetching.
            // A single batch query may initialize multiple entity IDs under the exact same SQL index.
            List<Integer> distinctSqlIndices =
                    idToSqlIndex.values().stream().distinct().collect(Collectors.toList());

            // If the same role/entity was fetched across multiple distinct database requests,
            // it strictly indicates an N+1 or Batch+1 defect.
            if (distinctSqlIndices.size() > 1) {
                distinctSqlIndices.forEach(sqlIndex -> queries.stream()
                        .filter(query -> Objects.equals(query.getSqlIndex(), sqlIndex))
                        .findFirst()
                        .ifPresent(marker));
            }
        });
    }
}
