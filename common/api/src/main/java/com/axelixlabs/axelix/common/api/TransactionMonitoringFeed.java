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
package com.axelixlabs.axelix.common.api;

import java.util.List;

/**
 * The feed of transactions inside a given application.
 *
 * @since 20.01.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public record TransactionMonitoringFeed(List<TransactionalEntrypoint> entrypoints) {

    /**
     * The transactional entrypoint. In other words,
     *
     * @param className  the short name of the class where transaction is initiated.
     * @param methodName the name of the method where transaction is initiated.
     * @param executions currently recorded executions of this transaction entrypoint.
     */
    public record TransactionalEntrypoint(
            String className,
            String methodName,
            List<TransactionExecution> executions,
            ExecutionStats executionStats) {}

    /**
     * A single transaction execution record with timing information.
     *
     * @param durationsMs transaction execution duration in milliseconds
     * @param timestamp   unix timestamp (milliseconds from epoch) when transaction started
     */
    public record TransactionExecution(long durationsMs, long timestamp) {}

    /**
     * Aggregated execution statistics for a transactional entrypoint.
     *
     * @param averageDurationMs average execution duration in milliseconds
     * @param maxDurationMs     maximum execution duration in milliseconds
     * @param medianDurationMs  median execution duration in milliseconds
     */
    public record ExecutionStats(long averageDurationMs, long maxDurationMs, long medianDurationMs) {}
}
