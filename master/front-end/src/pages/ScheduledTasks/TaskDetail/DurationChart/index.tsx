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
import dayjs from "dayjs";
import { useTranslation } from "react-i18next";

import { HintTooltip } from "@/components";
import { formatTransactionDuration } from "@/helpers";
import type { IScheduledTaskExecutionRecord } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    executions: IScheduledTaskExecutionRecord[];
}

const getBarHeight = (durationMillis: number, maxDurationMillis: number): number =>
    maxDurationMillis > 0 ? 4 + Math.round(90 * Math.sqrt(durationMillis / maxDurationMillis)) : 4;

export const DurationChart = ({ executions }: IProps) => {
    const { t } = useTranslation();

    const runs = [...executions].reverse();
    const maxDurationMillis = Math.max(...runs.map((run) => run.durationMillis));

    const firstStartedAt = dayjs(runs[0].startedAt);
    const lastStartedAt = dayjs(runs[runs.length - 1].startedAt);
    const axisFormat = firstStartedAt.isSame(lastStartedAt, "day") ? "HH:mm:ss" : "DD.MM HH:mm";

    const renderBarHint = (run: IScheduledTaskExecutionRecord) => (
        <>
            <div>{dayjs(run.startedAt).format("DD.MM HH:mm:ss")}</div>
            <div>{formatTransactionDuration(run.durationMillis)}</div>
            <div>{run.success ? t("ScheduledTasks.succeeded") : (run.errorType ?? t("ScheduledTasks.failed"))}</div>
        </>
    );

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.Header}>
                <span className={styles.Title}>
                    {t("ScheduledTasks.durationTitle")}
                    <HintTooltip content={t("ScheduledTasks.durationHint")}>
                        <span className={styles.Question}>?</span>
                    </HintTooltip>
                </span>
                <span className={`${styles.Mono} ${styles.Max}`}>
                    {t("ScheduledTasks.max")} {formatTransactionDuration(maxDurationMillis)}
                </span>
            </div>

            <div className={styles.Bars}>
                {runs.map((run) => (
                    <HintTooltip key={`${run.startedAt}-${run.instanceId}`} content={renderBarHint(run)}>
                        <span
                            className={`${styles.Bar} ${run.success ? styles.BarOk : styles.BarBad}`}
                            style={{ height: `${getBarHeight(run.durationMillis, maxDurationMillis)}px` }}
                        />
                    </HintTooltip>
                ))}
            </div>

            <div className={`${styles.Mono} ${styles.Axis}`}>
                <span>{firstStartedAt.format(axisFormat)}</span>
                <span>{lastStartedAt.format(axisFormat)}</span>
            </div>
        </div>
    );
};
