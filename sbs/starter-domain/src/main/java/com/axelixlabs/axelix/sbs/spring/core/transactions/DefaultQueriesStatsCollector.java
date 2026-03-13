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

import java.util.ArrayList;
import java.util.List;

/**
 * Service providing access to queries in transaction monitoring data and statistics.
 *
 * @author Sergey Cherkasov
 */
public final class DefaultQueriesStatsCollector implements QueriesStatsCollector {

    private final ThreadLocal<List<TransactionQueryRecord>> threadLocal = ThreadLocal.withInitial(ArrayList::new);

    @Override
    public void recordQueries(TransactionQueryRecord transactionQueries) {
        threadLocal.get().add(transactionQueries);
    }

    @Override
    public List<TransactionQueryRecord> getAllStats() {
        List<TransactionQueryRecord> queries = new ArrayList<>(threadLocal.get());
        threadLocal.remove();
        return queries;
    }

    @Override
    public void clearAllStats() {
        threadLocal.remove();
    }
}
