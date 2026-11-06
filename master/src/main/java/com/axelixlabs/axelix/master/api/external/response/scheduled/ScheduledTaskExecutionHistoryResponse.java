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
package com.axelixlabs.axelix.master.api.external.response.scheduled;

import java.time.Instant;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.contract.metadata.ScheduledTaskLaunchType;

/**
 * The execution history of the scheduled task on all the instances of the application, with a single page of its
 * executions.
 *
 * @param summary         absent when there are no executions.
 * @param executions      the executions on the requested page, the most recent first.
 * @param totalExecutions the number of the executions across all pages.
 * @param page            the zero-based number of the page.
 * @param size            the maximum number of executions on a page.
 *
 * @author Nikita Kirillov
 */
public record ScheduledTaskExecutionHistoryResponse(
        @Nullable Summary summary,
        List<FailuresByErrorType> failuresByErrorType,
        List<Execution> executions,
        long totalExecutions,
        int page,
        int size) {

    /**
     * @param lastRunAt         the start of the most recent execution.
     * @param successRate       the share of the succeeded executions in percent.
     * @param p95DurationMillis the 95th percentile of the duration.
     */
    public record Summary(
            Instant lastRunAt,
            long totalRuns,
            long failedRuns,
            double successRate,
            long averageDurationMillis,
            long p95DurationMillis,
            long maxDurationMillis) {}

    /**
     * @param errorType        the simple name of the class of the exception.
     * @param count            the number of the failed executions with this error type.
     * @param lastErrorMessage the message of the most recent failure with this error type.
     * @param lastOccurredAt   the start of the most recent failed execution with this error type.
     */
    public record FailuresByErrorType(
            String errorType, long count, @Nullable String lastErrorMessage, Instant lastOccurredAt) {}

    public record Execution(
            Instant startedAt,
            String instanceId,
            String instanceName,
            long durationMillis,
            boolean success,
            ScheduledTaskLaunchType launchType,
            @Nullable String errorType,
            @Nullable String errorMessage) {}
}
