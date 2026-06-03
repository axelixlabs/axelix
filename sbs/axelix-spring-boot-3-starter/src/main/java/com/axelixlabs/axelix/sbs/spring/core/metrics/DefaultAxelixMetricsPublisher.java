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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

/**
 * Default implementation of {@link AxelixMetricsPublisher}
 *
 * @author Nikita Kirillov
 */
public class DefaultAxelixMetricsPublisher implements AxelixMetricsPublisher {

    private final ConcurrentHashMap<String, Counter> cacheCounters = new ConcurrentHashMap<>();

    private final MeterRegistry meterRegistry;

    public DefaultAxelixMetricsPublisher(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void publishTransactionMetrics(
            String className, String methodName, long durationNano, String status, int queryCount) {
        Timer.builder(AxelixMetricNames.TRANSACTION_DURATION)
                .description("Duration of transactions")
                .tag("class", className)
                .tag("method", methodName)
                .tag("status", status)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationNano, TimeUnit.NANOSECONDS);

        Counter.builder(AxelixMetricNames.TRANSACTION_QUERIES)
                .description("Total number of SQL queries executed inside transactions")
                .tag("class", className)
                .tag("method", methodName)
                .register(meterRegistry)
                .increment(queryCount);
    }

    @Override
    public void incrementCacheLookup(String cacheName, String outcome) {
        String counterKey = cacheName + ":" + outcome;

        cacheCounters
                .computeIfAbsent(counterKey, key -> Counter.builder(AxelixMetricNames.CACHE_REQUESTS)
                        .description("Total number of cache lookups")
                        .tag("cache", cacheName)
                        .tag("result", outcome)
                        .register(meterRegistry))
                .increment();
    }

    @Override
    public void registerCacheStatusGauge(String cacheName, AtomicBoolean enabledFlag) {
        Tags cacheTags = Tags.of("cache", cacheName);

        meterRegistry.gauge(AxelixMetricNames.CACHE_ENABLED, cacheTags, enabledFlag, flag -> flag.get() ? 1.0 : 0.0);
    }
}
