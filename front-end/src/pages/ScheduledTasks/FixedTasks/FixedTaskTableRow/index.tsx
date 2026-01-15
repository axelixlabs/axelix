/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { Button } from "antd";
import RunIcon from "assets/icons/run.svg?react";

import { EditableValue, TooltipWithCopy } from "components";
import type { IFixedTasks } from "models";

import { ScheduledTasksStatusSwitch } from "../../ScheduledTasksStatusSwitch";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Task body for scheduled task types
     */
    task: IFixedTasks;
}

export const FixedTaskTableRow = ({ task }: IProps) => {
    return (
        <div className={`TableRow ${styles.FixedTaskTableRow}`}>
            <div className={`RowChunk ${styles.TooltipWrapperChunk}`}>
                <TooltipWithCopy text={task.runnable.target} />
            </div>
            <div className={`RowChunk ${styles.CenteredRowChunk}`}>{task.initialDelay}</div>
            <div className="RowChunk">
                <EditableValue initialValue={String(task.interval)} onNewValue={() => {}} />
            </div>
            <div className={`RowChunk ${styles.CenteredRowChunk}`}>
                <ScheduledTasksStatusSwitch runnable={task} />
            </div>
            <div className={`RowChunk ${styles.CenteredRowChunk}`}>
                <Button icon={<RunIcon />} type="primary" />
            </div>
        </div>
    );
};
