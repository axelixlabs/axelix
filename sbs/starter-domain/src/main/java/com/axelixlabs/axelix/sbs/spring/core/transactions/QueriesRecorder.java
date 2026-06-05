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
package com.axelixlabs.axelix.sbs.spring.core.transactions;

import java.util.List;

/**
 * Recorder of the SQL queries. It assumes that all sequentially related queries
 * are executed within a single Thread. Implementations must support nested transaction isolation levels
 * by treating query registration context as a stack hierarchy.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
public interface QueriesRecorder {

    /**
     * Initializes a new isolated context layer for query collection.
     * Must be called at the start of a transaction scope to protect parent transaction data from being overwritten.
     */
    void startNewContext();

    /**
     * Records a query execution for statistics collection within the current transaction scope.
     *
     * @param query the query execution record
     */
    void recordQuery(SqlQueryRecord query);

    /**
     * Pops all query statistics collected within the particular transaction
     * (i.e. the history of queries is removed from {@link QueriesRecorder})
     *
     * @return list ща query statistics
     */
    List<SqlQueryRecord> popAllRecords();
}
