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
import { CheckOutlined, CloseOutlined, EditOutlined } from "@ant-design/icons";

import { App, Button, Input, Popover } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { type IFixedTasks } from "models";
import { changeScheduledTaskInterval } from "services";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Task body for scheduled task types
     */
    task: IFixedTasks;
}

export const FixedTasksEditableValue = ({ task }: IProps) => {
    const { instanceId } = useParams();
    const { message } = App.useApp();
    const { t } = useTranslation();

    const [actualValue, setActualValue] = useState<string>(task.interval.toString());
    const [tempValue, setTempValue] = useState<string>(task.interval.toString());
    const [loading, setLoading] = useState<boolean>(false);
    const [isPopoverOpen, setIsPopoverOpen] = useState<boolean>(false);
    const [isValidInterval, setIsValidInterval] = useState<boolean>(true);

    const handleUpdate = async (): Promise<void> => {
        setLoading(true);

        const normalizedTempValue = tempValue.trim();

        changeScheduledTaskInterval({
            instanceId: instanceId!,
            interval: +normalizedTempValue,
            trigger: task.runnable.target,
        })
            .then(() => {
                message.success(t("ScheduledTasks.fixedTaskIntervalChangeSuccess"));
                setActualValue(normalizedTempValue);
                setIsPopoverOpen(false);
            })
            .finally(() => {
                setLoading(false);
            });
    };

    const handleCancel = (): void => {
        setTempValue(actualValue);
        setIsPopoverOpen(false);
    };

    const handleInputChange = (newVal: string): void => {
        if (isNaN(Number(newVal)) || newVal.trim().length == 0) {
            setIsValidInterval(false);
        } else {
            setIsValidInterval(true);
        }

        setTempValue(newVal);
    };

    return (
        <>
            <div className={styles.IntervalPreviewWrapper}>
                {actualValue}
                <Popover
                    title={t("ScheduledTasks.enterNewInterval")}
                    open={isPopoverOpen}
                    onOpenChange={(newOpen) => {
                        setIsPopoverOpen(newOpen);
                        setTempValue(actualValue);
                    }}
                    content={
                        <div className={styles.EditWrapper}>
                            <Input
                                value={tempValue}
                                onChange={(e) => handleInputChange(e.target.value)}
                                status={isValidInterval ? "success" : "error"}
                                disabled={loading}
                            />

                            <Button
                                icon={<CloseOutlined />}
                                type="primary"
                                onClick={handleCancel}
                                className={styles.EditActionButtons}
                            />

                            <Button
                                icon={<CheckOutlined />}
                                type="primary"
                                disabled={!isValidInterval}
                                onClick={handleUpdate}
                                loading={loading}
                                className={styles.EditActionButtons}
                            />
                        </div>
                    }
                    styles={{
                        container: {
                            minWidth: "300px",
                        },
                    }}
                    trigger="click"
                >
                    <Button icon={<EditOutlined />} type="primary" />
                </Popover>
            </div>
        </>
    );
};
