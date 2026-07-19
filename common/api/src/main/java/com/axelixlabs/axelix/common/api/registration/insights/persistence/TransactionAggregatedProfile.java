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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Aggregated information about a particular transactional method in the Instance.
 *
 * @author Mikhail Polivakha
 */
public class TransactionAggregatedProfile {

    private final TransactionOrigin transactionOrigin;
    private final TransactionalKey transactionalKey;
    private final TransactionOverallStats transactionOverallStats;
    private final List<CountedLazyLoadingTarget> lazyLoadingTargets;
    private final Map<String, Integer> inMemoryPagination;
    private final List<ExternalCallInsight> externalCalls;

    @JsonCreator
    public TransactionAggregatedProfile(
            @JsonProperty("transactionOrigin") TransactionOrigin transactionOrigin,
            @JsonProperty("transactionalKey") TransactionalKey transactionalKey,
            @JsonProperty("transactionOverallStats") TransactionOverallStats transactionOverallStats,
            @JsonProperty("lazyLoadingTargets") List<CountedLazyLoadingTarget> lazyLoadingTargets,
            @JsonProperty("inMemoryPagination") Map<String, Integer> inMemoryPagination,
            @JsonProperty("externalCalls") List<ExternalCallInsight> externalCalls) {
        this.transactionOrigin = transactionOrigin;
        this.transactionalKey = transactionalKey;
        this.transactionOverallStats = transactionOverallStats;
        this.lazyLoadingTargets = lazyLoadingTargets;
        this.inMemoryPagination = inMemoryPagination;
        this.externalCalls = externalCalls;
    }

    public TransactionOrigin getTransactionOrigin() {
        return transactionOrigin;
    }

    public TransactionalKey getTransactionalKey() {
        return transactionalKey;
    }

    public TransactionOverallStats getTransactionOverallStats() {
        return transactionOverallStats;
    }

    public List<CountedLazyLoadingTarget> getLazyLoadingTargets() {
        return lazyLoadingTargets;
    }

    public Map<String, Integer> getInMemoryPagination() {
        return inMemoryPagination;
    }

    public List<ExternalCallInsight> getExternalCalls() {
        return externalCalls;
    }
}
