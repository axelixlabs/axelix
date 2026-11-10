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

import { formatRelativeTime } from "@/helpers";
import type { IScheduledTaskFailuresByErrorType } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    failures: IScheduledTaskFailuresByErrorType[];
}

export const FailuresByType = ({ failures }: IProps) => {
    const { t, i18n } = useTranslation();

    if (failures.length === 0) {
        return null;
    }

    return (
        <div className={styles.MainWrapper}>
            <span className={styles.Title}>{t("ScheduledTasks.failuresByType")}</span>

            {failures.map(({ errorType, count, lastErrorMessage, lastOccurredAt }) => (
                <div className={styles.Row} key={errorType}>
                    <span className={`${styles.Mono} ${styles.Count}`}>×{count}</span>
                    <span className={styles.Details}>
                        <span className={`${styles.Mono} ${styles.ErrorType}`}>{errorType}</span>
                        {lastErrorMessage && <span className={styles.ErrorMessage}>{lastErrorMessage}</span>}
                    </span>
                    <span className={styles.Last}>
                        {t("ScheduledTasks.last")} {formatRelativeTime(lastOccurredAt, i18n.language)}
                    </span>
                </div>
            ))}
        </div>
    );
};
