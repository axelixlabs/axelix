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
import type { Dispatch, SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import { EmptyHandler, PageSearch } from "@/components";
import { SCHEDULED_TASK_TYPE_LABEL_KEYS, isEmpty, parseTaskId } from "@/helpers";
import type { IScheduledTask, IScheduledTasksResponseBody, TScheduledTaskType } from "@/models";

import shared from "../shared.module.css";
import { HEALTHY_SUCCESS_RATE, getSuccessRateClass } from "../taskStyles";

import styles from "./styles.module.css";

interface IProps {
    scheduledTasks: IScheduledTasksResponseBody;
    selectedTaskId?: string;
    onSelect: (taskId: string) => void;
    setSearch: Dispatch<SetStateAction<string>>;
}

const TASK_TYPES = Object.keys(SCHEDULED_TASK_TYPE_LABEL_KEYS) as TScheduledTaskType[];

const getRoundedSuccessRate = ({ successRate }: IScheduledTask): number | null =>
    successRate === null ? null : Math.floor(successRate);

const getDotClass = (task: IScheduledTask): string => {
    const successRate = getRoundedSuccessRate(task);

    if (!task.enabled) {
        return styles.DotOff;
    }

    if (successRate === null) {
        return styles.DotEmpty;
    }

    return successRate >= HEALTHY_SUCCESS_RATE ? styles.DotOk : styles.DotBad;
};

export const TaskList = ({ scheduledTasks, selectedTaskId, onSelect, setSearch }: IProps) => {
    const { t } = useTranslation();

    const getRateLabel = (task: IScheduledTask): string => {
        const successRate = getRoundedSuccessRate(task);

        if (!task.enabled) {
            return t("off");
        }

        return successRate === null ? "—" : `${successRate}%`;
    };

    return (
        <div className={styles.MainWrapper}>
            <PageSearch setSearch={setSearch} placeholder={t("ScheduledTasks.searchTasks")} removeBottomGutter />

            <EmptyHandler isEmpty={isEmpty(scheduledTasks)}>
                <div className={styles.Groups}>
                    {TASK_TYPES.filter((type) => scheduledTasks[type].length > 0).map((type) => (
                        <div className={styles.Group} key={type}>
                            <span className={styles.GroupTitle}>
                                {t(SCHEDULED_TASK_TYPE_LABEL_KEYS[type])} · {scheduledTasks[type].length}
                            </span>

                            {scheduledTasks[type].map((task) => {
                                const { method, suffix, className } = parseTaskId(task.taskId);

                                return (
                                    <button
                                        type="button"
                                        key={task.taskId}
                                        title={task.taskId}
                                        onClick={() => onSelect(task.taskId)}
                                        className={`${styles.Row} ${task.taskId === selectedTaskId ? styles.Selected : ""}`}
                                    >
                                        <span className={`${styles.Dot} ${getDotClass(task)}`} />
                                        <span className={styles.Labels}>
                                            <span className={styles.MethodLine}>
                                                <span className={`${shared.Mono} ${styles.Method}`}>{method}</span>
                                                <span className={`${shared.Mono} ${styles.Suffix}`}>{suffix}</span>
                                            </span>
                                            <span className={`${shared.Mono} ${styles.ClassName}`}>{className}</span>
                                        </span>
                                        <span
                                            className={`${shared.Mono} ${styles.Rate} ${getSuccessRateClass(getRoundedSuccessRate(task), task.enabled)}`}
                                        >
                                            {getRateLabel(task)}
                                        </span>
                                    </button>
                                );
                            })}
                        </div>
                    ))}
                </div>
            </EmptyHandler>
        </div>
    );
};
