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
import { App, Form, Modal } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { emptyStringToNull, extractErrorCode, showErrorNotification } from "@/helpers";
import { type IUser, StatelessRequest } from "@/models";
import { editUser } from "@/services";

import { userUpdateFrom } from "../userUpdate";

import { EditUserForm, type IEditUserFields } from "./EditUserForm";

interface IProps {
    /**
     * Local user being edited
     */
    user: IUser;

    /**
     * Whether the dialog is open
     */
    open: boolean;

    /**
     * Closes the dialog without saving
     */
    onClose: () => void;

    /**
     * Reloads the profile after the user is saved
     */
    onSaved: () => void;
}

const toNullable = (value: string | undefined): string | null => emptyStringToNull(value?.trim() ?? "");

export const EditUser = ({ user, open, onClose, onSaved }: IProps) => {
    const { t } = useTranslation();
    const { message } = App.useApp();
    const [form] = Form.useForm<IEditUserFields>();
    const [requestData, setRequestData] = useState(StatelessRequest.inactive());

    useEffect(() => {
        if (open) {
            form.setFieldsValue({
                firstName: user.firstName ?? "",
                lastName: user.lastName ?? "",
                username: user.username,
                email: user.email ?? "",
                password: "",
                confirmPassword: "",
            });
        }
    }, [open, user, form]);

    const close = (): void => {
        form.resetFields();
        onClose();
    };

    const save = async (): Promise<void> => {
        let data: IEditUserFields;

        try {
            data = await form.validateFields();
        } catch {
            return;
        }

        setRequestData(StatelessRequest.loading());

        editUser(
            userUpdateFrom(user, {
                username: data.username.trim(),
                firstName: toNullable(data.firstName),
                lastName: toNullable(data.lastName),
                email: toNullable(data.email),
                password: data.password?.trim() ? data.password : null,
            }),
        )
            .then(() => {
                setRequestData(StatelessRequest.success());
                message.success(t("Users.userEdited"));
                onSaved();
                close();
            })
            .catch((error) => {
                setRequestData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
                showErrorNotification(extractErrorCode(error?.response?.data));
            });
    };

    return (
        <Modal
            open={open}
            onCancel={close}
            onOk={save}
            centered
            width={560}
            okText={t("confirm")}
            cancelText={t("cancel")}
            confirmLoading={requestData.loading}
            title={<span className="TextMedium">{t("Users.Profile.editTitle")}</span>}
        >
            <EditUserForm user={user} form={form} />
        </Modal>
    );
};
