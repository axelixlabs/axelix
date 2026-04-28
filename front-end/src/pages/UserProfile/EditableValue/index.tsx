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
import {CheckOutlined, CloseOutlined, EditOutlined} from "@ant-design/icons";

import {App, Button, Input, Form} from "antd";
import {type Dispatch, type SetStateAction, useState} from "react";

import styles from "./styles.module.css";
import type {IUser, UserProfileEditableValueField} from "models";
import {editUser} from "services";
import {useTranslation} from "react-i18next";
import {userProfileInputTypeMap} from "utils";

interface IProps {
    /**
     * The user data
     */
    user: IUser;

    /**
     * Field that determines which user field is being changed
     */
    field: UserProfileEditableValueField,

    /**
     * The setter of user data
     */
    setUser: Dispatch<SetStateAction<IUser>>
}

export const EditableValue = ({user, field, setUser}: IProps) => {
    const {message} = App.useApp();
    const {t} = useTranslation();

    const isPasswordField = field === "password";
    const initialValue = !isPasswordField ? user[field] : "";
    const {id, email, username, roles, provider, lastLoginAt} = user

    const [editingValue, setEditingValue] = useState<boolean>(false);
    const [actualValue, setActualValue] = useState<string>(initialValue);

    const handleConfirm = (): void => {
        editUser({
            id: id,
            email: email,
            username: username,
            roles: roles,
            password: null,
            [field]: actualValue
        })
            .then(() => {
                message.success(t("Users.userEdited"))
                if (!isPasswordField) {
                    setUser({
                        id: id,
                        username: username,
                        provider: provider,
                        lastLoginAt: lastLoginAt,
                        email: email,
                        roles: roles,
                        [field]: actualValue
                    });
                }
            })
            .catch(() => {
                setActualValue(initialValue)
            })
            .finally(() => {
                setEditingValue(false);
            })
    };

    if (!editingValue) {
        return (
            <div className={styles.PreviewWrapper}>
                {actualValue || "••••••••••"}
                <Button
                    icon={<EditOutlined/>}
                    type="primary"
                    onClick={() => setEditingValue(true)}
                    className={styles.ActionButton}
                />
            </div>
        );
    }

    return (
        <Form
            initialValues={{
                [field]: actualValue
            }}
            onFinish={handleConfirm}
            className={styles.EditForm}
        >
            {isPasswordField ? (
                <Input.Password
                    value={actualValue}
                    onChange={(e) => setActualValue(e.target.value)}
                    autoFocus
                    className={styles.EditFormField}
                />
            ) : (
                <Input
                    type={userProfileInputTypeMap[field]}
                    value={actualValue}
                    onChange={(e) => setActualValue(e.target.value)}
                    autoFocus
                    className={styles.EditFormField}
                />
            )}

            <Button
                icon={<CloseOutlined/>}
                type="primary"
                onClick={() => {
                    setEditingValue(false);
                    setActualValue(initialValue);
                }}
                className={styles.ActionButton}
            />

            <Button
                icon={<CheckOutlined/>}
                type="primary"
                htmlType="submit"
                className={styles.ActionButton}
            />
        </Form>
    );
};