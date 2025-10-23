import { Button, Form, Input } from "antd";
import classNames from "classnames";
import { useTranslation } from "react-i18next";

import { useAppDispatch, useAppSelector } from "hooks";
import type { ILoginSubmitValue } from "models";
import { loginThunk } from "store/slices/login";

import styles from "./styles.module.css";

export const Login = () => {
    const { t } = useTranslation();
    const dispatch = useAppDispatch();

    const loading = useAppSelector((store) => store.login.loading);
    const error = useAppSelector((store) => store.login.error);

    const onFinish = (values: ILoginSubmitValue): void => {
        const { username, password } = values;

        const loginData = {
            username,
            password,
        };

        dispatch(loginThunk(loginData));
    };

  return (
    <div className={styles.LoginFormWrapper}>
      <h1 className={classNames('MediumTitle', styles.LoginTitle)}>{t("Authorization.login")}</h1>
      <Form layout="vertical" onFinish={onFinish} autoComplete="off">
        <Form.Item
          key="username"
          label={t("Authorization.username")}
          name="username"
          required={false}
          rules={[{ required: true, message: t("Authorization.enterUsername") }]}
        >
          <Input className={styles.LoginInput} />
        </Form.Item>
        <Form.Item
          key="password"
          label={t("Authorization.password")}
          name="password"
          required={false}
          rules={[{ required: true, message: t("Authorization.enterPassword") }]}
        >
          <Input.Password className={styles.LoginInput} />
        </Form.Item>
        <Button
          type="primary"
          htmlType="submit"
          loading={loading}
          className={styles.SubmitButton}
        >
          {t("Authorization.loginButtonText")}
        </Button>
        <p className={styles.Error}>{error}</p>
      </Form>
    </div>
  );
};

export default Login;
