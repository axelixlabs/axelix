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
import { Input } from "antd";
import type { Dispatch, SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import type { IScheduledTask } from "@/models";

import sharedStyles from "../../../../../shared.module.css";

import styles from "./styles.module.css";

interface IProps {
    task: IScheduledTask;
    isCron: boolean;
    intervalMs: string;
    cron: string;
    setCron: Dispatch<SetStateAction<string>>;
    setIntervalMs: Dispatch<SetStateAction<string>>;
}

export const EditTriggerPopoverContentCronExpression = ({
    isCron,
    intervalMs,
    task,
    cron,
    setCron,
    setIntervalMs,
}: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            {isCron ? (
                <div className={styles.Field}>
                    <span className={`TextUltraSmall ${styles.Label}`}>{t("ScheduledTasks.expression")}</span>
                    <Input
                        className={sharedStyles.Mono}
                        value={cron}
                        onChange={(event) => setCron(event.target.value)}
                    />
                    <span className={`TextUltraSmall ${styles.Help}`}>{t("ScheduledTasks.editCronHelp")}</span>
                </div>
            ) : (
                <div className={styles.Field}>
                    <span className={styles.Label}>{task.type}</span>
                    <Input
                        className={sharedStyles.Mono}
                        value={intervalMs}
                        suffix="ms"
                        onChange={(event) => setIntervalMs(event.target.value)}
                    />
                </div>
            )}
        </>
    );
};
