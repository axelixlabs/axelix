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
import { Popover } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import type { IScheduledTask, ITriggerOverride } from "@/models";

import { EditTriggerPopoverContent } from "./EditTriggerPopoverContent";
import styles from "./styles.module.css";

interface IProps {
    task: IScheduledTask;
    onApply: (override: ITriggerOverride) => void;
}

const firstNumber = (raw: string): string => {
    return (raw.match(/\d+/g) ?? ["2000"])[0];
};

export const EditTriggerPopover = ({ task, onApply }: IProps) => {
    const { t } = useTranslation();

    const [open, setOpen] = useState<boolean>(false);
    const [cron, setCron] = useState<string>(task.raw);
    const [intervalMs, setIntervalMs] = useState<string>(firstNumber(task.raw));

    const handleOpenChange = (next: boolean): void => {
        if (next) {
            setCron(task.raw);
            setIntervalMs(firstNumber(task.raw));
        }

        setOpen(next);
    };

    return (
        <>
            <Popover
                open={open}
                onOpenChange={handleOpenChange}
                trigger="click"
                placement="bottomRight"
                title={t("ScheduledTasks.editTrigger")}
                content={
                    <EditTriggerPopoverContent
                        task={task}
                        cron={cron}
                        intervalMs={intervalMs}
                        onApply={onApply}
                        setOpen={setOpen}
                        setCron={setCron}
                        setIntervalMs={setIntervalMs}
                    />
                }
            >
                <div className={`TextUltraSmall ${styles.PopoverTrigger} ${open ? styles.PopoverTriggerOpen : ""}`}>
                    ✎ {t("ScheduledTasks.editTrigger")}
                </div>
            </Popover>
        </>
    );
};
