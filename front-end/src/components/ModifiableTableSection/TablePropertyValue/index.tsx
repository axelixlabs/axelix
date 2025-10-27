import { CheckOutlined, CloseOutlined, EditOutlined } from "@ant-design/icons";

import { Button, Input } from "antd";
import { useState } from "react";
import { useParams } from "react-router-dom";

import { useAppDispatch } from "hooks";
import { updatePropertyThunk } from "store/thunks";

import styles from "./styles.module.css";

import CrownIcon from "assets/icons/crown.svg";

interface IProps {
    /**
     * Property value
     */
    propertyValue: string;

    /**
     * The name of the property.
     */
    propertyName: string;

    /**
     *  True if propertyValue is primary, false otherwise
     */
    isPrimary?: boolean;
}

export const TablePropertyValue = ({ propertyName, propertyValue, isPrimary }: IProps) => {
    const dispatch = useAppDispatch();
    const { instanceId } = useParams();

    const [editProperty, setEditProperty] = useState<boolean>(false);
    const [newPropertyValue, setNewPropertyValue] = useState<string>(propertyValue);

    const updatePropertyClickHandler = (): void => {
        dispatch(
            updatePropertyThunk({
                instanceId: instanceId!,
                propertyName: propertyName,
                newValue: newPropertyValue,
            }),
        );
    };

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.InnerWrapper}>
                {editProperty ? (
                    <div className={styles.EditPropertyWrapper}>
                        <Input
                            value={newPropertyValue}
                            onChange={(e) => setNewPropertyValue(e.target.value)}
                            className={styles.EditPropertyField}
                        />

                        <Button
                            icon={<CloseOutlined />}
                            type="primary"
                            onClick={() => {
                                setEditProperty(false);
                                setNewPropertyValue(propertyValue);
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
                        {propertyValue ?? "null"}
                        <Button
                            icon={<EditOutlined />}
                            type="primary"
                            onClick={() => setEditProperty(true)}
                            className={styles.EditButton}
                        />
                    </div>
                )}
                {isPrimary && <img src={CrownIcon} alt="Crown icon" className={styles.PrimaryIcon} />}
            </div>
        </div>
    );
};
