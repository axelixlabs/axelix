import { Button, Form, Input } from "antd";
import { useTranslation } from "react-i18next";

import { useAppDispatch, useAppSelector } from "hooks";
import type { ILoginSubmitRequestData } from "models";
import { loginThunk } from "store/thunks";

import styles from "./styles.module.css";

export const Login = () => {
    const { t } = useTranslation();
    const dispatch = useAppDispatch();

    const loading = useAppSelector((store) => store.login.loading);
    const error = useAppSelector((store) => store.login.error);

    const onFinish = (values: ILoginSubmitRequestData): void => {
        const { username, password } = values;

        const loginData = {
            username,
            password,
        };

        dispatch(loginThunk(loginData));
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
                <Button type="primary" htmlType="submit" loading={loading} className={styles.SubmitButton}>
                    {t("Authentication.loginButtonText")}
                </Button>
                <p className={styles.Error}>{error}</p>
            </Form>
        </div>
    );
};

export default Login;
