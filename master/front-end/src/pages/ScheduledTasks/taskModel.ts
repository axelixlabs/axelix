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
import type {
    IDurationStats,
    IScheduledTask,
    IScheduledTaskExecution,
    IScheduledTaskSeed,
    ITaskInteractionState,
} from "@/models";

import { RUNS_BY_TASK, SCHEDULED_TASK_SEEDS, formatDuration } from "./mock";

const simpleName = (className: string): string => className.slice(className.lastIndexOf(".") + 1);

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

export const SEED_ORDER = SCHEDULED_TASK_SEEDS;
