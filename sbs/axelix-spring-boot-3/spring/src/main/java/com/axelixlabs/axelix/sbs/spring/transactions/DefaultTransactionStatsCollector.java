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
package com.axelixlabs.axelix.sbs.spring.transactions;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation {@link TransactionStatsCollector}.
 *
 * <p>Collects and stores transaction execution statistics for monitoring.
 *
 * @since 22.01.2026
 * @author Nikita Kirillov
 */
public class DefaultTransactionStatsCollector implements TransactionStatsCollector {

    private final ConcurrentHashMap<MethodClassKey, TransactionStats> statsMap = new ConcurrentHashMap<>();
    private final int maxTransactionsPerMethod;
    private final ScheduledExecutorService executor;

    public DefaultTransactionStatsCollector(int maxTransactionsPerMethod, Duration cleanupInterval) {
        this.maxTransactionsPerMethod = maxTransactionsPerMethod;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        // TODO: allow configuring the end-user scheduled executor service
        this.executor.scheduleWithFixedDelay(
                () -> this.statsMap.values().forEach(TransactionStats::clear),
                0L,
                cleanupInterval.toSeconds(),
                TimeUnit.SECONDS);
    }

    @Override
    public void recordTransaction(MethodClassKey key, TransactionRecord transactionRecord) {
        statsMap.compute(key, (k, stats) -> {
            if (stats == null) {
                stats = new TransactionStats(maxTransactionsPerMethod);
            }
            stats.addTransactionRecord(transactionRecord);
            return stats;
        });
    }

    @Override
    public ConcurrentHashMap<MethodClassKey, TransactionStats> getAllStats() {
        return statsMap;
    }

    @Override
    public void clearAllStats() {
        statsMap.clear();
    }
}
