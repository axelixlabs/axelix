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
import { App } from "antd";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EditableValue, TooltipWithCopy } from "components";
import { type ICron } from "models";
import { changeCronExpression } from "services";

import { ForceRunTask } from "../../ForceRunTask";
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
    const { message } = App.useApp();
    const { t } = useTranslation();

    return (
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
                            trigger: task.runnable.target,
                        })
                            .then(() => {
                                message.success(t("ScheduledTasks.cronExpressionChangeSuccess"));
                            })
                            .catch(() => {
                                message.error(t("ScheduledTasks.cronExpressionChangeError"));
                            });
                    }}
                />
            </div>
            <div className={`RowChunk ${styles.CenteredRowChunk}`}>
                <ScheduledTasksStatusSwitch runnable={task} />
            </div>
            <div className={`RowChunk ${styles.CenteredRowChunk}`}>
                <ForceRunTask trigger={task.runnable.target} />
            </div>
        </div>
    );
};
