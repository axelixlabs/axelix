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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Interface responsible for publishing application-level metrics to the underlying metrics system.
 *
 * @author Nikita Kirillov
 */
public interface AxelixMetricsPublisher {

    /**
     * Publishes aggregated transaction statistics.
     *
     * @param className    the simple name of the target class executing the transaction
     * @param methodName   the name of the target method executing the transaction
     * @param durationNano transaction execution duration in nanoseconds
     * @param status       the final outcome status of the transaction (e.g., "success" or "error")
     * @param queryCount   the total number of SQL queries executed inside the transaction scope
     */
    void publishTransactionMetrics(
            String className, String methodName, long durationNano, String status, int queryCount);

    /**
     * Increments the cache lookup counter based on the operation result type.
     *
     * @param cacheName  the name of the cache being queried
     * @param outcome    the outcome of the cache lookup (strictly "hit" or "miss")
     */
    void incrementCacheLookup(String cacheName, String outcome);

    /**
     * Registers a gauge to dynamically monitor the current cache execution status (enabled or disabled).
     *
     * @param cacheName   the name of the cache to monitor
     * @param enabledFlag the thread-safe flag indicating whether the cache is currently enabled
     */
    void registerCacheStatusGauge(String cacheName, AtomicBoolean enabledFlag);
}
