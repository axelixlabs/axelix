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
import { message } from "antd";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EditableValue, TooltipWithCopy } from "components";
import { type ICron } from "models";
import { changeCronExpression } from "services";

import { ScheduledTasksStatusSwitch } from "../../ScheduledTasksStatusSwitch";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Cron task to render
     */
    task: ICron;
}

export const CronTaskTableRow = ({ task }: IProps) => {
    const { instanceId } = useParams();
    const [messageApi, contextHolder] = message.useMessage();
    const { t } = useTranslation();

    return (
        <>
            {contextHolder}
            <div className={`TableRow ${styles.CronTaskTableRow}`}>
                <div className={`RowChunk ${styles.TooltipWrapperChunk}`}>
                    <TooltipWithCopy text={task.runnable.target} />
                </div>
                <div className="RowChunk">
                    <EditableValue
                        initialValue={task.expression}
                        onNewValue={(newValue) => {
                            changeCronExpression({
                                instanceId: instanceId!,
                                newCronExpression: newValue,
                            })
                                .then(() => {
                                    messageApi.success(t("ScheduledTasks.cronExpressionChangeSuccess"));
                                })
                                .catch(() => {
                                    messageApi.error(t("ScheduledTasks.cronExpressionChangeError"));
                                });
                        }}
                    />
                </div>
                <div className={`RowChunk ${styles.RowChunk}`}>
                    <ScheduledTasksStatusSwitch runnable={task} />
                </div>
            </div>
        </>
    );
};
