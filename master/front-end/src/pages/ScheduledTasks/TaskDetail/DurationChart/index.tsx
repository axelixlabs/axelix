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

import { type IScheduledTaskExecution, NOW_SECONDS, formatDuration, formatTimeOfDay } from "../../mock";
import shared from "../../shared.module.css";

import styles from "./styles.module.css";

interface IProps {
    runs: IScheduledTaskExecution[];
    maxDurationMs: number;
}

export const DurationChart = ({ runs, maxDurationMs }: IProps) => {
    const { t } = useTranslation();

    const barHeight = (durationMs: number): number => 4 + Math.round(90 * Math.sqrt(durationMs / maxDurationMs));

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.Header}>
                <span>{t("ScheduledTasks.durationTitle")}</span>
                <span className={`${shared.Mono} ${styles.Max}`}>
                    {t("ScheduledTasks.max")} {formatDuration(maxDurationMs)}
                </span>
            </div>

            <div className={styles.Bars}>
                {runs.map((run, index) => (
                    <span
                        key={index}
                        className={`${styles.Bar} ${run.succeeded ? styles.BarOk : styles.BarBad}`}
                        style={{ height: `${barHeight(run.durationMs)}px` }}
                    />
                ))}
            </div>

            <div className={`${shared.Mono} ${styles.Axis}`}>
                <span>{formatTimeOfDay(NOW_SECONDS - runs[0].agoSeconds)}</span>
                <span>{formatTimeOfDay(NOW_SECONDS - runs[runs.length - 1].agoSeconds)}</span>
            </div>
        </div>
    );
};
