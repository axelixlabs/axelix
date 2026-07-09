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

import java.util.concurrent.ConcurrentHashMap;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

import com.axelixlabs.axelix.sbs.spring.core.cache.CacheLookup;
import com.axelixlabs.axelix.sbs.spring.core.cache.EnhancedCache;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionExecutionProfile;

/**
 * Default implementation of {@link AxelixMetricsPublisher}
 *
 * @author Nikita Kirillov
 */
class DefaultAxelixMetricsPublisher implements AxelixMetricsPublisher {

    private final ConcurrentHashMap<String, Counter> cacheCounters = new ConcurrentHashMap<>();

    private final MeterRegistry meterRegistry;

    DefaultAxelixMetricsPublisher(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void publishTransactionMetrics(
            String className, String methodName, TransactionExecutionProfile transaction) {
        Timer.builder(AxelixMetricNames.TRANSACTION_DURATION)
                .description("Duration of transactions")
                .tag("class", className)
                .tag("method", methodName)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(transaction.getTransactionDuration());

        Counter.builder(AxelixMetricNames.TRANSACTION_QUERIES)
                .description("Total number of SQL queries executed inside transactions")
                .tag("class", className)
                .tag("method", methodName)
                .register(meterRegistry)
                .increment(transaction.getQueriesCount());
    }

    @Override
    public void incrementCacheLookup(String cacheName, CacheLookup.Outcome outcome) {
        String outcomeValue = outcome.getValue();
        String counterKey = cacheName + ":" + outcomeValue;

        cacheCounters
                .computeIfAbsent(
                        counterKey,
                        key -> Counter.builder(AxelixMetricNames.CACHE_REQUESTS)
                                .description("Total number of cache lookups")
                                .tag("cache", cacheName)
                                .tag("result", outcomeValue)
                                .register(meterRegistry))
                .increment();
    }

    @Override
    public void registerCacheStatusGauge(EnhancedCache enhancedCache) {
        Tags cacheTags = Tags.of("cache", enhancedCache.getName());

        meterRegistry.gauge(AxelixMetricNames.CACHE_ENABLED, cacheTags, enhancedCache, c -> c.isEnabled() ? 1.0 : 0.0);
    }
}
