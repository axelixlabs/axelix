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
import { useParams } from "react-router-dom";

import { EditableValue } from "components";
import { useAppDispatch } from "hooks";
import { updatePropertyThunk } from "store/thunks";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Property value
     */
    propertyValue: string;

    /**
     * The name of the property.
     */
    propertyName: string;
}

export const ConfigPropsPropertyValue = ({ propertyName, propertyValue }: IProps) => {
    const dispatch = useAppDispatch();
    const { instanceId } = useParams();

    const updatePropertyClickHandler = (newValue: string): void => {
        dispatch(
            updatePropertyThunk({
                instanceId: instanceId!,
                propertyName: propertyName,
                newValue: newValue,
            }),
        );
    };

    return (
        <>
            <div className={styles.MainWrapper}>
                <EditableValue
                    className={styles.PropertyValueWrapper}
                    editClassName={styles.EditPropertyWrapper}
                    initialValue={propertyValue || "null"}
                    onNewValue={updatePropertyClickHandler}
                />
            </div>
        </>
    );
};
