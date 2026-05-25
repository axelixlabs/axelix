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
import { CheckOutlined, CloseOutlined, EditOutlined } from "@ant-design/icons";

import { App, Button, Form, Input } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { emptyStringToNull, extractErrorCode } from "helpers";
import {
    type IEditUserRequestData,
    type IEditableUser,
    StatelessRequest,
    type UserProfileEditableValueField,
} from "models";
import { editUser } from "services";
import { userProfileInputTypeMap } from "utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The user data
     */
    user: IEditableUser;

    /**
     * Field that determines which user field is being changed
     */
    field: UserProfileEditableValueField;

    /**
     * Callback for re-loading the user.
     */
    reLoadUser: () => void;
}

export const EditableValue = ({ user, field, reLoadUser }: IProps) => {
    const { message } = App.useApp();
    const { t } = useTranslation();

    const isPasswordField = field === "password";
    const initialValue = user[field];
    const { id, email, username, roles } = user;

    const [editingValue, setEditingValue] = useState<boolean>(false);
    const [actualValue, setActualValue] = useState<string>(initialValue);
    const [requestData, setRequestData] = useState(StatelessRequest.inactive());

    const handleConfirm = (): void => {
        setRequestData(StatelessRequest.loading());

        const data: IEditUserRequestData = {
            id: id,
            email: emptyStringToNull(email),
            username: username,
            roles: roles,
            password: isPasswordField ? actualValue : null,
        };

        if (!isPasswordField) {
            data[field] = actualValue;
        }

        editUser(data)
            .then(() => {
                setRequestData(StatelessRequest.success());
                message.success(t("Users.userEdited"));
                setEditingValue(false);
                reLoadUser();
            })
            .catch((error) => {
                setRequestData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    if (!editingValue) {
        return (
            <div className={styles.PreviewWrapper}>
                {isPasswordField ? "••••••••••" : actualValue}
                <Button
                    icon={<EditOutlined />}
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
                [field]: actualValue,
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
                icon={<CloseOutlined />}
                type="primary"
                onClick={() => {
                    setEditingValue(false);
                    setActualValue(initialValue);
                }}
                className={styles.ActionButton}
            />

            <Button
                icon={<CheckOutlined />}
                type="primary"
                htmlType="submit"
                loading={requestData.loading}
                className={styles.ActionButton}
            />
        </Form>
    );
};
