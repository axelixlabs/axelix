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
import { Button, Input, Popover } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { getCronDescription } from "@/helpers";

import { formatDuration } from "../../mock";
import shared from "../../shared.module.css";
import type { IScheduledTask, ITriggerOverride } from "../../taskModel";
import { triggerTypeClass } from "../../taskStyles";

import styles from "./styles.module.css";

interface IProps {
    task: IScheduledTask;
    onApply: (override: ITriggerOverride) => void;
}

interface IDerivedTrigger {
    valid: boolean;
    human: string;
    raw: string;
    error: string;
}

const CRON_INVALID = "Invalid cron expression";

const firstNumber = (raw: string): string => (raw.match(/\d+/g) ?? ["2000"])[0];

const initialDelay = (raw: string): string => (raw.match(/\d+/g) ?? ["0", "0"])[1] ?? "0";

export const EditTriggerPopover = ({ task, onApply }: IProps) => {
    const { t } = useTranslation();

    const isCron = task.type === "Cron";

    const [open, setOpen] = useState(false);
    const [cron, setCron] = useState(task.raw);
    const [intervalMs, setIntervalMs] = useState(firstNumber(task.raw));

    const handleOpenChange = (next: boolean): void => {
        if (next) {
            setCron(task.raw);
            setIntervalMs(firstNumber(task.raw));
        }

        setOpen(next);
    };

    const derive = (): IDerivedTrigger => {
        if (isCron) {
            const trimmed = cron.trim();
            const fields = trimmed ? trimmed.split(/\s+/) : [];

            if (fields.length !== 6) {
                return { valid: false, human: "", raw: trimmed, error: t("ScheduledTasks.editCronFieldsError") };
            }

            const human = getCronDescription(trimmed);

            if (human === CRON_INVALID) {
                return { valid: false, human: "", raw: trimmed, error: t("ScheduledTasks.editCronInvalidError") };
            }

            return { valid: true, human, raw: trimmed, error: "" };
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

    const content = (
        <div className={styles.Form}>
            <div className={styles.TypeNote}>
                <span className={`${shared.TypeBadge} ${triggerTypeClass(shared, task.type)}`}>{task.type}</span>
                {isCron
                    ? t("ScheduledTasks.editCronNote")
                    : task.type === "Fixed delay"
                      ? t("ScheduledTasks.editDelayNote")
                      : t("ScheduledTasks.editRateNote")}
            </div>

            {isCron ? (
                <div className={styles.Field}>
                    <span className={styles.Label}>{t("ScheduledTasks.expression")}</span>
                    <Input className={shared.Mono} value={cron} onChange={(event) => setCron(event.target.value)} />
                    <span className={styles.Help}>{t("ScheduledTasks.editCronHelp")}</span>
                </div>
            ) : (
                <div className={styles.Field}>
                    <span className={styles.Label}>{task.type}</span>
                    <Input
                        className={shared.Mono}
                        value={intervalMs}
                        suffix="ms"
                        onChange={(event) => setIntervalMs(event.target.value)}
                    />
                </div>
            )}

            <div className={`${styles.Preview} ${derived.valid ? styles.PreviewOk : styles.PreviewBad}`}>
                <b>{derived.valid ? t("ScheduledTasks.editPreviewRuns") : t("ScheduledTasks.editPreviewInvalid")}</b>
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
    );

    return (
        <Popover
            open={open}
            onOpenChange={handleOpenChange}
            trigger="click"
            placement="bottomRight"
            title={t("ScheduledTasks.editTrigger")}
            content={content}
        >
            <button type="button" className={`${styles.TriggerButton} ${open ? styles.TriggerButtonOpen : ""}`}>
                ✎ {t("ScheduledTasks.editTrigger")}
            </button>
        </Popover>
    );
};
