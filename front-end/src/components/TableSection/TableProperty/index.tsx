import { useEffect, useState } from "react";
import { Button, Input } from "antd";
import { useParams } from "react-router-dom";
import { EditOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons';

import { updatePropertyThunk } from "store/thunks";
import { useAppDispatch, useAppSelector } from "hooks";

import styles from './styles.module.css'

interface IProps {
    /**
     * Property value
     */
    value: string;
    /**
     * Property key
     */
    propertyKey: string;
    propertySourceName: string;
}

export const TableProperty = ({ propertySourceName, propertyKey, value }: IProps) => {
    const dispatch = useAppDispatch();
    const { instanceId } = useParams()

    const [editProperty, setEditProperty] = useState<boolean>(false);
    const [newValue, setNewValue] = useState<string>(value)

    const { loading, error } = useAppSelector((store) => store.updateProperty)

    useEffect(() => {
        // todo Do some small changes in future
        if(error) {
            setNewValue(value)
        }
    }, [error])

    const updatePropertyClickHandler = (): void => {
        if (instanceId) {
            dispatch(updatePropertyThunk({
                instanceId,
                data: {
                    propertySourceName,
                    propertyName: propertyKey,
                    newValue
                }
            }))
            setEditProperty(false)
        }
    }

    return (
        <>
            {editProperty ? (
                <>
                    <Input
                        value={newValue}
                        onChange={(e) => setNewValue(e.target.value)}
                        className={styles.EditPropertyField}
                    />

                    <div className={styles.ActionButtonsWrapper}>
                        <Button
                            icon={<CloseOutlined />}
                            type="primary"
                            onClick={() => {
                                setEditProperty(false)
                                setNewValue(value)
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
                    {value ?? 'null'}
                    <Button
                        icon={<EditOutlined />}
                        type="primary"
                        onClick={() => setEditProperty(true)}
                        disabled={loading}
                        className={styles.EditButton}
                    />
                </>
            )}
        </>
    )
};