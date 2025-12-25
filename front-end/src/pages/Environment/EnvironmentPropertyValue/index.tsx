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

import { Button, Input, Tooltip } from "antd";
import { type MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { useAppDispatch } from "hooks";
import type { IEnvProperty } from "models";
import { updatePropertyThunk } from "store/thunks";

import styles from "./styles.module.css";

import CrownIcon from "assets/icons/crown.svg";

interface IProps {
    /**
     * Single property
     */
    property: IEnvProperty;
}

export const EnvironmentPropertyValue = ({ property }: IProps) => {
    const { name, value, isPrimary } = property;

    const { t } = useTranslation();
    const dispatch = useAppDispatch();
    const { instanceId } = useParams();

    const [editProperty, setEditProperty] = useState<boolean>(false);
    const [newPropertyValue, setNewPropertyValue] = useState<string>(value);

    const updatePropertyClickHandler = (e: MouseEvent<HTMLButtonElement>): void => {
        e.stopPropagation();
        dispatch(
            updatePropertyThunk({
                instanceId: instanceId!,
                propertyName: name,
                newValue: newPropertyValue,
            }),
        );
    };

    // TODO: Create separate components in future for this component
    return (
        <div className={styles.MainWrapper}>
            {editProperty ? (
                <div className={styles.EditPropertyWrapper}>
                    <Input
                        value={newPropertyValue || "null"}
                        onChange={(e) => setNewPropertyValue(e.target.value)}
                        className={styles.EditPropertyField}
                    />

                    <Button
                        icon={<CloseOutlined />}
                        type="primary"
                        onClick={(e) => {
                            e.stopPropagation();
                            setEditProperty(false);
                            setNewPropertyValue(value);
                        }}
                        className={styles.CloseButton}
                    />

                    <Button
                        icon={<CheckOutlined />}
                        type="primary"
                        onClick={updatePropertyClickHandler}
                        className={styles.UpdateButton}
                    />
                </div>
            ) : (
                <div className={styles.PropertyValueWrapper}>
                    {value || "null"}
                    <Button
                        icon={<EditOutlined />}
                        type="primary"
                        onClick={(e) => {
                            e.stopPropagation();
                            setEditProperty(true);
                        }}
                        className={styles.EditButton}
                    />
                </div>
            )}
            <Tooltip title={t("Environments.primaryProperty")}>
                <img src={CrownIcon} alt="Crown icon" className={!isPrimary ? styles.IconPlaceholder : ""} />
            </Tooltip>
        </div>
    );
};
