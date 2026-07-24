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
package com.axelixlabs.axelix.sbs.spring.core.metrics;

import com.axelixlabs.axelix.sbs.spring.core.cache.CacheLookup;
import com.axelixlabs.axelix.sbs.spring.core.cache.EnhancedCache;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionExecutionProfile;

/**
 * Interface responsible for publishing application-level metrics to the underlying metrics system.
 *
 * @author Nikita Kirillov
 */
public interface AxelixMetricsPublisher {

    /**
     * Publishes aggregated transaction statistics.
     *
     * @param className   the simple name of the target class executing the transaction
     * @param methodName  the name of the target method executing the transaction
     * @param transaction executed transaction for which the metrics are supposed to be published.
     */
    void publishTransactionMetrics(String className, String methodName, TransactionExecutionProfile transaction);

    /**
     * Increments the cache lookup counter based on the operation result type.
     *
     * @param cacheName  the name of the cache being queried
     * @param outcome    the outcome of the cache lookup (strictly "hit" or "miss")
     */
    void incrementCacheLookup(String cacheName, CacheLookup.Outcome outcome);

    /**
     * Registers a gauge to dynamically monitor the current cache execution status (enabled or disabled).
     *
     * @see   EnhancedCache
     * @param enhancedCache the enhanced cache instance to monitor
     */
    void registerCacheStatusGauge(EnhancedCache enhancedCache);
}
