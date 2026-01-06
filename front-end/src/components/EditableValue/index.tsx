/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    } else {
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
                    onClick={() => {
                        setEditingValue(false);
                        setActualValue(initialValue);
                    }}
                    className={styles.CloseButton}
                />

                <Button
                    icon={<CheckOutlined />}
                    type="primary"
                    onClick={() => {
                        setEditingValue(false);
                        setActualValue(actualValue);
                        onNewValue(actualValue);
                    }}
                    className={styles.UpdateButton}
                />
            </div>
        );
    }
};
