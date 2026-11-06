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

export interface IScheduledTask {
    taskId: string;

    /**
     * Whether the given runnable is enabled or not
     */
    enabled: boolean;

    /**
     * Share of the succeeded executions in percent (0-100), null when the task has no recorded executions
     */
    successRate: number | null;
}

/**
 * Body chunk of cron task type
 */
export interface ICron extends IScheduledTask {
    expression: string;
}

/**
 * Body chunk of fixedDelay or fixedRate tasks types
 */
export interface IFixedTasks extends IScheduledTask {
    interval: number;
    initialDelay: number;
}

/**
 * Initial scheduled tasks response data
 */
export interface IScheduledTasksResponseBody {
    cron: ICron[];
    fixedDelay: IFixedTasks[];
    fixedRate: IFixedTasks[];
}

/**
 * Aggregated statistics over all recorded executions of a scheduled task
 */
export interface IScheduledTaskHistorySummary {
    /**
     * The start of the most recent execution
     */
    lastRunAt: string;

    /**
     * The number of all recorded executions
     */
    totalRuns: number;

    /**
     * The number of the failed executions
     */
    failedRuns: number;

    /**
     * The share of the succeeded executions in percent (0-100)
     */
    successRate: number;

    /**
     * The average duration in milliseconds
     */
    averageDurationMillis: number;

    /**
     * The 95th percentile of the duration in milliseconds
     */
    p95DurationMillis: number;

    /**
     * The maximum duration in milliseconds
     */
    maxDurationMillis: number;
}

/**
 * Failures of a scheduled task grouped by the error type
 */
export interface IScheduledTaskFailuresByErrorType {
    /**
     * The simple name of the class of the exception
     */
    errorType: string;

    /**
     * The number of the failed executions with this error type
     */
    count: number;

    /**
     * The message of the most recent failure with this error type
     */
    lastErrorMessage: string | null;

    /**
     * The start of the most recent failed execution with this error type
     */
    lastOccurredAt: string;
}

/**
 * A single recorded execution of a scheduled task
 */
export interface IScheduledTaskExecutionRecord {
    /**
     * The start of the execution
     */
    startedAt: string;

    /**
     * The id of the instance that ran the execution
     */
    instanceId: string;

    /**
     * The name of the instance that ran the execution
     */
    instanceName: string;

    /**
     * The duration of the execution in milliseconds
     */
    durationMillis: number;

    /**
     * Whether the execution succeeded
     */
    success: boolean;

    /**
     * How the execution was launched
     */
    launchType: string;

    /**
     * The simple name of the class of the exception, null when the execution succeeded
     */
    errorType: string | null;

    /**
     * The message of the exception, null when the execution succeeded or the exception has no message
     */
    errorMessage: string | null;
}

/**
 * The execution history of a scheduled task on all the instances of the application
 */
export interface IScheduledTaskHistoryResponse {
    /**
     * Absent when there are no executions
     */
    summary: IScheduledTaskHistorySummary | null;

    /**
     * The failures grouped by the error type, the most frequent first
     */
    failuresByErrorType: IScheduledTaskFailuresByErrorType[];

    /**
     * The executions on the requested page, the most recent first
     */
    executions: IScheduledTaskExecutionRecord[];

    /**
     * The number of the executions across all pages
     */
    totalExecutions: number;

    /**
     * The zero-based number of the page
     */
    page: number;

    /**
     * The maximum number of executions on a page
     */
    size: number;
}

/**
 * The trigger type of the scheduled task, named after the group of the response it comes from
 */
export type TScheduledTaskType = keyof IScheduledTasksResponseBody;

/**
 * A scheduled task together with its trigger type
 */
export type TScheduledTaskItem =
    { type: "cron"; task: ICron } | { type: "fixedDelay" | "fixedRate"; task: IFixedTasks };

export interface IUpdateScheduledTasksStatusRequestData {
    instanceId: string;
    trigger: string;
    force: boolean;
    statusType: "enable" | "disable";
}

export interface IUpdateCronExpressionDataRequestData {
    /**
     * Instance id of service
     */
    instanceId: string;

    /**
     * The trigger whose expression we are changing
     */
    trigger: string;

    /**
     * New cron expression
     */
    newCronExpression: string;
}

export interface IForceRunRequestData {
    /**
     * Instance id of service
     */
    instanceId: string;

    /**
     * The trigger that we force to run
     */
    trigger: string;
}

export interface IChangeScheduledTaskIntervalRequestData {
    /**
     * Instance id of service
     */
    instanceId: string;

    /**
     * The trigger whose interval we are changing
     */
    trigger: string;

    /**
     * The new interval
     */
    interval: number;
}
