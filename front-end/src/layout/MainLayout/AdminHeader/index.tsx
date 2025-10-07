import { Avatar, Dropdown } from "antd";
import { Header } from "antd/es/layout/layout";
import { useTranslation } from "react-i18next";
import { UserOutlined } from "@ant-design/icons";

import { NavigationBar } from "./NavigationBar";
import { LanguageSwitcher } from "components";
import Logo from "assets/icons/logo.png";

import type { MenuProps } from "antd";

import styles from "./styles.module.css";

export const AdminHeader = () => {
  const { t } = useTranslation();

  const items: MenuProps["items"] = [
    {
      key: "logout",
      label: <div>{t("logout")}</div>,
    },
  ];

  return (
    <Header className={styles.Header}>
      <img src={Logo} alt="Axile logo" className={styles.Logo} />
      <div className={styles.LinksAndAvatarWrapper}>
        <NavigationBar />
        <Dropdown menu={{ items }}>
          <Avatar size={32} icon={<UserOutlined />} className={styles.Avatar} />
        </Dropdown>
        <LanguageSwitcher />
      </div>
    </Header>
  );
};
