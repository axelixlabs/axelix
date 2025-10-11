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
     * Property key
     */
    propertyKey: string;
}

export const TablePropertyValue = ({ propertyKey, propertyValue }: IProps) => {
    const dispatch = useAppDispatch();
    const { instanceId } = useParams()

    const [editProperty, setEditProperty] = useState<boolean>(false);
    const [newPropertyValue, setNewPropertyValue] = useState<string>(propertyValue)

    const updatePropertyClickHandler = (): void => {
        if (instanceId) {
            dispatch(updatePropertyThunk({
                instanceId,
                updatePropertyData: {
                    propertyName: propertyKey,
                    newValue: newPropertyValue,
                }
            }))
        }
    }

    return (
        <>
            {editProperty ? (
                <>
                    <Input
                        value={newPropertyValue}
                        onChange={(e) => setNewPropertyValue(e.target.value)}
                        className={styles.EditPropertyField}
                    />

                    <div className={styles.ActionButtonsWrapper}>
                        <Button
                            icon={<CloseOutlined />}
                            type="primary"
                            onClick={() => {
                                setEditProperty(false)
                                setNewPropertyValue(propertyValue)
                            }}
                        />

                        <Button
                            icon={<CheckOutlined />}
                            type="primary"
                            onClick={updatePropertyClickHandler}
                        />
                    </div>
                </>
            ) : (
                <>
                    {propertyValue ?? 'null'}
                    <Button
                        icon={<EditOutlined />}
                        type="primary"
                        onClick={() => setEditProperty(true)}
                        className={styles.EditButton}
                    />
                </>
            )}
        </>
    )
};