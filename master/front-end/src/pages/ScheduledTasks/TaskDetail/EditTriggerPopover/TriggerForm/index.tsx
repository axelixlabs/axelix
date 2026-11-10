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
import { App, Button } from "antd";
import type { AxiosError } from "axios";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";

import { extractErrorCode } from "@/helpers";
import { type IErrorResponse, StatelessRequest, type TScheduledTaskItem } from "@/models";
import { changeCronExpression, changeScheduledTaskInterval, checkCronExpressionValid } from "@/services";

import { TriggerField } from "./TriggerField";
import { TriggerPreview } from "./TriggerPreview";
import styles from "./styles.module.css";

interface IProps {
    item: TScheduledTaskItem;
    onChange: () => Promise<void>;
    onClose: () => void;
}

const isIntervalValid = (value: string): boolean => /^[1-9]\d*$/.test(value) && Number.isSafeInteger(Number(value));

const updateTrigger = (item: TScheduledTaskItem, instanceId: string, value: string) =>
    item.type === "cron"
        ? changeCronExpression({ instanceId, newCronExpression: value, trigger: item.task.taskId })
        : changeScheduledTaskInterval({ instanceId, interval: Number(value), trigger: item.task.taskId });

export const TriggerForm = ({ item, onChange, onClose }: IProps) => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
    const { message } = App.useApp();

    const isCron = item.type === "cron";
    const currentValue = item.type === "cron" ? item.task.expression : String(item.task.interval);

    const [value, setValue] = useState(currentValue);
    const [isCronValid, setIsCronValid] = useState(true);
    const [updateRequest, setUpdateRequest] = useState(StatelessRequest.inactive());

    const normalizedValue = value.trim();
    const isValid = isCron ? isCronValid : isIntervalValid(normalizedValue);

    const handleValueChange = (newValue: string): void => {
        if (isCron && newValue.trim() !== normalizedValue) {
            checkCronExpressionValid(newValue)
                .then((response) => {
                    setIsCronValid(response.data.valid);
                })
                .catch(() => {
                    setIsCronValid(false);
                });
        }

        setValue(newValue);
    };

    const applyTrigger = (): void => {
        setUpdateRequest(StatelessRequest.loading());

        updateTrigger(item, instanceId!, normalizedValue)
            .then(() => {
                message.success(
                    isCron
                        ? t("ScheduledTasks.cronExpressionChangeSuccess")
                        : t("ScheduledTasks.fixedTaskIntervalChangeSuccess"),
                );

                return onChange();
            })
            .then(onClose)
            .catch((error: AxiosError<IErrorResponse>) => {
                setUpdateRequest(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    return (
        <div className={styles.Form}>
            <TriggerField
                type={item.type}
                value={value}
                isValid={isValid}
                loading={updateRequest.loading}
                onChange={handleValueChange}
            />

            <TriggerPreview type={item.type} value={normalizedValue} isValid={isValid} />

            <div className={styles.Footer}>
                <Button size="small" onClick={onClose}>
                    {t("cancel")}
                </Button>
                <Button
                    size="small"
                    type="primary"
                    disabled={!isValid || normalizedValue === currentValue}
                    loading={updateRequest.loading}
                    onClick={applyTrigger}
                >
                    {t("ScheduledTasks.applyTrigger")}
                </Button>
            </div>
        </div>
    );
};
