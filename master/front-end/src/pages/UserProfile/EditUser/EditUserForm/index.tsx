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
import { Form, type FormInstance, Input } from "antd";
import { useTranslation } from "react-i18next";

import type { IUser } from "@/models";

import styles from "./styles.module.css";

export interface IEditUserFields {
    firstName: string;
    lastName: string;
    username: string;
    email: string;
    password: string;
    confirmPassword: string;
}

interface IProps {
    /**
     * User being edited, used as the form's initial values
     */
    user: IUser;

    /**
     * Form instance owned by the dialog
     */
    form: FormInstance<IEditUserFields>;

    /**
     * Whether the password fields are shown. OIDC users sign in elsewhere.
     */
    isLocal: boolean;
}

export const EditUserForm = ({ user, form, isLocal }: IProps) => {
    const { t } = useTranslation();

    const optionalLabel = (label: string) => (
        <>
            {label}
            <span className={styles.Optional}>{t("Users.Create.optional")}</span>
        </>
    );

    return (
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
                                          : Promise.reject(new Error(t("Users.ValidationErrors.passwordsMismatch"))),
                            }),
                        ]}
                    >
                        <Input.Password autoComplete="new-password" />
                    </Form.Item>
                </div>
            )}
        </Form>
    );
};
