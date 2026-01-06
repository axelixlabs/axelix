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
import { Tooltip } from "antd";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EditableValue } from "components";
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
    const { instanceId } = useParams();
    const dispatch = useAppDispatch();

    const updatePropertyClickHandler = (newValue: string): void => {
        dispatch(
            updatePropertyThunk({
                instanceId: instanceId!,
                propertyName: name,
                newValue: newValue,
            }),
        );
    };

    return (
        <div className={styles.MainWrapper}>
            <EditableValue
                editClassName={styles.EditPropertyWrapper}
                className={styles.PropertyValueWrapper}
                initialValue={value}
                onNewValue={(newValue) => updatePropertyClickHandler(newValue)}
            />
            <Tooltip title={t("Environments.primaryProperty")}>
                <img src={CrownIcon} alt="Crown icon" className={!isPrimary ? styles.IconPlaceholder : ""} />
            </Tooltip>
        </div>
    );
};
