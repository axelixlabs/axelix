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

import { getCronDescription } from "@/helpers";
import type { TScheduledTaskItem } from "@/models";

import { EditTriggerPopover } from "../EditTriggerPopover";
import { TriggerTypeBadge } from "../TriggerTypeBadge";
import { getIntervalDescription } from "../getIntervalDescription";

import { RunTaskButton } from "./RunTaskButton";
import { TaskStatusSwitch } from "./TaskStatusSwitch";
import styles from "./styles.module.css";

interface IProps {
    item: TScheduledTaskItem;
    onChange: () => Promise<void>;
}

export const TaskHeader = ({ item, onChange }: IProps) => {
    const { t, i18n } = useTranslation();

    return (
        <div className={styles.Header}>
            <div className={styles.TitleBlock}>
                <span className={`${styles.Mono} ${styles.Method}`}>{item.task.methodName}</span>
                <span className={`${styles.Mono} ${styles.ClassName}`}>{item.task.className}</span>
                <span className={styles.TriggerLine}>
                    <TriggerTypeBadge type={item.type} />
                    {item.type === "cron"
                        ? getCronDescription(item.task.expression, i18n.language, t)
                        : getIntervalDescription(item.type, item.task.interval, i18n.language, t)}
                    {item.type === "cron" && (
                        <span className={`${styles.Mono} ${styles.TypeBadge} ${styles.Expression}`}>
                            {item.task.expression}
                        </span>
                    )}
                </span>
            </div>

            <div className={styles.Controls}>
                <TaskStatusSwitch task={item.task} onChange={onChange} />
                <EditTriggerPopover item={item} onChange={onChange} />
                <RunTaskButton trigger={item.task.taskId} />
            </div>
        </div>
    );
};
