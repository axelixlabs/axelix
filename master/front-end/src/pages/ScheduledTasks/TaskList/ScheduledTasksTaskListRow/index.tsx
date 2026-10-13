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

import type { IScheduledTask } from "@/models";

import sharedStyles from "../../shared.module.css";
import { successRateClass } from "../../taskStyles";

import styles from "./styles.module.css";

interface IProps {
    task: IScheduledTask;
    onSelect: (key: string) => void;
    selectedKey: string;
}

export const ScheduledTasksTaskListRow = ({ task, onSelect, selectedKey }: IProps) => {
    const { t } = useTranslation();

    const { key, fqn, method, simpleClassName } = task;

    const dotClass = (task: IScheduledTask): string => {
        const { enabled, hasHistory, lastRun } = task;

        if (!enabled) {
            return styles.DotOff;
        }

        if (!hasHistory) {
            return styles.DotEmpty;
        }

        if (lastRun?.succeeded) {
            return styles.DotOk;
        }

        return styles.DotBad;
    };

    const rateLabel = (task: IScheduledTask): string => {
        const { enabled, hasHistory, successRate } = task;

        if (!enabled) {
            return t("off");
        }

        return hasHistory ? `${successRate}%` : "—";
    };

    return (
        <>
            <div
                title={fqn}
                onClick={() => onSelect(key)}
                className={`TextUltraSmall ${styles.Row} ${key === selectedKey ? styles.Selected : ""}`}
            >
                <div className={`${styles.Dot} ${dotClass(task)}`} />
                <div className={styles.Labels}>
                    <div className={`${sharedStyles.Mono} ${styles.Method}`}>{method}</div>
                    <div className={`${sharedStyles.Mono} ${styles.ClassName}`}>{simpleClassName}</div>
                </div>
                <div className={`${sharedStyles.Mono} ${styles.Rate} ${successRateClass(sharedStyles, task)}`}>
                    {rateLabel(task)}
                </div>
            </div>
        </>
    );
};
