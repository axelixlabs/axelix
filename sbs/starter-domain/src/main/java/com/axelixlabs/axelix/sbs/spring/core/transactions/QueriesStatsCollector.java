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
 * This interface defines the contract for collecting and retrieving SQL query monitoring data.
 *
 * @author Sergey Cherkasov
 */
public interface QueriesStatsCollector {

    /**
     * Records a query execution for statistics collection.
     *
     * @param transactionQueries the query execution record
     */
    void recordQueries(TransactionQueryRecord transactionQueries);

    /**
     * Returns all query statistics collected within the particular transaction.
     *
     * @return list ща query statistics
     */
    List<TransactionQueryRecord> getAllStats();

    /**
     * Clears all collected query statistics.
     */
    void clearAllStats();
}
