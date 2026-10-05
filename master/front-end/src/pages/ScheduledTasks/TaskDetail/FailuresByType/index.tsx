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

import { type IScheduledTaskExecution, formatRelative } from "../../mock";
import shared from "../../shared.module.css";

import styles from "./styles.module.css";

interface IProps {
    runs: IScheduledTaskExecution[];
}

interface IFailureGroup {
    errorType: string;
    errorMessage: string;
    count: number;
    lastAgoSeconds: number;
}

const groupFailures = (runs: IScheduledTaskExecution[]): IFailureGroup[] => {
    const byType = new Map<string, IFailureGroup>();

    runs.filter((run) => !run.succeeded).forEach((run) => {
        const type = run.errorType ?? "";
        const existing = byType.get(type);

        if (existing) {
            existing.count += 1;
            existing.lastAgoSeconds = Math.min(existing.lastAgoSeconds, run.agoSeconds);
        } else {
            byType.set(type, {
                errorType: type,
                errorMessage: run.errorMessage,
                count: 1,
                lastAgoSeconds: run.agoSeconds,
            });
        }
    });

    return [...byType.values()].sort((a, b) => b.count - a.count);
};

export const FailuresByType = ({ runs }: IProps) => {
    const { t } = useTranslation();

    const groups = groupFailures(runs);

    if (groups.length === 0) {
        return null;
    }

    return (
        <div className={styles.MainWrapper}>
            <span className={styles.Title}>{t("ScheduledTasks.failuresByType")}</span>

            {groups.map((group) => (
                <div className={styles.Row} key={group.errorType}>
                    <span className={`${shared.Mono} ${styles.Count}`}>×{group.count}</span>
                    <span className={styles.Details}>
                        <span className={`${shared.Mono} ${styles.ErrorType}`}>{group.errorType}</span>
                        <span className={styles.ErrorMessage}>{group.errorMessage}</span>
                    </span>
                    <span className={styles.Last}>
                        {t("ScheduledTasks.last")} {formatRelative(group.lastAgoSeconds)}
                    </span>
                </div>
            ))}
        </div>
    );
};
