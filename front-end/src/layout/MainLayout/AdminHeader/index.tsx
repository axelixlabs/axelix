import { UserOutlined } from "@ant-design/icons";

import { Avatar, Dropdown, type MenuProps } from "antd";
import { Header } from "antd/es/layout/layout";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";

import { LanguageSwitcher } from "components";

import { NavigationBar } from "./NavigationBar";
import styles from "./styles.module.css";

import LogoIcon from "assets/icons/logo.png";

export const AdminHeader = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();

    const items: MenuProps["items"] = [
        {
            key: "logout",
            label: <div>{t("Authentication.logout")}</div>,
        },
    ];

    return (
        <Header className={styles.Header}>
            <img src={LogoIcon} alt="Axile logo" onClick={() => navigate("/")} className={styles.Logo} />
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
