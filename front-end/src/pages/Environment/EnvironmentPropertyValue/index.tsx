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
import { Tooltip } from "antd";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EditableValue } from "components";
import { useAppDispatch } from "hooks";
import type { IEnvProperty } from "models";
import { updatePropertyThunk } from "store/thunks";

import styles from "./styles.module.css";

import { CrownIcon } from "assets";

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
                <CrownIcon className={`${styles.PrimaryIcon} ${!isPrimary ? styles.IconPlaceholder : ""}`} />
            </Tooltip>
        </div>
    );
};
