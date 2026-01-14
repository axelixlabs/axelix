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
import { useTranslation } from "react-i18next";

import { EmptyHandler } from "components";
import type { ICron } from "models";

import styles from "../../styles.module.css";
import { CronTableHeader } from "../CronTableHeader";
import { CronTaskTableRow } from "../CronTaskTableRow";

interface IProps {
    /**
     * List of cron tasks to be rendered
     */
    cronTasks: ICron[];
}

export const CronTasks = ({ cronTasks }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.SectionWrapper}>
                <div className={`TextMedium ${styles.TaskType}`}>{t("ScheduledTasks.cron")}</div>

                <div className="CustomizedTable">
                    <CronTableHeader />
                    <EmptyHandler isEmpty={cronTasks.length === 0}>
                        {cronTasks.map((task, index) => (
                            <CronTaskTableRow task={task} key={index} />
                        ))}
                    </EmptyHandler>
                </div>
            </div>
        </>
    );
};
