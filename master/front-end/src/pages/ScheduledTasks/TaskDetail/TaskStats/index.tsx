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
import { useTranslation } from "react-i18next";

import { formatRelativeTime, formatTransactionDuration } from "@/helpers";
import type { IScheduledTaskHistorySummary } from "@/models";
import successRateStyles from "@/pages/ScheduledTasks/successRate.module.css";

import { getSuccessRateClass } from "../../taskStyles";

import styles from "./styles.module.css";

interface IProps {
    summary: IScheduledTaskHistorySummary | null;
    enabled: boolean;
}

interface IStat {
    labelKey: string;
    value?: string;
    valueClass?: string;
}

const STAT_LABEL_KEYS = {
    lastRun: "ScheduledTasks.statLastRun",
    success: "ScheduledTasks.statSuccess",
    failed: "ScheduledTasks.statFailed",
    avg: "ScheduledTasks.statAvg",
    p95: "ScheduledTasks.statP95",
};

const getStats = (summary: IScheduledTaskHistorySummary, enabled: boolean, language: string): IStat[] => {
    const successRate = Math.floor(summary.successRate);

    return [
        { labelKey: STAT_LABEL_KEYS.lastRun, value: formatRelativeTime(summary.lastRunAt, language) },
        {
            labelKey: STAT_LABEL_KEYS.success,
            value: `${successRate}%`,
            valueClass: getSuccessRateClass(successRate, enabled),
        },
        {
            labelKey: STAT_LABEL_KEYS.failed,
            value: String(summary.failedRuns),
            valueClass: summary.failedRuns > 0 ? successRateStyles.RateBad : undefined,
        },
        { labelKey: STAT_LABEL_KEYS.avg, value: formatTransactionDuration(summary.averageDurationMillis) },
        { labelKey: STAT_LABEL_KEYS.p95, value: formatTransactionDuration(summary.p95DurationMillis) },
    ];
};

const EMPTY_STATS: IStat[] = Object.values(STAT_LABEL_KEYS).map((labelKey) => ({ labelKey }));

export const TaskStats = ({ summary, enabled }: IProps) => {
    const { t, i18n } = useTranslation();

    const stats = summary ? getStats(summary, enabled, i18n.language) : EMPTY_STATS;

    return (
        <div className={styles.Stats}>
            {stats.map(({ labelKey, value, valueClass }) => (
                <div className={styles.Stat} key={labelKey}>
                    <span className={styles.StatLabel}>{t(labelKey)}</span>
                    <span
                        className={`${styles.Mono} ${styles.StatValue} ${value ? (valueClass ?? "") : styles.StatValueMuted}`}
                    >
                        {value ?? "—"}
                    </span>
                </div>
            ))}
        </div>
    );
};
