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
import { SearchOutlined } from "@ant-design/icons";

import { Input } from "antd";
import { useTranslation } from "react-i18next";

import { EmptyHandler } from "@/components";
import { SCHEDULED_TASK_TYPE_LABEL_KEYS, isEmpty } from "@/helpers";
import type { IScheduledTask, IScheduledTasksResponseBody, TScheduledTaskType } from "@/models";

import shared from "../shared.module.css";
import { HEALTHY_SUCCESS_RATE, getSuccessRateClass } from "../taskStyles";

import styles from "./styles.module.css";

interface IProps {
    scheduledTasks: IScheduledTasksResponseBody;
    selectedTaskId?: string;
    onSelect: (taskId: string) => void;
    search: string;
    onSearchChange: (search: string) => void;
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

export const TaskList = ({ scheduledTasks, selectedTaskId, onSelect, search, onSearchChange }: IProps) => {
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
            <div className={styles.Header}>
                <h2 className={styles.Title}>{t("Sider.scheduledTasks")}</h2>
                <Input
                    className={styles.Search}
                    prefix={<SearchOutlined />}
                    allowClear
                    value={search}
                    onChange={(event) => onSearchChange(event.target.value)}
                    aria-label={t("ScheduledTasks.searchTasks")}
                    placeholder={t("ScheduledTasks.searchTasks")}
                />
            </div>

            <EmptyHandler isEmpty={isEmpty(scheduledTasks)}>
                <div className={`ThinScrollbar ${styles.Groups}`} key={search.trim() ? "search" : "browse"}>
                    {TASK_TYPES.map((type) => (
                        <details key={type} hidden={scheduledTasks[type].length === 0} open>
                            <summary className={styles.GroupTitle}>
                                {t(SCHEDULED_TASK_TYPE_LABEL_KEYS[type])} · {scheduledTasks[type].length}
                            </summary>

                            {scheduledTasks[type].map((task) => {
                                return (
                                    <button
                                        type="button"
                                        key={task.taskId}
                                        title={`${task.className}.${task.methodName}`}
                                        onClick={() => onSelect(task.taskId)}
                                        className={styles.Row}
                                        aria-pressed={task.taskId === selectedTaskId}
                                    >
                                        <span className={`${styles.Dot} ${getDotClass(task)}`} />
                                        <span className={styles.Labels}>
                                            <span className={styles.Method}>{task.methodName}</span>
                                            <span className={`${shared.Mono} ${styles.ClassName}`}>
                                                {task.className.slice(task.className.lastIndexOf(".") + 1)}
                                            </span>
                                        </span>
                                        <span
                                            className={`${shared.Mono} ${styles.Rate} ${getSuccessRateClass(getRoundedSuccessRate(task), task.enabled)}`}
                                        >
                                            {getRateLabel(task)}
                                        </span>
                                    </button>
                                );
                            })}
                        </details>
                    ))}
                </div>
            </EmptyHandler>
        </div>
    );
};
