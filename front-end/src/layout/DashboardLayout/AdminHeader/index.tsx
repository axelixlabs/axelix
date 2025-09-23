import { Avatar, Dropdown } from "antd";
import { Link } from "react-router-dom";
import { Header } from "antd/es/layout/layout";
import { useTranslation } from "react-i18next";
import { UserOutlined } from "@ant-design/icons";

import { LanguageSwitcher } from "../../../components/LanguageSwitcher/LanguageSwitcher";
import Logo from "../../../assets/icons/logo.png";
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
        <nav data-test="header-links">
          <Link to="#" className={styles.Link}>
            {t("dashboard")}
          </Link>
          <Link to="#" className={styles.Link}>
            {t("wallboard")}
          </Link>
        </nav>
        <Dropdown menu={{ items }}>
          <Avatar size={32} icon={<UserOutlined />} className={styles.Avatar} />
        </Dropdown>
        <LanguageSwitcher />
      </div>
    </Header>
  );
};
