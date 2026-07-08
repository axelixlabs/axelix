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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

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
    @JsonCreator
    public TransactionMonitoringFeed(@JsonProperty("entrypoints") List<TransactionalEntrypoint> entrypoints) {
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
        @JsonCreator
        public TransactionalEntrypoint(
                @JsonProperty("className") String className,
                @JsonProperty("methodName") String methodName,
                @JsonProperty("executions") List<TransactionExecution> executions,
                @JsonProperty("executionStats") ExecutionStats executionStats) {
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

        private final long startTimestampMs;
        private final long endTimestampMs;
        private final List<Query> queries;

        /**
         * Creates a new TransactionExecution.
         *
         * @param startTimestampMs   unix timestamp (milliseconds from epoch) when transaction started
         * @param endTimestampMs     unix timestamp (milliseconds from epoch) when transaction finished
         * @param queries            the list of queries executed during a particular transaction
         */
        @JsonCreator
        public TransactionExecution(
                @JsonProperty("startTimestampMs") long startTimestampMs,
                @JsonProperty("endTimestampMs") long endTimestampMs,
                @JsonProperty("queries") List<Query> queries) {
            this.startTimestampMs = startTimestampMs;
            this.endTimestampMs = endTimestampMs;
            this.queries = queries;
        }

        public long getStartTimestampMs() {
            return startTimestampMs;
        }

        public long getEndTimestampMs() {
            return endTimestampMs;
        }

        public List<Query> getQueries() {
            return queries;
        }

        @Override
        public String toString() {
            return "TransactionExecution{" + "startTimestampMs="
                    + startTimestampMs + ", endTimestampMs="
                    + endTimestampMs + ", queriesCount="
                    + queries + '}';
        }
    }

    /**
     * The query executed during a particular transaction.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Query {

        private final String sql;
        private final Long startTimestampMs;
        private final Long endTimestampMs;
        private final @Nullable Boolean inMemoryPaginated;
        private final @Nullable LazyLoadingTarget lazyLoadingTarget;

        /**
         * Creates a new Query.
         *
         * @param sql               the executed SQL statement
         * @param startTimestampMs  unix timestamp (milliseconds from epoch) when the query started
         * @param endTimestampMs    unix timestamp (milliseconds since epoch) when the query finished
         * @param inMemoryPaginated whether Hibernate applied pagination in memory for this query,
         *                          or {@code null} if in-memory pagination did not take place inside this query.
         */
        @JsonCreator
        public Query(
                @JsonProperty("sql") String sql,
                @JsonProperty("startTimestampMs") Long startTimestampMs,
                @JsonProperty("endTimestampMs") Long endTimestampMs,
                @JsonProperty("inMemoryPaginated") @Nullable Boolean inMemoryPaginated,
                @JsonProperty("lazyLoadingTarget") @Nullable LazyLoadingTarget lazyLoadingTarget) {
            this.sql = sql;
            this.startTimestampMs = startTimestampMs;
            this.endTimestampMs = endTimestampMs;
            this.inMemoryPaginated = inMemoryPaginated;
            this.lazyLoadingTarget = lazyLoadingTarget;
        }

        public String getSql() {
            return sql;
        }

        public Long getStartTimestampMs() {
            return startTimestampMs;
        }

        public Long getEndTimestampMs() {
            return endTimestampMs;
        }

        public @Nullable Boolean isInMemoryPaginated() {
            return inMemoryPaginated;
        }

        public @Nullable LazyLoadingTarget getLazyLoadingTarget() {
            return lazyLoadingTarget;
        }

        @Override
        public String toString() {
            return "Query{" + "sql='"
                    + sql + '\'' + ", startTimestampMs="
                    + startTimestampMs + ", endTimestampMs="
                    + endTimestampMs + ", inMemoryPaginated="
                    + inMemoryPaginated + ", lazyLoadingTarget="
                    + lazyLoadingTarget + '}';
        }
    }

    public static class LazyLoadingTarget {

        /**
         * The entity on which the assassination was lazy loaded
         */
        private final Class<?> ownerEntityClass;

        /**
         * The association
         */
        private final String associationPropertyName;

        @JsonCreator
        public LazyLoadingTarget(
                @JsonProperty("ownerEntityClass") Class<?> ownerEntityClass,
                @JsonProperty("associationPropertyName") String associationPropertyName) {
            this.ownerEntityClass = ownerEntityClass;
            this.associationPropertyName = associationPropertyName;
        }

        public Class<?> getOwnerEntityClass() {
            return ownerEntityClass;
        }

        public String getAssociationPropertyName() {
            return associationPropertyName;
        }

        public Class<?> ownerEntityClass() {
            return ownerEntityClass;
        }

        public String associationPropertyName() {
            return associationPropertyName;
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
        @JsonCreator
        public ExecutionStats(
                @JsonProperty("averageDurationMs") long averageDurationMs,
                @JsonProperty("maxDurationMs") long maxDurationMs,
                @JsonProperty("medianDurationMs") long medianDurationMs) {
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
