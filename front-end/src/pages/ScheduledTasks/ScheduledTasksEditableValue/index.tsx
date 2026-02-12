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
import type { AxiosResponse } from "axios";
import { OptionalTooltip } from "pages/ScheduledTasks/OptionalTooltip";
import { useState } from "react";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Initial value.
     */
    initialValue: string;

    /**
     * Callback to invoke when the value chane accepted.
     * @param value the new value after change.
     */
    onNewValue: (value: string) => Promise<AxiosResponse<any>>; // TODO: Fix the type in future

    /**
     * Function to generate a tooltip for the value.
     * Receives the current value and returns a string to display.
     */
    tooltipFormatter?: (value: string) => string;

    /**
     * Message to show when the update succeeds.
     */
    successMessage: string;
}

export const ScheduledTasksEditableValue = ({ initialValue, successMessage, tooltipFormatter, onNewValue }: IProps) => {
    const { message } = App.useApp();

    const [actualValue, setActualValue] = useState<string>(initialValue);
    const [tempValue, setTempValue] = useState<string>(initialValue);
    const [loading, setLoading] = useState<boolean>(false);
    const [isPopoverOpen, setIsPopoverOpen] = useState<boolean>(false);

    const handleUpdate = async (): Promise<void> => {
        setLoading(true);
        onNewValue(actualValue)
            .then(() => {
                message.success(successMessage);
                setActualValue(tempValue);
                setIsPopoverOpen(false);
            })
            // TODO: Add bad request handling from backend
            .finally(() => {
                setLoading(false);
            });
    };

    return (
        <div className={styles.IntervalPreviewWrapper}>
            <OptionalTooltip value={tempValue} tooltipFormatter={isPopoverOpen ? undefined : tooltipFormatter}>
                {actualValue}
            </OptionalTooltip>
            <Popover
                open={isPopoverOpen}
                onOpenChange={(newOpen) => setIsPopoverOpen(newOpen)}
                content={
                    <div className={styles.EditWrapper}>
                        <OptionalTooltip value={tempValue} tooltipFormatter={tooltipFormatter}>
                            <Input
                                value={tempValue}
                                onChange={(e) => setTempValue(e.target.value)}
                                disabled={loading}
                            />
                        </OptionalTooltip>

                        <Button
                            icon={<CloseOutlined />}
                            type="primary"
                            onClick={() => {
                                setTempValue(initialValue);
                                setIsPopoverOpen(false);
                            }}
                            className={styles.EditActionButtons}
                        />

                        <Button
                            icon={<CheckOutlined />}
                            type="primary"
                            onClick={handleUpdate}
                            loading={loading}
                            className={styles.EditActionButtons}
                        />
                    </div>
                }
                trigger="click"
            >
                <Button icon={<EditOutlined />} type="primary" />
            </Popover>
        </div>
    );
};
