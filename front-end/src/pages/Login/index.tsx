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
import { Alert, Button, Form, Input } from "antd";
import type { AxiosError } from "axios";
import { useState } from "react";
import { Trans, useTranslation } from "react-i18next";

import { extractErrorCode } from "helpers";
import { EIgnoredErrors, type IErrorResponse, type ILoginSubmitRequestData, StatelessRequest } from "models";
import { login } from "services";
import { IS_AUTH } from "utils";

import styles from "./styles.module.css";

const Login = () => {
    const { t } = useTranslation();
    const [loginData, setLoginData] = useState(StatelessRequest.inactive());

    const onFinish = (values: ILoginSubmitRequestData): void => {
        const { username, password } = values;

        const loginResponseBody = {
            username,
            password,
        };

        setLoginData(StatelessRequest.loading());

        login(loginResponseBody)
            .then(() => {
                setLoginData(StatelessRequest.success());
                localStorage.setItem(IS_AUTH, "true");
                window.location.href = "/";
            })
            .catch((error: AxiosError<IErrorResponse>) => {
                setLoginData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    return (
        <>
            <div className={styles.LoginFormWrapper}>
                <h1 className={`TextLarge ${styles.LoginTitle}`}>
                    <Trans
                        t={t}
                        i18nKey={"Authentication.login"}
                        components={{ green: <span className={styles.GreenLetter} /> }}
                    />
                </h1>
                <Form layout="vertical" onFinish={onFinish} autoComplete="off">
                    <Form.Item
                        key="username"
                        label={t("Authentication.username")}
                        name="username"
                        required={false}
                        rules={[{ required: true, message: t("Authentication.enterUsername") }]}
                    >
                        <Input className={styles.LoginInput} />
                    </Form.Item>
                    <Form.Item
                        key="password"
                        label={t("Authentication.password")}
                        name="password"
                        required={false}
                        rules={[{ required: true, message: t("Authentication.enterPassword") }]}
                    >
                        <Input.Password className={styles.LoginInput} />
                    </Form.Item>

                    {loginData.error === EIgnoredErrors.INVALID_CREDENTIALS && (
                        <Alert
                            title={t(`Error.codes.${loginData.error}`)}
                            type="error"
                            showIcon
                            className={styles.ErrorAlert}
                        />
                    )}

                    <Button
                        type="primary"
                        htmlType="submit"
                        loading={loginData.loading}
                        className={styles.SubmitButton}
                    >
                        {t("Authentication.loginButtonText")}
                    </Button>
                </Form>
            </div>
        </>
    );
};

export default Login;
