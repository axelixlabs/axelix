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
import { App, Button, Checkbox, Col, Form, Input, Row, Select } from "antd";
import type { AxiosError } from "axios";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { UniversalModal } from "@/components";
import { extractErrorCode } from "@/helpers";
import {
    type ICreateUserFormFields,
    type ICreateUserRequestData,
    type IErrorResponse,
    StatelessRequest,
} from "@/models";
import { createUser } from "@/services";
import { getRoleCheckboxOptions } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Function to fetch all users
     */
    fetchUsers: () => void;
}

export const CreateUser = ({ fetchUsers }: IProps) => {
    const { t } = useTranslation();
    const { message } = App.useApp();

    const [modalOpen, setModalOpen] = useState<boolean>(false);
    const [requestData, setRequestData] = useState(StatelessRequest.inactive());
    const [form] = Form.useForm<ICreateUserFormFields>();

    const handleSubmit = async (): Promise<void> => {
        let data: ICreateUserFormFields;

        try {
            data = await form.validateFields();
        } catch {
            return;
        }

        const convertedData: ICreateUserRequestData = {
            username: data.username,
            email: data.email || null,
            password: data.password,
            role: data.role,
        };

        setRequestData(StatelessRequest.loading());

        createUser(convertedData)
            .then(() => {
                setRequestData(StatelessRequest.success());
                message.success(t("Users.userCreated"));
                setModalOpen(false);
                form.resetFields();
                fetchUsers();
            })
            .catch((error: AxiosError<IErrorResponse>) => {
                setRequestData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    const onClose = (): void => {
        setModalOpen(false);
        form.resetFields();
    };

    const roleCheckboxOptions = getRoleCheckboxOptions(t);

    return (
        <>
            <Button type="primary" onClick={() => setModalOpen(true)}>
                {t("Users.createUser")}
            </Button>

            <UniversalModal
                title={t("Users.createUser")}
                subtitle="Northwind Industrial · seat 413 of 500"
                open={modalOpen}
                onOk={handleSubmit}
                onClose={onClose}
                okText={t("Users.createUser")}
                loading={requestData.loading}
            >
                <Form
                    form={form}
                    layout="vertical"
                    requiredMark={false}
                >
                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item
                                name="firstname"
                                label="First name"
                                rules={[
                                    {
                                        required: true,
                                        message: t("Users.ValidationErrors.firstname"),
                                    },
                                ]}
                            >
                                <Input />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                name="lastname"
                                label="Last name"
                                rules={[
                                    {
                                        required: true,
                                        message: t("Users.ValidationErrors.lastname"),
                                    },
                                ]}
                            >
                                <Input />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Form.Item
                        name="email"
                        label="Work email"
                        extra={
                            <span className={`TextUltraSmall ${styles.EmailHint}`}>
                                Username defaults to dana.rowe - change
                            </span>
                        }
                        rules={[
                            {
                                required: true,
                                type: "email",
                                message: t("Users.ValidationErrors.emailFormat"),
                            },
                        ]}
                    >
                        <Input />
                    </Form.Item>

                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item
                                name="department"
                                label="Department"
                                rules={[
                                    {
                                        required: true,
                                        message: t("Users.ValidationErrors.department"),
                                    },
                                ]}
                            >
                                <Select />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                name="jobTitle"
                                label={
                                    <>
                                        {t("Users.CreateUser.jobTitle")}
                                        <span className={`TextSmall ${styles.OptionalLabel}`}>optional</span>
                                    </>
                                }
                            >
                                <Input placeholder="e.g. Reliability Engineer" />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Form.Item
                        name="roles"
                        label={t("Users.CreateUser.roles")}
                        rules={[
                            {
                                required: true,
                                message: t("Users.ValidationErrors.roles"),
                            },
                        ]}
                    >
                        <Checkbox.Group className={styles.RoleGroup}>
                            {roleCheckboxOptions.map(({ value, label, description }) => (
                                <label key={value} className={styles.RoleOption}>
                                    <Checkbox value={value} />
                                    <div className="TextSmall">
                                        <div className={styles.RoleLabel}>{label}</div>
                                        <div className={styles.RoleDescription}>{description}</div>
                                    </div>
                                </label>
                            ))}
                        </Checkbox.Group>
                    </Form.Item>

                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item
                                name="password"
                                label={t("password")}
                                rules={[
                                    {
                                        required: true,
                                        message: t("Users.ValidationErrors.password"),
                                    },
                                ]}
                            >
                                <Input.Password />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                name="confirmPassword"
                                label={t("Users.CreateUser.confirmPassword")}
                                dependencies={["password"]}
                                rules={[
                                    {
                                        required: true,
                                        message: t("Users.ValidationErrors.confirmPassword"),
                                    },
                                    ({ getFieldValue }) => ({
                                        validator(_, value) {
                                            if (!value || getFieldValue("password") === value) {
                                                return Promise.resolve();
                                            }
                                            return Promise.reject(
                                                new Error(t("Users.ValidationErrors.passwordMismatch")),
                                            );
                                        },
                                    }),
                                ]}
                            >
                                <Input.Password />
                            </Form.Item>
                        </Col>
                    </Row>
                </Form>
            </UniversalModal>
        </>
    );
};
