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

import type { IScheduledTaskExecution } from "@/models";
import { NOW_SECONDS, formatDuration, formatTimeOfDay } from "@/pages/ScheduledTasks/mock";

import sharedStyles from "../../../shared.module.css";

import styles from "./styles.module.css";

interface IProps {
    run: IScheduledTaskExecution;
}

export const ExecutionsTableRow = ({ run }: IProps) => {
    const { t } = useTranslation();

    const { succeeded, agoSeconds, instance, durationMs, manual, errorType, errorMessage } = run;

    return (
        <>
            <div className={`TextUltraSmall ${styles.Row} ${succeeded ? "" : styles.RowFailed}`}>
                <div className={`${sharedStyles.Mono} ${styles.StartedTime}`}>
                    {formatTimeOfDay(NOW_SECONDS - agoSeconds)}
                </div>
                <div className={`${sharedStyles.Mono} ${styles.Instance}`}>{instance}</div>
                <div className={`${sharedStyles.Mono} ${styles.Duration}`}>{formatDuration(durationMs)}</div>

                <div className={styles.Result}>
                    <div className={styles.ResultLine}>
                        {succeeded ? (
                            <div className={styles.ResultSucceeded}>✓ {t("ScheduledTasks.succeeded")}</div>
                        ) : (
                            <div className={`${sharedStyles.Mono} ${styles.ResultFailed}`}>✕ {errorType}</div>
                        )}
                        {manual && <div className={styles.ManualChip}>{t("ScheduledTasks.manual")}</div>}
                    </div>
                    {errorMessage && <div className={styles.ResultMessage}>{errorMessage}</div>}
                </div>
            </div>
        </>
    );
};
