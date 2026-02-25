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
import { TooltipWithCopy } from "components";
import type { IFixedTasks } from "models";

import { FixedTasksEditableValue } from "../../FixedTasksEditableValue";
import { ForceRunTask } from "../../ForceRunTask";
import { ScheduledTasksStatusSwitch } from "../../ScheduledTasksStatusSwitch";
import styles from "../../styles.module.css";

interface IProps {
    /**
     * Task body for scheduled task types
     */
    task: IFixedTasks;
}

export const FixedTaskTableRow = ({ task }: IProps) => {
    return (
        <>
            <div className={styles.RowChunksWrapper}>
                <div className={styles.BodyRowChunk}>
                    <TooltipWithCopy text={task.runnable.target} />
                </div>
                <div className={styles.BodyRowChunk}>{task.initialDelay}</div>
                <div className={styles.BodyRowChunk}>
                    <FixedTasksEditableValue task={task} />
                </div>
                <div className={styles.BodyRowChunk}>
                    <ScheduledTasksStatusSwitch runnable={task} />
                </div>
                <div className={styles.BodyRowChunk}>
                    <ForceRunTask trigger={task.runnable.target} />
                </div>
            </div>
        </>
    );
};
