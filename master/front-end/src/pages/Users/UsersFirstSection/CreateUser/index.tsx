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
import { App, Button, Checkbox, Form, Input, Modal } from "antd";
import type { AxiosError } from "axios";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { extractErrorCode } from "@/helpers";
import {
    ERoles,
    type ICreateUserFormFields,
    type ICreateUserRequestData,
    type IErrorResponse,
    StatelessRequest,
} from "@/models";
import { createUser } from "@/services";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Total number of users, shown in the modal subtitle
     */
    usersCount: number;

    /**
     * Function to fetch all users
     */
    fetchUsers: () => void;
}

// From the least to the most privileged
const ROLES_ORDER = [ERoles.VIEWER, ERoles.EDITOR, ERoles.ADMIN];

const GENERATED_PASSWORD_LENGTH = 16;
const PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%^&*-_=+";

const generatePassword = (): string => {
    const randomValues = crypto.getRandomValues(new Uint32Array(GENERATED_PASSWORD_LENGTH));
    return Array.from(randomValues, (value) => PASSWORD_ALPHABET[value % PASSWORD_ALPHABET.length]).join("");
};

const toNullable = (value: string | undefined): string | null => value?.trim() || null;

export const CreateUser = ({ usersCount, fetchUsers }: IProps) => {
    const { t } = useTranslation();
    const { message } = App.useApp();

    const [modalOpen, setModalOpen] = useState<boolean>(false);
    const [passwordVisible, setPasswordVisible] = useState<boolean>(false);
    const [requestData, setRequestData] = useState(StatelessRequest.inactive());
    const [form] = Form.useForm<ICreateUserFormFields>();
    const selectedRoles = Form.useWatch("roles", form) ?? [];

    const handleSubmit = async (): Promise<void> => {
        let data: ICreateUserFormFields;

        try {
            data = await form.validateFields();
        } catch {
            return;
        }

        const convertedData: ICreateUserRequestData = {
            username: data.username.trim(),
            firstName: toNullable(data.firstName),
            lastName: toNullable(data.lastName),
            email: toNullable(data.email),
            password: data.password,
            roles: data.roles,
        };

        setRequestData(StatelessRequest.loading());

        createUser(convertedData)
            .then(() => {
                setRequestData(StatelessRequest.success());
                message.success(t("Users.userCreated"));
                onClose();
                fetchUsers();
            })
            .catch((error: AxiosError<IErrorResponse>) => {
                setRequestData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    const onGeneratePassword = (): void => {
        const password = generatePassword();
        form.setFieldsValue({ password, confirmPassword: password });
        form.validateFields(["password", "confirmPassword"]);
        setPasswordVisible(true);
    };

    const onClose = (): void => {
        setModalOpen(false);
        setPasswordVisible(false);
        form.resetFields();
    };

    const optionalLabel = (label: string) => (
        <>
            {label}
            <span className={styles.Optional}>{t("Users.Create.optional")}</span>
        </>
    );

    const passwordVisibility = { visible: passwordVisible, onVisibleChange: setPasswordVisible };

    return (
        <>
            <Button type="primary" onClick={() => setModalOpen(true)}>
                {t("Users.createUser")}
            </Button>

            <Modal
                open={modalOpen}
                onOk={handleSubmit}
                onCancel={onClose}
                centered
                width={640}
                okText={t("Users.Create.title")}
                cancelText={t("cancel")}
                confirmLoading={requestData.loading}
                mask={{ closable: false, blur: true }}
                title={
                    <div className={styles.Title}>
                        <span className="TextMedium">{t("Users.Create.title")}</span>
                        <span className={styles.Subtitle}>{t("Users.Create.usersCount", { count: usersCount })}</span>
                    </div>
                }
            >
                <Form form={form} layout="vertical" requiredMark={false} className={styles.Form}>
                    <div className={styles.Row}>
                        <Form.Item name="firstName" label={optionalLabel(t("Users.Create.firstName"))}>
                            <Input autoComplete="off" />
                        </Form.Item>

                        <Form.Item name="lastName" label={optionalLabel(t("Users.Create.lastName"))}>
                            <Input autoComplete="off" />
                        </Form.Item>
                    </div>

                    <div className={`${styles.Row} ${styles.HintedRow}`}>
                        <Form.Item
                            name="username"
                            label={t("username")}
                            rules={[
                                {
                                    required: true,
                                    whitespace: true,
                                    message: t("Users.ValidationErrors.username"),
                                },
                            ]}
                        >
                            <Input autoComplete="off" className={styles.Username} />
                        </Form.Item>

                        <Form.Item
                            name="email"
                            label={optionalLabel(t("Users.Create.email"))}
                            rules={[
                                {
                                    type: "email",
                                    message: t("Users.ValidationErrors.emailFormat"),
                                },
                            ]}
                        >
                            <Input autoComplete="off" />
                        </Form.Item>
                    </div>
                    <p className={styles.Hint}>{t("Users.Create.usernameHint")}</p>

                    <Form.Item
                        name="roles"
                        label={t("Users.roles")}
                        rules={[
                            {
                                required: true,
                                message: t("Users.ValidationErrors.roles"),
                            },
                        ]}
                    >
                        <Checkbox.Group className={styles.Roles}>
                            {ROLES_ORDER.map((role) => (
                                <Checkbox
                                    key={role}
                                    value={role}
                                    className={`${styles.Role} ${selectedRoles.includes(role) ? styles.RoleChecked : ""}`}
                                >
                                    <span className={styles.RoleText}>
                                        <span className={styles.RoleName}>{role}</span>
                                        <span className={styles.RoleDescription}>
                                            {t(`Users.Create.RoleDescriptions.${role}`)}
                                        </span>
                                    </span>
                                </Checkbox>
                            ))}
                        </Checkbox.Group>
                    </Form.Item>

                    <div className={styles.Row}>
                        <Form.Item
                            name="password"
                            label={t("password")}
                            extra={
                                <Button
                                    type="link"
                                    size="small"
                                    className={styles.Generate}
                                    onClick={onGeneratePassword}
                                >
                                    {t("Users.Create.generate")}
                                </Button>
                            }
                            rules={[
                                {
                                    required: true,
                                    whitespace: true,
                                    message: t("Users.ValidationErrors.password"),
                                },
                            ]}
                        >
                            <Input.Password autoComplete="new-password" visibilityToggle={passwordVisibility} />
                        </Form.Item>

                        <Form.Item
                            name="confirmPassword"
                            label={t("Users.Create.confirmPassword")}
                            dependencies={["password"]}
                            rules={[
                                {
                                    required: true,
                                    message: t("Users.ValidationErrors.password"),
                                },
                                ({ getFieldValue }) => ({
                                    validator: (_, value: string | undefined) =>
                                        !value || getFieldValue("password") === value
                                            ? Promise.resolve()
                                            : Promise.reject(new Error(t("Users.ValidationErrors.passwordsMismatch"))),
                                }),
                            ]}
                        >
                            <Input.Password autoComplete="new-password" visibilityToggle={passwordVisibility} />
                        </Form.Item>
                    </div>
                </Form>
            </Modal>
        </>
    );
};
