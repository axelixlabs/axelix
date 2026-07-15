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
package com.axelixlabs.axelix.common.api.registration.insights.persistence;

import java.util.Map;

import com.axelixlabs.axelix.common.api.LazyLoadingTarget;

/**
 * Aggregated information about a particular transactional method in the Instance.
 *
 * @author Mikhail Polivakha
 */
public class TransactionAggregatedProfile {

    private final TransactionOrigin transactionOrigin;
    private final TransactionalKey transactionalKey;
    private final TransactionQueriesStats transactionQueriesStats;
    private final Map<LazyLoadingTarget, Integer> lazyLoadingTarget;
    private final Map<String, Integer> inMemoryPagination;

    public TransactionAggregatedProfile(
            TransactionOrigin transactionOrigin,
            TransactionalKey transactionalKey,
            TransactionQueriesStats transactionQueriesStats,
            Map<LazyLoadingTarget, Integer> lazyLoadingTarget,
            Map<String, Integer> inMemoryPagination) {
        this.transactionOrigin = transactionOrigin;
        this.transactionalKey = transactionalKey;
        this.transactionQueriesStats = transactionQueriesStats;
        this.lazyLoadingTarget = lazyLoadingTarget;
        this.inMemoryPagination = inMemoryPagination;
    }

    public TransactionOrigin getTransactionOrigin() {
        return transactionOrigin;
    }

    public TransactionalKey getTransactionalKey() {
        return transactionalKey;
    }

    public TransactionQueriesStats getTransactionQueriesStats() {
        return transactionQueriesStats;
    }

    public Map<LazyLoadingTarget, Integer> getLazyLoadingTarget() {
        return lazyLoadingTarget;
    }

    public Map<String, Integer> getInMemoryPagination() {
        return inMemoryPagination;
    }
}
