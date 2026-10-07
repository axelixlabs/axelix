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
import { EScheduledTasksTriggerType, type IScheduledTaskExecution, type IScheduledTaskSeed } from "@/models";

/**
 * "Now" expressed as seconds since midnight (14:32:10). Timestamps are rendered as a time of day,
 * which keeps the mock free of absolute dates.
 */
export const NOW_SECONDS = 14 * 3600 + 32 * 60 + 10;

const RUN_COUNT = 30;

const PACKAGE = "org.springframework.samples.petclinic.";

export const ERROR_MESSAGES: Record<string, string> = {
    SQLTransientConnectionException: "HikariPool-1 - Connection is not available, request timed out after 30000ms.",
    IllegalStateException: "Expected visits for owner 7, query returned 0 rows",
    TimeoutException: "Pet image export did not finish within 2000 ms",
    ResourceAccessException: "I/O error on POST request for PayGrid: Read timed out",
};

export const SCHEDULED_TASK_SEEDS: IScheduledTaskSeed[] = [
    {
        key: "processPendingPayments",
        method: "processPendingPayments",
        className: `${PACKAGE}payments.PaymentBatchJob`,
        type: EScheduledTasksTriggerType.CRON,
        raw: "0 0 2 * * *",
        human: "Daily at 02:00",
        period: 86400,
        base: 4200,
        spread: 2600,
        fails: [26, 28, 29],
        error: "ResourceAccessException",
        dailyAtSeconds: 2 * 3600,
        seed: 5,
        hasHistory: true,
    },
    {
        key: "publishHeartbeat",
        method: "publishHeartbeatToServiceRegistry",
        className: `${PACKAGE}scheduled.registry.ServiceRegistryHeartbeatScheduler`,
        type: EScheduledTasksTriggerType.CRON,
        raw: "*/2 * * * * *",
        human: "Every 2 s",
        period: 2,
        base: 2,
        spread: 5,
        fails: [],
        seed: 7,
        hasHistory: true,
    },
    {
        key: "evictReservations",
        method: "evictExpiredAppointmentReservations",
        className: `${PACKAGE}scheduling.ClinicMaintenanceScheduler`,
        type: EScheduledTasksTriggerType.CRON,
        raw: "*/5 * * * * *",
        human: "Every 5 s",
        period: 5,
        base: 30,
        spread: 40,
        fails: [],
        seed: 11,
        hasHistory: true,
    },
    {
        key: "generateInvoiceReport",
        method: "generateMonthlyInvoiceReport",
        className: `${PACKAGE}billing.BillingReportScheduler`,
        type: EScheduledTasksTriggerType.CRON,
        raw: "0 0 3 1 * *",
        human: "Monthly, day 1 at 03:00",
        period: 0,
        base: 0,
        spread: 0,
        fails: [],
        seed: 3,
        hasHistory: false,
    },
    {
        key: "exportPetImages",
        method: "exportPetImagesToObjectStorage",
        className: `${PACKAGE}scheduling.ClinicMaintenanceScheduler`,
        type: EScheduledTasksTriggerType.FIXED_DELAY,
        raw: "delay 2000 · initial 0",
        human: "2 s after each run ends",
        period: 3.3,
        base: 1100,
        spread: 450,
        fails: [22],
        error: "TimeoutException",
        seed: 19,
        hasHistory: true,
    },
    {
        key: "runConsistencyQueries",
        method: "runTestDataConsistencyQueries",
        className: `${PACKAGE}testdata.consistency.TestDataConsistencyQueryRunner`,
        type: EScheduledTasksTriggerType.FIXED_RATE,
        raw: "rate 60000 · initial 10000",
        human: "Every 60 s",
        period: 60,
        base: 850,
        spread: 2700,
        fails: [3, 4, 11, 19, 20, 26, 27],
        error: "SQLTransientConnectionException",
        errorOverrides: { 26: "IllegalStateException" },
        previousInstanceBefore: 8,
        seed: 23,
        hasHistory: true,
    },
    {
        key: "refreshSpecialtiesCache",
        method: "refreshVeterinarianSpecialtiesCache",
        className: `${PACKAGE}scheduling.ClinicMaintenanceScheduler`,
        type: EScheduledTasksTriggerType.FIXED_RATE,
        raw: "rate 2000 · initial 100",
        human: "Every 2 s",
        period: 2,
        base: 8,
        spread: 24,
        fails: [],
        stoppedSecondsAgo: 14 * 60,
        seed: 31,
        hasHistory: true,
    },
];

const pad = (value: number): string => String(value).padStart(2, "0");

/**
 * Formats seconds-since-midnight as HH:MM:SS, wrapping into a single day so histories that span
 * more than a day (e.g. a nightly batch) still render as the time of day they ran.
 */
export const formatTimeOfDay = (seconds: number): string => {
    const wrapped = ((Math.floor(seconds) % 86400) + 86400) % 86400;
    return `${pad(Math.floor(wrapped / 3600))}:${pad(Math.floor(wrapped / 60) % 60)}:${pad(wrapped % 60)}`;
};

/**
 * Formats seconds-since-midnight as HH:MM.
 */
export const formatHourMinute = (seconds: number): string => formatTimeOfDay(seconds).slice(0, 5);

export const formatDuration = (ms: number): string =>
    ms < 1000 ? `${ms} ms` : `${(ms / 1000).toFixed(ms < 10000 ? 2 : 1)} s`;

export const formatRelative = (seconds: number): string => {
    if (seconds < 60) {
        return `${Math.round(seconds)} s ago`;
    }

    if (seconds < 3600) {
        return `${Math.round(seconds / 60)} min ago`;
    }

    if (seconds < 86400) {
        return `${Math.round(seconds / 3600)} h ago`;
    }

    return `${Math.round(seconds / 86400)} d ago`;
};

const runErrorType = (seed: IScheduledTaskSeed, index: number): string | null => {
    if (!seed.fails.includes(index)) {
        return null;
    }

    return seed.errorOverrides?.[index] ?? seed.error ?? null;
};

const runDuration = (seed: IScheduledTaskSeed, errorType: string | null, random: () => number): number => {
    if (errorType === "SQLTransientConnectionException") {
        return 30000 + Math.round(random() * 60);
    }

    if (errorType === "TimeoutException") {
        return 2000 + Math.round(random() * 20);
    }

    if (errorType === "ResourceAccessException") {
        return 84000 + Math.round(random() * 3000);
    }

    return Math.round(seed.base + random() * seed.spread);
};

/**
 * Seconds before "now" a run started. Daily tasks are anchored to their time of day (one day
 * apart); every other task is spaced by its period. Exactly one random() call keeps the generated
 * history deterministic and preserves the call order of the other fields.
 */
const runAgoSeconds = (seed: IScheduledTaskSeed, index: number, random: () => number): number => {
    if (seed.dailyAtSeconds !== undefined) {
        return (RUN_COUNT - 1 - index) * 86400 + (NOW_SECONDS - seed.dailyAtSeconds) + Math.round(random() * 90);
    }

    return (
        (RUN_COUNT - 1 - index) * seed.period +
        (seed.stoppedSecondsAgo ?? 0) +
        1 +
        Math.round(random() * seed.period * 0.1)
    );
};

const runInstance = (seed: IScheduledTaskSeed, index: number): string =>
    seed.previousInstanceBefore && index < seed.previousInstanceBefore
        ? "petclinic-6d8f7b-h2v9c"
        : "petclinic-6d8f7b-qz4lm";

/**
 * Deterministically generates a task's execution history using a Lehmer pseudo-random generator,
 * so the same seed always produces the same runs across reloads.
 */
const generateRuns = (seed: IScheduledTaskSeed): IScheduledTaskExecution[] => {
    if (!seed.hasHistory) {
        return [];
    }

    let state = seed.seed;
    const random = (): number => (state = (state * 16807) % 2147483647) / 2147483647;

    return Array.from({ length: RUN_COUNT }, (_, index) => {
        const agoSeconds = runAgoSeconds(seed, index, random);
        const errorType = runErrorType(seed, index);
        const durationMs = runDuration(seed, errorType, random);

        return {
            agoSeconds,
            durationMs,
            succeeded: errorType === null,
            errorType,
            errorMessage: errorType ? ERROR_MESSAGES[errorType] : "",
            instance: runInstance(seed, index),
            manual: false,
        };
    });
};

/**
 * The generated history is static per task, so it is computed once and shared.
 */
export const RUNS_BY_TASK: Record<string, IScheduledTaskExecution[]> = Object.fromEntries(
    SCHEDULED_TASK_SEEDS.map((seed) => [seed.key, generateRuns(seed)]),
);

/**
 * Builds a fresh manual-run record for the "Run now" action.
 */
export const createManualRun = (seed: IScheduledTaskSeed): IScheduledTaskExecution => ({
    agoSeconds: 0,
    durationMs: Math.round(seed.base + Math.random() * seed.spread),
    succeeded: true,
    errorType: null,
    errorMessage: "",
    instance: "petclinic-6d8f7b-qz4lm",
    manual: true,
});
