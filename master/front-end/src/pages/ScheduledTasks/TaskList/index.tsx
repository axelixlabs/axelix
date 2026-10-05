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
import { Input } from "antd";
import { useTranslation } from "react-i18next";

import shared from "../shared.module.css";
import type { ETriggerType, IScheduledTask } from "../taskModel";
import { successRateClass } from "../taskStyles";

import styles from "./styles.module.css";

interface IProps {
    groups: { type: ETriggerType; tasks: IScheduledTask[] }[];
    selectedKey: string;
    onSelect: (key: string) => void;
    search: string;
    onSearch: (value: string) => void;
}

const GROUP_LABEL_KEY: Record<ETriggerType, string> = {
    Cron: "ScheduledTasks.cron",
    "Fixed delay": "ScheduledTasks.fixedDelay",
    "Fixed rate": "ScheduledTasks.fixedRate",
};

export const TaskList = ({ groups, selectedKey, onSelect, search, onSearch }: IProps) => {
    const { t } = useTranslation();

    const dotClass = (task: IScheduledTask): string => {
        if (!task.enabled) {
            return styles.DotOff;
        }

        if (!task.hasHistory) {
            return styles.DotEmpty;
        }

        return task.lastRun?.succeeded ? styles.DotOk : styles.DotBad;
    };

    const rateLabel = (task: IScheduledTask): string => {
        if (!task.enabled) {
            return t("off");
        }

        return task.hasHistory ? `${task.successRate}%` : "—";
    };

    return (
        <div className={styles.MainWrapper}>
            <Input
                allowClear
                value={search}
                placeholder={t("ScheduledTasks.searchTasks")}
                onChange={(event) => onSearch(event.target.value)}
            />

            <div className={styles.Groups}>
                {groups.map((group) => (
                    <div className={styles.Group} key={group.type}>
                        <span className={styles.GroupTitle}>
                            {t(GROUP_LABEL_KEY[group.type])} · {group.tasks.length}
                        </span>

                        {group.tasks.map((task) => (
                            <button
                                type="button"
                                key={task.key}
                                title={task.fqn}
                                onClick={() => onSelect(task.key)}
                                className={`${styles.Row} ${task.key === selectedKey ? styles.Selected : ""}`}
                            >
                                <span className={`${styles.Dot} ${dotClass(task)}`} />
                                <span className={styles.Labels}>
                                    <span className={`${shared.Mono} ${styles.Method}`}>{task.method}</span>
                                    <span className={`${shared.Mono} ${styles.ClassName}`}>{task.simpleClassName}</span>
                                </span>
                                <span className={`${shared.Mono} ${styles.Rate} ${successRateClass(shared, task)}`}>
                                    {rateLabel(task)}
                                </span>
                            </button>
                        ))}
                    </div>
                ))}
            </div>
        </div>
    );
};
