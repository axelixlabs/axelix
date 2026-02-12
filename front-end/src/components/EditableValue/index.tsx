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

import { Button, Input } from "antd";
import { useState } from "react";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Custom css class name to apply when value is in the process of editing
     */
    editClassName?: string;

    /**
     * Custom css class name to apply when value is not edited.
     */
    className?: string;

    /**
     * Initial value.
     */
    initialValue: string;

    /**
     * Callback to invoke when the value chane accepted.
     * @param value the new value after change.
     */
    onNewValue: (value: string) => void;
}

export const EditableValue = ({
    initialValue,
    onNewValue,
    className = styles.DefaultPropertyValueWrapper,
    editClassName = styles.DefaultEditPropertyWrapper,
}: IProps) => {
    // TODO:
    //  We need to improve this component:
    //  1. Introduce loading of buttons for this component
    //  2. Handle bad request for various types of input
    const [editingValue, setEditingValue] = useState<boolean>(false);
    const [actualValue, setActualValue] = useState<string>(initialValue);

    if (!editingValue) {
        return (
            <div className={className}>
                {actualValue}

                <Button
                    icon={<EditOutlined />}
                    type="primary"
                    onClick={() => setEditingValue(true)}
                    className={styles.EditButton}
                />
            </div>
        );
    }

    return (
        <div className={editClassName}>
            <Input
                value={actualValue}
                onChange={(e) => setActualValue(e.target.value)}
                className={styles.EditPropertyField}
            />

            <Button
                icon={<CloseOutlined />}
                type="primary"
                onClick={(e) => {
                    e.stopPropagation();
                    setEditingValue(false);
                    setActualValue(initialValue);
                }}
                className={styles.CloseButton}
            />

            <Button
                icon={<CheckOutlined />}
                type="primary"
                onClick={(e) => {
                    e.stopPropagation();
                    setEditingValue(false);
                    setActualValue(actualValue);
                    onNewValue(actualValue);
                }}
                className={styles.UpdateButton}
            />
        </div>
    );
};
