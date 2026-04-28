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
import {App, Button, Form, Input, Select} from "antd";
import type {AxiosError} from "axios";
import {useState} from "react";
import {useTranslation} from "react-i18next";

import {UniversalModal} from "components";
import {extractErrorCode} from "helpers";
import {ERoles, type ICreateUserRequestData, type IErrorResponse, StatelessRequest} from "models";
import {createUser} from "services";

interface IProps {
    /**
     * Function to fetch all users
     */
    fetchUsers: () => void
}

export const CreateUser = ({fetchUsers}: IProps) => {
    const {t} = useTranslation();
    const {message} = App.useApp();

    const [modalOpen, setModalOpen] = useState<boolean>(false);
    const [requestData, setRequestData] = useState(StatelessRequest.inactive());
    const [form] = Form.useForm<ICreateUserRequestData>();

    const rolesOptions = Object.values(ERoles).map((value) => ({
        label: value,
        value,
    }))

    const handleSubmit = async (): Promise<void> => {
        let values: ICreateUserRequestData;

        try {
            values = await form.validateFields();
        } catch {
            return;
        }

        setRequestData(StatelessRequest.loading());

        createUser(values)
            .then(() => {
                setRequestData(StatelessRequest.success());
                message.success(t("Users.userCreated"));
                fetchUsers()
            })
            .catch((error: AxiosError<IErrorResponse>) => {
                setRequestData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            })
            .finally(() => {
                setModalOpen(false);
                form.resetFields();
            })
    };

    const onClose = (): void => {
        setModalOpen(false);
        form.resetFields();
    };

    return (
        <>
            <Button type="primary" onClick={() => setModalOpen(true)}>
                {t("Users.createUser")}
            </Button>

            <UniversalModal
                title={t("Users.createUser")}
                open={modalOpen}
                onOk={handleSubmit}
                onClose={onClose}
                loading={requestData.loading}
            >
                <Form form={form} layout="vertical" requiredMark={false}>
                    <Form.Item
                        name="username"
                        label={t("username")}
                        rules={[
                            {
                                required: true,
                                message: t("Users.ValidationErrors.username")
                            }
                        ]}
                    >
                        <Input/>
                    </Form.Item>

                    <Form.Item
                        name="email"
                        label="Email"
                        rules={[
                            {
                                type: "email",
                                message: t("Users.ValidationErrors.emailFormat")
                            },
                        ]}
                    >
                        <Input/>
                    </Form.Item>

                    <Form.Item
                        name="password"
                        label={t("password")}
                        rules={[
                            {
                                required: true,
                                message: t("Users.ValidationErrors.password")
                            }
                        ]}
                    >
                        <Input.Password/>
                    </Form.Item>

                    <Form.Item
                        name="role"
                        label={t("Users.role")}
                        rules={[
                            {
                                required: true,
                                message: t("Users.ValidationErrors.roles")
                            }
                        ]}
                    >
                        <Select
                            options={rolesOptions}
                        />
                    </Form.Item>
                </Form>
            </UniversalModal>
        </>
    );
};
