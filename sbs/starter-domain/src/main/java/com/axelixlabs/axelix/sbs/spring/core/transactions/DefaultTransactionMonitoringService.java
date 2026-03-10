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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.axelixlabs.axelix.common.api.TransactionMonitoringFeed;
import com.axelixlabs.axelix.common.api.TransactionMonitoringFeed.ExecutionStats;
import com.axelixlabs.axelix.common.api.TransactionMonitoringFeed.TransactionExecution;
import com.axelixlabs.axelix.common.api.TransactionMonitoringFeed.TransactionalEntrypoint;
import com.axelixlabs.axelix.sbs.spring.core.SlidingWindow;

/**
 * Service providing access to transaction monitoring data and statistics.
 *
 * <p>Collects and aggregates transaction execution metrics from monitored @Transactional methods.
 *
 * @since 22.01.2026
 * @author Nikita Kirillov
 */
public class DefaultTransactionMonitoringService implements TransactionMonitoringService {

    private final TransactionStatsCollector transactionStatsCollector;

    public DefaultTransactionMonitoringService(TransactionStatsCollector transactionStatsCollector) {
        this.transactionStatsCollector = transactionStatsCollector;
    }

    @Override
    public TransactionMonitoringFeed getMonitoringFeed() {
        Map<MethodClassKey, SlidingWindow<TransactionRecord>> statsMap = transactionStatsCollector.getAllStats();

        List<TransactionalEntrypoint> methodStats = statsMap.entrySet().stream()
                .map(entry -> createTransactionalEntrypoint(
                        entry.getKey().getTargetClass().getName(),
                        entry.getKey().getMethod().getName(),
                        entry.getValue().get()))
                .collect(Collectors.toList());

        return new TransactionMonitoringFeed(methodStats);
    }

    @Override
    public void clearAllStats() {
        transactionStatsCollector.clearAllStats();
    }

    private TransactionalEntrypoint createTransactionalEntrypoint(
            String className, String methodName, List<TransactionRecord> transactions) {

        if (transactions.isEmpty()) {
            return new TransactionalEntrypoint(className, methodName, List.of(), new ExecutionStats(0, 0, 0));
        }

        long averageDuration = calculateAverageDuration(transactions);
        long maxDuration = calculateMaxDuration(transactions);
        long medianDuration = calculateMedianDuration(transactions);

        List<TransactionExecution> executions =
                transactions.stream().map(this::convertToTransactionExecution).collect(Collectors.toList());

        return new TransactionalEntrypoint(
                className, methodName, executions, new ExecutionStats(averageDuration, maxDuration, medianDuration));
    }

    private long calculateAverageDuration(List<TransactionRecord> transactions) {
        long total = transactions.stream()
                .mapToLong(TransactionRecord::getDurationMs)
                .sum();
        return total / transactions.size();
    }

    private long calculateMaxDuration(List<TransactionRecord> transactions) {
        return transactions.stream()
                .mapToLong(TransactionRecord::getDurationMs)
                .max()
                .orElse(0);
    }

    private long calculateMedianDuration(List<TransactionRecord> transactions) {
        List<Long> durations = transactions.stream()
                .map(TransactionRecord::getDurationMs)
                .sorted()
                .collect(Collectors.toList());

        int size = durations.size();
        if (size % 2 == 0) {
            return (durations.get(size / 2 - 1) + durations.get(size / 2)) / 2;
        } else {
            return durations.get(size / 2);
        }
    }

    private TransactionExecution convertToTransactionExecution(TransactionRecord record) {
        return new TransactionExecution(
                record.getDurationMs(), record.getStartTimestamp(), convertToQueries(record.getQueries()));
    }

    private List<TransactionMonitoringFeed.Query> convertToQueries(List<TransactionQueryRecord> queriesRecords) {
        return queriesRecords.stream()
                .map(queries -> new TransactionMonitoringFeed.Query(
                        queries.getSql(),
                        queries.getDurationMs(),
                        queries.getStartTimestampMs(),
                        queries.getEndTimestampMs()))
                .collect(Collectors.toList());
    }
}
