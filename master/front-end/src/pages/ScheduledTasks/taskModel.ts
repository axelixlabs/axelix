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
import {
    type ETriggerType,
    type IScheduledTaskExecution,
    type IScheduledTaskSeed,
    RUNS_BY_TASK,
    SCHEDULED_TASK_SEEDS,
    formatDuration,
} from "./mock";

export type { ETriggerType } from "./mock";

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
    type: ETriggerType;
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

const simpleName = (className: string): string => className.slice(className.lastIndexOf(".") + 1);

interface IDurationStats {
    successRate: number;
    failedCount: number;
    avgLabel: string;
    p95Label: string;
    maxDurationMs: number;
}

const computeStats = (runs: IScheduledTaskExecution[]): IDurationStats => {
    if (runs.length === 0) {
        return { successRate: 0, failedCount: 0, avgLabel: "—", p95Label: "—", maxDurationMs: 0 };
    }

    const succeeded = runs.filter((run) => run.succeeded).length;
    const durations = runs.map((run) => run.durationMs);
    const sortedDurations = [...durations].sort((a, b) => a - b);
    const average = Math.round(durations.reduce((sum, value) => sum + value, 0) / runs.length);

    return {
        successRate: Math.round((succeeded / runs.length) * 100),
        failedCount: runs.length - succeeded,
        avgLabel: formatDuration(average),
        p95Label: formatDuration(sortedDurations[Math.floor(runs.length * 0.9)]),
        maxDurationMs: Math.max(...durations),
    };
};

export const buildTask = (seed: IScheduledTaskSeed, interaction: ITaskInteractionState): IScheduledTask => {
    const runs = RUNS_BY_TASK[seed.key];
    const { enabled, triggerOverride, manualRuns, running } = interaction;

    return {
        key: seed.key,
        method: seed.method,
        className: seed.className,
        simpleClassName: simpleName(seed.className),
        fqn: `${seed.className}.${seed.method}`,
        type: seed.type,
        human: triggerOverride?.human ?? seed.human,
        raw: triggerOverride?.raw ?? seed.raw,
        showRaw: seed.type === "Cron",
        enabled,
        hasHistory: seed.hasHistory,
        running,
        seed,
        runs,
        manualRuns,
        ...computeStats(runs),
        lastRun: manualRuns[0] ?? runs.at(-1) ?? null,
    };
};

/**
 * The tasks grouped by trigger type, in the fixed display order, keeping only non-empty groups.
 */
export const groupTasksByType = (tasks: IScheduledTask[]): { type: ETriggerType; tasks: IScheduledTask[] }[] => {
    const order: ETriggerType[] = ["Cron", "Fixed delay", "Fixed rate"];

    return order
        .map((type) => ({ type, tasks: tasks.filter((task) => task.type === type) }))
        .filter((group) => group.tasks.length > 0);
};

export const SEED_ORDER = SCHEDULED_TASK_SEEDS;
