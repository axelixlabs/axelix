import { Form, Input, Button } from "antd";
import { useTranslation } from "react-i18next";

import { useAppDispatch, useAppSelector } from "hooks";
import { loginThunk } from "store/slices/login";
import type { ILoginSubmitValue } from "models";

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
      <h1 className={styles.LoginTitle}>{t("login")}</h1>
      <Form layout="vertical" onFinish={onFinish} autoComplete="off">
        <Form.Item
          key={t("username")}
          label={t("username")}
          name="username"
          required={false}
          rules={[{ required: true, message: t("enterUsername") }]}
        >
          <Input className={styles.LoginInput} />
        </Form.Item>
        <Form.Item
          key={t("password")}
          label={t("password")}
          name="password"
          required={false}
          rules={[{ required: true, message: t("enterPassword") }]}
        >
          <Input.Password className={styles.LoginInput} />
        </Form.Item>
        <Button
          type="primary"
          htmlType="submit"
          loading={loading}
          className={styles.SubmitButton}
        >
          {t("loginButtonText")}
        </Button>
        <p className={styles.Error}>{error}</p>
      </Form>
    </div>
  );
};

export default Login;