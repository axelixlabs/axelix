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
import type { EScheduledTasksTriggerType } from "../enums/scheduledTasks";

interface ITarget {
    /**
     * Target field of runnable field
     */
    target: string;
}

interface IRunnable {
    /**
     * Runnable field of scheduled tasks types body
     */
    runnable: ITarget;

    /**
     * Whether the given runnable is enabled or not
     */
    enabled: boolean;
}

/**
 * Body chunk of cron task type
 */
export interface ICron extends IRunnable {
    expression: string;
}

/**
 * Body chunk of fixedDelay or fixedRate tasks types
 */
export interface IFixedTasks extends IRunnable {
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

/**
 * A trigger override produced by the "Edit trigger" form. The trigger type never changes - only
 * its schedule (cron expression for cron tasks, interval for periodic ones).
 */
export interface ITriggerOverride {
    human: string;
    raw: string;
}

/**
 * The mutable, per-task interaction state the page layers on top of the static seeds.
 */
export interface ITaskInteractionState {
    enabled: boolean;
    triggerOverride?: ITriggerOverride;
    manualRuns: IScheduledTaskExecution[];
    running: boolean;
}

/**
 * The fully derived view model a single task renders from.
 */
export interface IScheduledTask {
    key: string;
    method: string;
    className: string;
    simpleClassName: string;
    fqn: string;
    type: EScheduledTasksTriggerType;
    human: string;
    raw: string;
    showRaw: boolean;
    enabled: boolean;
    hasHistory: boolean;
    running: boolean;
    seed: IScheduledTaskSeed;
    runs: IScheduledTaskExecution[];
    manualRuns: IScheduledTaskExecution[];
    successRate: number;
    failedCount: number;
    avgLabel: string;
    p95Label: string;
    maxDurationMs: number;
    lastRun: IScheduledTaskExecution | null;
}

export interface IDurationStats {
    successRate: number;
    failedCount: number;
    avgLabel: string;
    p95Label: string;
    maxDurationMs: number;
}

export interface IScheduledTaskExecution {
    /**
     * Seconds before "now" the execution started. Times on the page are rendered as a time of day.
     */
    agoSeconds: number;
    durationMs: number;
    succeeded: boolean;
    errorType: string | null;
    errorMessage: string;
    instance: string;
    manual: boolean;
}

/**
 * The static definition of a scheduled task, before any user interaction is layered on top.
 */
export interface IScheduledTaskSeed {
    key: string;
    method: string;
    className: string;
    type: EScheduledTasksTriggerType;

    /**
     * Raw trigger expression (cron expression, or the "delay/rate · initial" summary).
     */
    raw: string;

    /**
     * Human-friendly schedule, e.g. "Every 2 s" or "Monthly, day 1 at 03:00".
     */
    human: string;

    /**
     * Seconds between runs - drives the generated timestamps of the history.
     */
    period: number;

    /**
     * Baseline duration in ms, plus the random spread added on top of it.
     */
    base: number;
    spread: number;

    /**
     * Indexes (0..29) of the generated runs that failed.
     */
    fails: number[];

    /**
     * Default error type for failed runs, optionally overridden per run index via errorOverrides.
     */
    error?: string;
    errorOverrides?: Record<number, string>;

    /**
     * Runs before this index ran on the previous instance (shows an instance roll-over).
     */
    previousInstanceBefore?: number;

    /**
     * When set, the task was switched off this many seconds ago and starts disabled.
     */
    stoppedSecondsAgo?: number;

    /**
     * When set, the task runs once a day at this time of day (seconds since midnight), so every
     * generated run lands on that clock time one day apart. Overrides period-based spacing.
     */
    dailyAtSeconds?: number;

    /**
     * Seed for the deterministic pseudo-random generator.
     */
    seed: number;

    /**
     * When false, the task has no recorded executions - the detail pane shows the 11a empty state.
     */
    hasHistory: boolean;
}

export interface IScheduledTasksTaskGroup {
    type: EScheduledTasksTriggerType;
    tasks: IScheduledTask[];
}

export interface IDetailStat {
    key: string;
    label: string;
    value: string;
    valueClass?: string;
}

export interface IDerivedTrigger {
    valid: boolean;
    human: string;
    raw: string;
    error: string;
}

export interface IGroupTasksByType {
    type: EScheduledTasksTriggerType;
    tasks: IScheduledTask[];
}
