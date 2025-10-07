import { useState } from "react";
import { Button, Input } from "antd";
import { EditOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons';

import { updateEnvConfigPropertyThunk } from "store/slices";
import { useAppDispatch } from "hooks";

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
    /**
     * If editableProperty is true, the property can be edited.
     */
    editableProperty?: boolean;
}

export const TableProperty = ({ editableProperty, propertyKey, value }: IProps) => {
    const dispatch = useAppDispatch();

    const [editProperty, setEditProperty] = useState<boolean>(false);
    const [editValue, setEditValue] = useState<string>(value)

    return (
        <>
            {editProperty ? (
                <>
                    <Input
                        value={editValue}
                        onChange={(e) => setEditValue(e.target.value)}
                        className={styles.EditPropertyField}
                    />

                    <div className={styles.ActionButtonsWrapper}>
                        <Button
                            icon={<CloseOutlined />}
                            type="primary"
                            onClick={() => {
                                setEditProperty(false)
                                setEditValue(value)
                            }}
                        />

                        <Button
                            icon={<CheckOutlined />}
                            type="primary"
                            onClick={() => {
                                dispatch(updateEnvConfigPropertyThunk({
                                    instanceId: "2be78791-5045-4b9a-a02a-cc5a4cdd0094",
                                    data: {
                                        propertyName: propertyKey,
                                        newValue: editValue
                                    }
                                }))
                            }}
                        />
                    </div>
                </>
            ) : (
                <>
                    {value ?? 'null'}
                    {editableProperty && (
                        <Button
                            icon={<EditOutlined />}
                            type="primary"
                            onClick={() => setEditProperty(true)}
                            className={styles.EditButton}
                        />
                    )}
                </>
            )}
        </>
    )
};