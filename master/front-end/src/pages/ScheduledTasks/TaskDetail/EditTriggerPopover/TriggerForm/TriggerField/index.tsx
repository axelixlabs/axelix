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
import { useTranslation } from "react-i18next";

import type { TScheduledTaskType } from "@/models";

import shared from "../../../../shared.module.css";
import { TriggerTypeBadge } from "../../../TriggerTypeBadge";
import styles from "../styles.module.css";

interface IProps {
    type: TScheduledTaskType;
    value: string;
    isValid: boolean;
    loading: boolean;
    onChange: (value: string) => void;
}

const TYPE_NOTE_KEY: Record<TScheduledTaskType, string> = {
    cron: "ScheduledTasks.editCronNote",
    fixedDelay: "ScheduledTasks.editDelayNote",
    fixedRate: "ScheduledTasks.editRateNote",
};

export const TriggerField = ({ type, value, isValid, loading, onChange }: IProps) => {
    const { t } = useTranslation();
    const isCron = type === "cron";

    return (
        <>
            <div className={styles.TypeNote}>
                <TriggerTypeBadge type={type} />
                {t(TYPE_NOTE_KEY[type])}
            </div>

            <div className={styles.Field}>
                <span className={styles.Label}>
                    {isCron ? t("ScheduledTasks.expression") : t("ScheduledTasks.interval")}
                </span>
                <Input
                    className={shared.Mono}
                    value={value}
                    status={isValid ? undefined : "error"}
                    disabled={loading}
                    onChange={(event) => onChange(event.target.value)}
                />
                {isCron && <span className={styles.Help}>{t("ScheduledTasks.editCronHelp")}</span>}
            </div>
        </>
    );
};
