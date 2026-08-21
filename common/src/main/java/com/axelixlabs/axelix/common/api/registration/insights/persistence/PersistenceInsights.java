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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * Insights of a particular Instance related to persistence.
 *
 * @author Mikhail Polivakha
 */
public class PersistenceInsights {

    /**
     * The aggregated information about the transactions inside the Instance.
     */
    private final List<TransactionAggregatedProfile> transactions;

    /**
     * The registry of JPA entities Axelix mapped in the Instance and the problems detected in their
     * associations, or {@code null} when the Instance did not report it (e.g. no JPA on the classpath,
     * or an older agent).
     */
    private final @Nullable JpaEntities jpaEntities;

    /**
     * Creates persistence insights without an entities map. Kept for callers (mostly tests) that only
     * care about the transactional insights.
     *
     * @param transactions the aggregated transactional insights.
     */
    public PersistenceInsights(List<TransactionAggregatedProfile> transactions) {
        this(transactions, null);
    }

    @JsonCreator
    public PersistenceInsights(
            @JsonProperty("transactions") List<TransactionAggregatedProfile> transactions,
            @JsonProperty("entitiesMap") @Nullable JpaEntities jpaEntities) {
        this.transactions = transactions;
        this.jpaEntities = jpaEntities;
    }

    public List<TransactionAggregatedProfile> getTransactions() {
        return transactions;
    }

    public @Nullable JpaEntities getEntitiesMap() {
        return jpaEntities;
    }
}
