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

import { Alert, App, Button, Input, Popover, Tooltip } from "antd";
import { type ChangeEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { getCronDescription } from "helpers";
import { type ICron } from "models";
import { changeCronExpression, checkCronExpressionValid } from "services";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single cron data
     */
    task: ICron;
}

export const CronExpressionEditableValue = ({ task }: IProps) => {
    const { instanceId } = useParams();

    const { message } = App.useApp();
    const { t } = useTranslation();

    const [actualValue, setActualValue] = useState<string>(task.expression);
    const [tempValue, setTempValue] = useState<string>(task.expression);
    const [expressionUpdateLoading, setExpressionUpdateLoading] = useState<boolean>(false);
    const [isPopoverOpen, setIsPopoverOpen] = useState<boolean>(false);
    const [isCronExpressionValid, setIsCronExpressionValid] = useState<boolean>(true);

    const handleUpdate = async (): Promise<void> => {
        setExpressionUpdateLoading(true);

        const normalizedCronExpression = tempValue.trim();

        changeCronExpression({
            instanceId: instanceId!,
            newCronExpression: normalizedCronExpression,
            trigger: task.runnable.target,
        })
            .then(() => {
                message.success(t("ScheduledTasks.cronExpressionChangeSuccess"));
                setActualValue(normalizedCronExpression);
                setIsPopoverOpen(false);
            })
            .finally(() => {
                setExpressionUpdateLoading(false);
            });
    };

    const handleCancel = (): void => {
        setTempValue(actualValue);
        setIsPopoverOpen(false);
        setIsCronExpressionValid(true);
    };

    const handleOnInputChange = (e: ChangeEvent<HTMLInputElement>): void => {
        if (e.target.value.trim() !== tempValue.trim()) {
            checkCronExpressionValid(e.target.value).then((response) => {
                setIsCronExpressionValid(response.data.valid);
            });
        }

        setTempValue(e.target.value);
    };

    return (
        <>
            <div className={styles.MainWrapper}>
                <Tooltip title={getCronDescription(actualValue)}>{actualValue}</Tooltip>
                <Popover
                    title={t("ScheduledTasks.enterNewCron")}
                    open={isPopoverOpen}
                    onOpenChange={(newOpen) => {
                        setIsPopoverOpen(newOpen);
                        setTempValue(actualValue);
                        setIsCronExpressionValid(true);
                    }}
                    content={
                        <div className={styles.EditWrapper}>
                            <Tooltip title={getCronDescription(tempValue)}>
                                <Input
                                    value={tempValue}
                                    onChange={handleOnInputChange}
                                    disabled={expressionUpdateLoading}
                                    status={!isCronExpressionValid ? "error" : undefined}
                                />
                            </Tooltip>

                            <div className={styles.AlertWithActionsWrapper}>
                                <Alert
                                    title={isCronExpressionValid ? "Valid" : "Invalid"}
                                    type={isCronExpressionValid ? "success" : "error"}
                                    showIcon
                                    styles={{ root: { paddingBlock: "4px" } }}
                                />

                                <div className={styles.EditActionsButtonsWrapper}>
                                    <Button icon={<CloseOutlined />} type="primary" onClick={handleCancel} />

                                    <Button
                                        icon={<CheckOutlined />}
                                        type="primary"
                                        disabled={!isCronExpressionValid}
                                        onClick={handleUpdate}
                                        loading={expressionUpdateLoading}
                                    />
                                </div>
                            </div>
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
