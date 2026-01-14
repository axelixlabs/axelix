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

            <div className="CustomizedTable">
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
