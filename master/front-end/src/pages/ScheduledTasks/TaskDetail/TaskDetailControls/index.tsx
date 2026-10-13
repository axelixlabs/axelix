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
import { Button, Popconfirm, Switch } from "antd";
import { useTranslation } from "react-i18next";

import type { IScheduledTask, ITriggerOverride } from "@/models";

import { EditTriggerPopover } from "./EditTriggerPopover";
import styles from "./styles.module.css";

interface IProps {
    task: IScheduledTask;
    onToggleEnabled: (enabled: boolean) => void;
    onEditTrigger: (override: ITriggerOverride) => void;
    onRunNow: () => void;
}

export const TaskDetailControls = ({ task, onToggleEnabled, onEditTrigger, onRunNow }: IProps) => {
    const { t } = useTranslation();

    const { enabled, running } = task;

    return (
        <>
            <div className={styles.MainWrapper}>
                {enabled ? (
                    <Popconfirm
                        title={t("ScheduledTasks.pauseTitle")}
                        description={t("ScheduledTasks.pauseDescription")}
                        okText={t("ScheduledTasks.pauseConfirm")}
                        cancelText={t("cancel")}
                        placement="bottomRight"
                        okButtonProps={{
                            className: styles.PopconfirmButtonStyles,
                        }}
                        onConfirm={() => onToggleEnabled(false)}
                    >
                        <Switch checked checkedChildren={t("on")} unCheckedChildren={t("off")} />
                    </Popconfirm>
                ) : (
                    <Switch
                        checked={false}
                        checkedChildren={t("on")}
                        unCheckedChildren={t("off")}
                        onChange={() => onToggleEnabled(true)}
                    />
                )}

                <EditTriggerPopover task={task} onApply={onEditTrigger} />

                <Button type="primary" loading={running} onClick={onRunNow}>
                    {running ? t("ScheduledTasks.running") : `▶ ${t("ScheduledTasks.runNow")}`}
                </Button>
            </div>
        </>
    );
};
