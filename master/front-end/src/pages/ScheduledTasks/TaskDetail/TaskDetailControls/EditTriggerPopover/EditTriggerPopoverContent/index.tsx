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
import { Button } from "antd";
import type { Dispatch, SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import { getCronDescription } from "@/helpers";
import { EScheduledTasksTriggerType, type IDerivedTrigger, type IScheduledTask, type ITriggerOverride } from "@/models";
import { formatDuration } from "@/pages/ScheduledTasks/mock";
import { triggerTypeClass } from "@/pages/ScheduledTasks/taskStyles";
import { CRON_INVALID } from "@/utils";

import sharedStyles from "../../../../shared.module.css";

import { EditTriggerPopoverContentCronExpression } from "./EditTriggerPopoverContentCronExpression";
import styles from "./styles.module.css";

interface IProps {
    task: IScheduledTask;
    cron: string;
    intervalMs: string;
    onApply: (override: ITriggerOverride) => void;
    setOpen: Dispatch<SetStateAction<boolean>>;
    setCron: Dispatch<SetStateAction<string>>;
    setIntervalMs: Dispatch<SetStateAction<string>>;
}

const initialDelay = (raw: string): string => {
    return (raw.match(/\d+/g) ?? ["0", "0"])[1] ?? "0";
};

export const EditTriggerPopoverContent = ({
    task,
    cron,
    intervalMs,
    onApply,
    setOpen,
    setCron,
    setIntervalMs,
}: IProps) => {
    const { t } = useTranslation();

    const isCron = task.type === EScheduledTasksTriggerType.CRON;

    const derive = (): IDerivedTrigger => {
        if (isCron) {
            const trimmed = cron.trim();
            const fields = trimmed ? trimmed.split(/\s+/) : [];

            if (fields.length !== 6) {
                return {
                    valid: false,
                    human: "",
                    raw: trimmed,
                    error: t("ScheduledTasks.editCronFieldsError"),
                };
            }

            const human = getCronDescription(trimmed);

            if (human === CRON_INVALID) {
                return {
                    valid: false,
                    human: "",
                    raw: trimmed,
                    error: t("ScheduledTasks.editCronInvalidError"),
                };
            }

            return {
                valid: true,
                human: human,
                raw: trimmed,
                error: "",
            };
        }

        const value = Number(intervalMs);

        if (!Number.isInteger(value) || value <= 0) {
            return { valid: false, human: "", raw: "", error: t("ScheduledTasks.editIntervalError") };
        }

        const duration = formatDuration(value);
        const prefix = task.type === "Fixed delay" ? "delay" : "rate";

        return {
            valid: true,
            human:
                task.type === "Fixed delay"
                    ? t("ScheduledTasks.fixedDelayPreview", { duration })
                    : t("ScheduledTasks.fixedRatePreview", { duration }),
            raw: `${prefix} ${intervalMs} · initial ${initialDelay(task.raw)}`,
            error: "",
        };
    };

    const derived = derive();

    const apply = (): void => {
        if (!derived.valid) {
            return;
        }

        onApply({ human: derived.human, raw: derived.raw });
        setOpen(false);
    };

    const getEditNote = (): string => {
        if (isCron) {
            return t("ScheduledTasks.editCronNote");
        }

        if (task.type === EScheduledTasksTriggerType.FIXED_DELAY) {
            return t("ScheduledTasks.editDelayNote");
        }

        return t("ScheduledTasks.editRateNote");
    };

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={`TextUltraSmall ${styles.TypeNote}`}>
                    <span className={`${sharedStyles.TypeBadge} ${triggerTypeClass(sharedStyles, task.type)}`}>
                        {task.type}
                    </span>

                    {getEditNote()}
                </div>

                <EditTriggerPopoverContentCronExpression
                    cron={cron}
                    intervalMs={intervalMs}
                    isCron={isCron}
                    setCron={setCron}
                    setIntervalMs={setIntervalMs}
                    task={task}
                />

                <div
                    className={`TextUltraSmall ${styles.Preview} ${derived.valid ? styles.PreviewOk : styles.PreviewBad}`}
                >
                    <b>
                        {derived.valid ? t("ScheduledTasks.editPreviewRuns") : t("ScheduledTasks.editPreviewInvalid")}
                    </b>
                    {derived.valid ? derived.human : derived.error}
                </div>

                <div className={styles.Footer}>
                    <Button size="small" onClick={() => setOpen(false)}>
                        {t("cancel")}
                    </Button>
                    <Button size="small" type="primary" disabled={!derived.valid} onClick={apply}>
                        {t("ScheduledTasks.applyTrigger")}
                    </Button>
                </div>
            </div>
        </>
    );
};
