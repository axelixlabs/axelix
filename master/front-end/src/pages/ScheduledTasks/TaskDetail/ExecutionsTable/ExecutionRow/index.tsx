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

import { formatTransactionDuration } from "@/helpers";
import type { IScheduledTaskExecutionRecord } from "@/models";

import shared from "../../../shared.module.css";
import styles from "../styles.module.css";

interface IProps {
    execution: IScheduledTaskExecutionRecord;
}

const formatStartedAt = (startedAt: string): string => {
    const started = dayjs(startedAt);

    return started.format(started.isSame(dayjs(), "day") ? "HH:mm:ss" : "DD.MM HH:mm:ss");
};
export const ExecutionRow = ({ execution }: IProps) => {
    const { t } = useTranslation();

    return (
        <div className={`${styles.Row} ${execution.success ? "" : styles.RowFailed}`}>
            <span className={shared.Mono}>{formatStartedAt(execution.startedAt)}</span>
            <span className={`${shared.Mono} ${styles.Instance}`}>{execution.instanceName}</span>
            <span className={`${shared.Mono} ${styles.Duration}`}>
                {formatTransactionDuration(execution.durationMillis)}
            </span>
            {execution.success ? (
                <span className={styles.ResultSucceeded}>✓ {t("ScheduledTasks.succeeded")}</span>
            ) : (
                <span className={styles.Result}>
                    <span className={`${shared.Mono} ${styles.ResultFailed}`}>
                        ✕ {execution.errorType ?? t("ScheduledTasks.failed")}
                    </span>
                    {execution.errorMessage && <span className={styles.ResultMessage}>{execution.errorMessage}</span>}
                </span>
            )}
        </div>
    );
};
