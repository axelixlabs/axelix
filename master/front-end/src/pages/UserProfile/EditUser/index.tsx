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
import { App, Form, Input, Modal } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { emptyStringToNull, extractErrorCode, showErrorNotification } from "@/helpers";
import { EUserOrigin, type IUser, StatelessRequest } from "@/models";
import { editUser } from "@/services";

import { userUpdateFrom } from "../userUpdate";

import styles from "./styles.module.css";

interface IFields {
    firstName: string;
    lastName: string;
    username: string;
    email: string;
    password: string;
    confirmPassword: string;
}

interface IProps {
    user: IUser;
    open: boolean;
    onClose: () => void;
    onSaved: () => void;
}

const toNullable = (value: string | undefined): string | null => emptyStringToNull(value?.trim() ?? "");

export const EditUser = ({ user, open, onClose, onSaved }: IProps) => {
    const { t } = useTranslation();
    const { message } = App.useApp();
    const [form] = Form.useForm<IFields>();
    const [requestData, setRequestData] = useState(StatelessRequest.inactive());
    const isLocal = user.userOrigin === EUserOrigin.LOCAL;

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

    const optionalLabel = (label: string) => (
        <>
            {label}
            <span className={styles.Optional}>{t("Users.Create.optional")}</span>
        </>
    );

    const close = (): void => {
        form.resetFields();
        onClose();
    };

    const save = async (): Promise<void> => {
        let data: IFields;

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
            <Form
                form={form}
                layout="vertical"
                requiredMark={false}
                className={styles.Form}
                initialValues={{
                    firstName: user.firstName ?? "",
                    lastName: user.lastName ?? "",
                    username: user.username,
                    email: user.email ?? "",
                    password: "",
                    confirmPassword: "",
                }}
            >
                <div className={styles.Row}>
                    <Form.Item name="firstName" label={optionalLabel(t("Users.Create.firstName"))}>
                        <Input autoComplete="off" />
                    </Form.Item>
                    <Form.Item name="lastName" label={optionalLabel(t("Users.Create.lastName"))}>
                        <Input autoComplete="off" />
                    </Form.Item>
                </div>

                <div className={styles.Row}>
                    <Form.Item
                        name="username"
                        label={t("username")}
                        rules={[{ required: true, whitespace: true, message: t("Users.ValidationErrors.username") }]}
                    >
                        <Input autoComplete="off" className={styles.Username} />
                    </Form.Item>
                    <Form.Item
                        name="email"
                        label={optionalLabel(t("Users.Profile.email"))}
                        rules={[{ type: "email", message: t("Users.ValidationErrors.emailFormat") }]}
                    >
                        <Input autoComplete="off" />
                    </Form.Item>
                </div>

                {isLocal && (
                    <div className={styles.Row}>
                        <Form.Item
                            name="password"
                            label={optionalLabel(t("password"))}
                            extra={t("Users.Profile.passwordKeep")}
                        >
                            <Input.Password autoComplete="new-password" />
                        </Form.Item>
                        <Form.Item
                            name="confirmPassword"
                            label={optionalLabel(t("Users.Create.confirmPassword"))}
                            dependencies={["password"]}
                            rules={[
                                ({ getFieldValue }) => ({
                                    validator: (_, value: string | undefined) =>
                                        !value && !getFieldValue("password")
                                            ? Promise.resolve()
                                            : getFieldValue("password") === value
                                              ? Promise.resolve()
                                              : Promise.reject(
                                                    new Error(t("Users.ValidationErrors.passwordsMismatch")),
                                                ),
                                }),
                            ]}
                        >
                            <Input.Password autoComplete="new-password" />
                        </Form.Item>
                    </div>
                )}
            </Form>
        </Modal>
    );
};
