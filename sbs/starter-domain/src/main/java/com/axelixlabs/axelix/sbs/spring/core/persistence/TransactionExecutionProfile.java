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
package com.axelixlabs.axelix.sbs.spring.core.persistence;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.utils.Assert;

/**
 * The living profile of the transaction execution.
 *
 * @author Mikhail Polivakha
 */
public class TransactionExecutionProfile {

    private final List<AnalyzedSqlQueryRecord> recordedQueries;
    private final Instant startedAt;
    private @Nullable Instant finishedAt;

    public TransactionExecutionProfile(Instant startedAt) {
        this.recordedQueries = new ArrayList<>(4);
        this.startedAt = startedAt;
        this.finishedAt = null;
    }

    public void recordQuery(SimpleSqlQueryRecord sqlQueryRecord) {
        recordedQueries.add(new AnalyzedSqlQueryRecord(sqlQueryRecord));
    }

    public void recordLazyLoading(LazyLoadingTarget lazyLoadingTarget) {
        Assert.state(() -> !recordedQueries.isEmpty(), "analyzed sql queries cannot be empty at this point");

        int tailIndex = recordedQueries.size() - 1;

        AnalyzedSqlQueryRecord analyzedSqlQueryRecord = recordedQueries.get(tailIndex);

        recordedQueries.set(tailIndex, analyzedSqlQueryRecord.markAsLadyLoaded(lazyLoadingTarget));
    }

    public TransactionExecutionProfile complete() {
        this.finishedAt = Instant.now();
        return this;
    }

    public List<AnalyzedSqlQueryRecord> getRecordedQueries() {
        return recordedQueries;
    }

    public long getStartedAtMillisFromEpoch() {
        return startedAt.toEpochMilli();
    }

    @SuppressWarnings("NullAway")
    public long getFinishedAtMillisFromEpoch() throws IllegalStateException {
        /*
         Why do we need this logic: In case the transaction is very short-lived (e.g. it is executed within milliseconds), then this might be possible, that
         any particular query executed in the transaction, i.e. last query record, will have the timestamp that is slightly
         ahead of the transaction end time as we calculate it.

         Why? Because of the rounding errors and non-deterministic granularity of System.currentTimeMillis().
         So we have to assume, that if for some reason any sql query got larger end timestamp than that of a transaction -
         we just take this end timestamp of this query as the transaction end timestamp and that is it.
        */
        Assert.state(() -> finishedAt != null, "This call is expected only when transaction is already finished");

        if (recordedQueries.isEmpty()) {
            return finishedAt.toEpochMilli();
        } else {
            AnalyzedSqlQueryRecord analyzedSqlQueryRecord = recordedQueries.get(recordedQueries.size() - 1);
            return Math.max(analyzedSqlQueryRecord.getEndTimestampMs(), finishedAt.toEpochMilli());
        }
    }

    public Duration getTransactionDuration() {
        return Duration.between(startedAt, finishedAt);
    }

    public int getQueriesCount() {
        return recordedQueries.size();
    }

    public static class AnalyzedSqlQueryRecord {

        private final SimpleSqlQueryRecord queryRecord;
        private @Nullable final LazyLoadingTarget lazyLoadingTarget;

        AnalyzedSqlQueryRecord(SimpleSqlQueryRecord queryRecord) {
            this.queryRecord = queryRecord;
            this.lazyLoadingTarget = null;
        }

        AnalyzedSqlQueryRecord(SimpleSqlQueryRecord queryRecord, LazyLoadingTarget lazyLoadingTarget) {
            this.queryRecord = queryRecord;
            this.lazyLoadingTarget = lazyLoadingTarget;
        }

        public AnalyzedSqlQueryRecord markAsLadyLoaded(LazyLoadingTarget lazyLoadingTarget) {
            return new AnalyzedSqlQueryRecord(queryRecord, lazyLoadingTarget);
        }

        public String getSql() {
            return queryRecord.getSql();
        }

        public long getDurationMs() {
            return queryRecord.getDurationMs();
        }

        public long getStartTimestampMs() {
            return queryRecord.getStartTimestampMs();
        }

        public long getEndTimestampMs() {
            return queryRecord.getEndTimestampMs();
        }

        public boolean isInMemoryPaginated() {
            return queryRecord.isInMemoryPaginated();
        }

        public @Nullable LazyLoadingTarget getLazyLoadingTarget() {
            return lazyLoadingTarget;
        }
    }
}
