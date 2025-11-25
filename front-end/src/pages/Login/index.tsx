/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { Button, Form, Input } from "antd";
import type { AxiosError } from "axios";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { extractErrorCode } from "helpers";
import { type IErrorResponse, type ILoginSubmitRequestData, StatelessRequest } from "models";
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
            // TODO: We need to decide whether we need the code below, since our errors are already being handled through Axios interceptors, and basically the code below isn’t used at all.
            .catch((error: AxiosError<IErrorResponse>) => {
                setLoginData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    return (
        <div className={styles.LoginFormWrapper}>
            <h1 className={`TextMedium ${styles.LoginTitle}`}>{t("Authentication.login")}</h1>
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
                <Button type="primary" htmlType="submit" loading={loginData.loading} className={styles.SubmitButton}>
                    {t("Authentication.loginButtonText")}
                </Button>
            </Form>
        </div>
    );
};

export default Login;
