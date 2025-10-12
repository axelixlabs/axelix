import { useState } from "react";
import { Button, Input } from "antd";
import { useParams } from "react-router-dom";
import { EditOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons';

import { updatePropertyThunk } from "store/thunks";
import { useAppDispatch } from "hooks";

import styles from './styles.module.css'

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

export const TablePropertyValue = ({ propertyName, propertyValue }: IProps) => {
    const dispatch = useAppDispatch();
    const { instanceId } = useParams()

    const [editProperty, setEditProperty] = useState<boolean>(false);
    const [newPropertyValue, setNewPropertyValue] = useState<string>(propertyValue)

    const updatePropertyClickHandler = (): void => {
        if (instanceId) {
            dispatch(updatePropertyThunk({
                instanceId,
                updatePropertyData: {
                    propertyName: propertyName,
                    newValue: newPropertyValue,
                }
            }))
        }
    }

    return (
        <div className={styles.MainWrapper}>
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
                            setEditProperty(false)
                            setNewPropertyValue(propertyValue)
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
                    {propertyValue ?? 'null'}
                    <Button
                        icon={<EditOutlined />}
                        type="primary"
                        onClick={() => setEditProperty(true)}
                        className={styles.EditButton}
                    />
                </div>
            )}
        </div>
    )
};