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
import java.util.Objects;

/**
 * The feed of transactions inside a given application.
 *
 * @since 20.01.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public final class TransactionMonitoringFeed {

    private final List<TransactionalEntrypoint> entrypoints;

    /**
     * Creates a new TransactionMonitoringFeed.
     *
     * @param entrypoints the list of transactional entrypoints.
     */
    public TransactionMonitoringFeed(List<TransactionalEntrypoint> entrypoints) {
        this.entrypoints = entrypoints;
    }

    public List<TransactionalEntrypoint> getEntrypoints() {
        return entrypoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransactionMonitoringFeed that = (TransactionMonitoringFeed) o;
        return Objects.equals(entrypoints, that.entrypoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entrypoints);
    }

    @Override
    public String toString() {
        return "TransactionMonitoringFeed{" + "entrypoints=" + entrypoints + '}';
    }

    /**
     * The transactional entrypoint. In other words,
     */
    public static final class TransactionalEntrypoint {

        private final String className;
        private final String methodName;
        private final List<TransactionExecution> executions;
        private final ExecutionStats executionStats;

        /**
         * Creates a new TransactionalEntrypoint.
         *
         * @param className      the short name of the class where transaction is initiated.
         * @param methodName     the name of the method where transaction is initiated.
         * @param executions     currently recorded executions of this transaction entrypoint.
         * @param executionStats the execution statistics.
         */
        public TransactionalEntrypoint(
                String className,
                String methodName,
                List<TransactionExecution> executions,
                ExecutionStats executionStats) {
            this.className = className;
            this.methodName = methodName;
            this.executions = executions;
            this.executionStats = executionStats;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public List<TransactionExecution> getExecutions() {
            return executions;
        }

        public ExecutionStats getExecutionStats() {
            return executionStats;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TransactionalEntrypoint that = (TransactionalEntrypoint) o;
            return Objects.equals(className, that.className)
                    && Objects.equals(methodName, that.methodName)
                    && Objects.equals(executions, that.executions)
                    && Objects.equals(executionStats, that.executionStats);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, methodName, executions, executionStats);
        }

        @Override
        public String toString() {
            return "TransactionalEntrypoint{"
                    + "className='"
                    + className
                    + '\''
                    + ", methodName='"
                    + methodName
                    + '\''
                    + ", executions="
                    + executions
                    + ", executionStats="
                    + executionStats
                    + '}';
        }
    }

    /**
     * A single transaction execution record with timing information.
     */
    public static final class TransactionExecution {

        private final long durationMs;
        private final long timestamp;
        private final List<Query> queries;

        /**
         * Creates a new TransactionExecution.
         *
         * @param durationMs transaction execution duration in milliseconds
         * @param timestamp  unix timestamp (milliseconds from epoch) when transaction started
         * @param queries    the list of queries executed during a particular transaction.
         */
        public TransactionExecution(long durationMs, long timestamp, List<Query> queries) {
            this.durationMs = durationMs;
            this.timestamp = timestamp;
            this.queries = queries;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public List<Query> getQueries() {
            return queries;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TransactionExecution that = (TransactionExecution) o;
            return durationMs == that.durationMs
                    && timestamp == that.timestamp
                    && Objects.equals(queries, that.queries);
        }

        @Override
        public int hashCode() {
            return Objects.hash(durationMs, timestamp, queries);
        }

        @Override
        public String toString() {
            return "TransactionExecution{" + "durationMs="
                    + durationMs + ", timestamp="
                    + timestamp + ", queries="
                    + queries + '}';
        }
    }

    /**
     * The query executed during a particular transaction.
     */
    public static final class Query {
        private final String sql;
        private final Long durationMs;
        private final Long startTimestampMs;
        private final Long endTimestampMs;

        /**
         * Creates a new Query.
         *
         * @param sql               the executed SQL statement
         * @param durationMs        query execution duration in milliseconds.
         * @param startTimestampMs  unix timestamp (milliseconds from epoch) when the query started.
         * @param endTimestampMs    unix timestamp (milliseconds since epoch) when the query finished.
         */
        public Query(String sql, Long durationMs, Long startTimestampMs, Long endTimestampMs) {
            this.sql = sql;
            this.durationMs = durationMs;
            this.startTimestampMs = startTimestampMs;
            this.endTimestampMs = endTimestampMs;
        }

        public String getSql() {
            return sql;
        }

        public Long getDurationMs() {
            return durationMs;
        }

        public Long getStartTimestampMs() {
            return startTimestampMs;
        }

        public Long getEndTimestampMs() {
            return endTimestampMs;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Query that = (Query) o;
            return Objects.equals(sql, that.sql)
                    && Objects.equals(durationMs, that.durationMs)
                    && Objects.equals(startTimestampMs, that.startTimestampMs)
                    && Objects.equals(endTimestampMs, that.endTimestampMs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sql, durationMs, startTimestampMs, endTimestampMs);
        }

        @Override
        public String toString() {
            return "Query{" + "sql='"
                    + sql + '\'' + ", durationMs="
                    + durationMs + ", startTimestampMs="
                    + startTimestampMs + ", endTimestampMs="
                    + endTimestampMs + '}';
        }
    }

    /**
     * Aggregated execution statistics for a transactional entrypoint.
     */
    public static final class ExecutionStats {

        private final long averageDurationMs;
        private final long maxDurationMs;
        private final long medianDurationMs;

        /**
         * Creates a new ExecutionStats.
         *
         * @param averageDurationMs average execution duration in milliseconds
         * @param maxDurationMs     maximum execution duration in milliseconds
         * @param medianDurationMs  median execution duration in milliseconds
         */
        public ExecutionStats(long averageDurationMs, long maxDurationMs, long medianDurationMs) {
            this.averageDurationMs = averageDurationMs;
            this.maxDurationMs = maxDurationMs;
            this.medianDurationMs = medianDurationMs;
        }

        public long getAverageDurationMs() {
            return averageDurationMs;
        }

        public long getMaxDurationMs() {
            return maxDurationMs;
        }

        public long getMedianDurationMs() {
            return medianDurationMs;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ExecutionStats that = (ExecutionStats) o;
            return averageDurationMs == that.averageDurationMs
                    && maxDurationMs == that.maxDurationMs
                    && medianDurationMs == that.medianDurationMs;
        }

        @Override
        public int hashCode() {
            return Objects.hash(averageDurationMs, maxDurationMs, medianDurationMs);
        }

        @Override
        public String toString() {
            return "ExecutionStats{"
                    + "averageDurationMs="
                    + averageDurationMs
                    + ", maxDurationMs="
                    + maxDurationMs
                    + ", medianDurationMs="
                    + medianDurationMs
                    + '}';
        }
    }
}
