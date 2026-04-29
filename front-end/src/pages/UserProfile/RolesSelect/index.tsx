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

import {App, Button, Form, Select, Tag} from "antd";
import {type Dispatch, type SetStateAction, useState} from "react";
import {useTranslation} from "react-i18next";

import {ERoles, type IEditableUser, type IUser, StatelessRequest} from "models";

import styles from "./styles.module.css";
import {editUser} from "services";
import {emptyStringToNull, extractErrorCode} from "../../../helpers";

interface IProps {
    /**
     * The user data
     */
    user: IEditableUser;

    /**
     * The setter of user data
     */
    setUser: Dispatch<SetStateAction<IUser | undefined>>;
}

export const RolesSelect = ({user, setUser}: IProps) => {
    const {t} = useTranslation();
    const {message} = App.useApp();
    const {
        id,
        email,
        username,
        roles: initialRoles,
        provider,
        lastLoginAt
    } = user

    const [editingValue, setEditingValue] = useState<boolean>(false);
    const [actualRoles, setActualRoles] = useState<ERoles[]>(initialRoles);
    const [requestData, setRequestData] = useState(StatelessRequest.inactive());

    const roleOptions = Object.values(ERoles).map((role) => ({
        value: role
    }))

    const handleConfirm = (): void => {
        setRequestData(StatelessRequest.loading());

        editUser({
            id: id,
            email: emptyStringToNull(email),
            username: username,
            roles: actualRoles,
            password: null,
        })
            .then(() => {
                setRequestData(StatelessRequest.success());
                message.success(t("Users.userEdited"));
                setEditingValue(false);
                setUser({
                    id: id,
                    username: username,
                    provider: provider,
                    lastLoginAt: lastLoginAt,
                    email: emptyStringToNull(email),
                    roles: actualRoles,
                });
            })
            .catch((error) => {
                setRequestData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            })
    };

    if (!editingValue) {
        return (
            <div className={styles.PreviewWrapper}>
                <div>
                    {actualRoles.map((role) => (
                        <Tag variant="outlined" color="blue" key={role} className={styles.Tag}>
                            {role}
                        </Tag>
                    ))}
                </div>

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
        <Form onFinish={handleConfirm} className={styles.EditForm}>
            <Select
                mode="multiple"
                value={actualRoles}
                options={roleOptions}
                onChange={setActualRoles}
                placeholder={t("Users.selectRoles")}
                showSearch={false}
                removeIcon={false}
                className={styles.RolesSelect}
            />

            <Button
                icon={<CloseOutlined/>}
                type="primary"
                onClick={() => {
                    setEditingValue(false);
                    setActualRoles(initialRoles);
                }}
                className={styles.ActionButton}
            />

            <Button
                icon={<CheckOutlined/>}
                type="primary"
                htmlType="submit"
                loading={requestData.loading}
                className={styles.ActionButton}
            />
        </Form>
    );
};
