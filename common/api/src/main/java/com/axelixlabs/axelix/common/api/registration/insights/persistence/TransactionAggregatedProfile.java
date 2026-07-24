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
import org.jspecify.annotations.Nullable;

/**
 * Aggregated information about a particular transactional method in the Instance.
 *
 * @author Mikhail Polivakha
 */
public class TransactionAggregatedProfile {

    private final TransactionOrigin transactionOrigin;
    private final TransactionalKey transactionalKey;
    private final ExecutionStats transactionOverallStats;
    private final List<CountedLazyLoadingTarget> lazyLoadingTargets;
    private final Map<String, Integer> inMemoryPagination;
    private final List<ExternalCallInsight> externalCalls;

    /**
     * The declared propagation behavior of the transaction (e.g. {@code REQUIRED}), or {@code null} when it could
     * not be determined. Reflects the value declared on the {@code @Transactional} annotation.
     */
    private final @Nullable String propagation;

    /**
     * The declared isolation level of the transaction (e.g. {@code DEFAULT}), or {@code null} when it could not be
     * determined. Reflects the value declared on the {@code @Transactional} annotation.
     */
    private final @Nullable String isolation;

    /**
     * Whether the transaction was declared read-only, or {@code null} when it could not be determined (e.g. an
     * older agent that did not report it).
     */
    private final @Nullable Boolean readOnly;

    @JsonCreator
    public TransactionAggregatedProfile(
            @JsonProperty("transactionOrigin") TransactionOrigin transactionOrigin,
            @JsonProperty("transactionalKey") TransactionalKey transactionalKey,
            @JsonProperty("transactionOverallStats") ExecutionStats transactionOverallStats,
            @JsonProperty("lazyLoadingTargets") List<CountedLazyLoadingTarget> lazyLoadingTargets,
            @JsonProperty("inMemoryPagination") Map<String, Integer> inMemoryPagination,
            @JsonProperty("externalCalls") List<ExternalCallInsight> externalCalls,
            @JsonProperty("propagation") @Nullable String propagation,
            @JsonProperty("isolation") @Nullable String isolation,
            @JsonProperty("readOnly") @Nullable Boolean readOnly) {
        this.transactionOrigin = transactionOrigin;
        this.transactionalKey = transactionalKey;
        this.transactionOverallStats = transactionOverallStats;
        this.lazyLoadingTargets = lazyLoadingTargets;
        this.inMemoryPagination = inMemoryPagination;
        this.externalCalls = externalCalls;
        this.propagation = propagation;
        this.isolation = isolation;
        this.readOnly = readOnly;
    }

    public TransactionOrigin getTransactionOrigin() {
        return transactionOrigin;
    }

    public TransactionalKey getTransactionalKey() {
        return transactionalKey;
    }

    public ExecutionStats getTransactionOverallStats() {
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

    public @Nullable String getPropagation() {
        return propagation;
    }

    public @Nullable String getIsolation() {
        return isolation;
    }

    public @Nullable Boolean isReadOnly() {
        return readOnly;
    }
}
