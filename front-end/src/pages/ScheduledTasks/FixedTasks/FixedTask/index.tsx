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
import { EmptyHandler } from "components";
import type { IFixedTasks } from "models";

import styles from "../../styles.module.css";
import { FixedTaskTableHeader } from "../FixedTaskTableHeader";
import { FixedTaskTableRow } from "../FixedTaskTableRow";

interface IProps {
    /**
     * The title that represents the task type to be displayed.
     * It is expected to be already i18n translated.
     */
    taskTitle: string;

    /**
     * The list of tasks that have a fixed schedule execution timeline, i.e. they
     * are either fixed delay or fixed rate tasks.
     */
    fixedTasks: IFixedTasks[];
}

export const FixedTasks = ({ taskTitle, fixedTasks }: IProps) => {
    return (
        <div className={styles.SectionWrapper}>
            <div className={`TextMedium ${styles.TaskType}`}>{taskTitle}</div>

            <div className={styles.FixedTaskTable}>
                <FixedTaskTableHeader />

                <EmptyHandler isEmpty={fixedTasks.length === 0}>
                    {fixedTasks.map((task, index) => (
                        <FixedTaskTableRow task={task} key={index} />
                    ))}
                </EmptyHandler>
            </div>
        </div>
    );
};
