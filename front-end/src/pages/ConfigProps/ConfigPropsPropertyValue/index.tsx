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
        <div className={styles.MainWrapper}>
            <EditableValue
                className={styles.PropertyValueWrapper}
                editClassName={styles.EditPropertyWrapper}
                initialValue={propertyValue || "null"}
                onNewValue={updatePropertyClickHandler}
            />
        </div>
    );
};
