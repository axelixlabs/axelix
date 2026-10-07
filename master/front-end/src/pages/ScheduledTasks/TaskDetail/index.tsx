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

import type { IScheduledTask, ITriggerOverride } from "@/models";

import { DurationChart } from "./DurationChart";
import { EmptyExecutionHistory } from "./EmptyExecutionHistory";
import { ExecutionsTable } from "./ExecutionsTable";
import { FailuresByType } from "./FailuresByType";
import { TaskDetailControls } from "./TaskDetailControls";
import { TaskDetailStats } from "./TaskDetailStats";
import { TaskDetailTitleBlock } from "./TaskDetailTitleBlock";
import styles from "./styles.module.css";

interface IProps {
    task: IScheduledTask;
    onToggleEnabled: (enabled: boolean) => void;
    onRunNow: () => void;
    onEditTrigger: (override: ITriggerOverride) => void;
    onlyFailed: boolean;
    setOnlyFailed: Dispatch<SetStateAction<boolean>>;
    page: number;
    setPage: Dispatch<SetStateAction<number>>;
}

export const TaskDetail = ({
    task,
    onToggleEnabled,
    onRunNow,
    onEditTrigger,
    onlyFailed,
    setOnlyFailed,
    page,
    setPage,
}: IProps) => {
    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.Header}>
                    <TaskDetailTitleBlock task={task} />

                    <TaskDetailControls
                        onEditTrigger={onEditTrigger}
                        onRunNow={onRunNow}
                        onToggleEnabled={onToggleEnabled}
                        task={task}
                    />
                </div>

                <TaskDetailStats task={task} />

                {task.hasHistory ? (
                    <>
                        <DurationChart runs={task.runs} maxDurationMs={task.maxDurationMs} />
                        <FailuresByType runs={task.runs} />
                        <ExecutionsTable
                            task={task}
                            onlyFailed={onlyFailed}
                            setOnlyFailed={setOnlyFailed}
                            page={page}
                            setPage={setPage}
                        />
                    </>
                ) : (
                    <EmptyExecutionHistory />
                )}
            </div>
        </>
    );
};
