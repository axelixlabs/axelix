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
import { UserOutlined } from "@ant-design/icons";

import { Avatar, Dropdown, type MenuProps } from "antd";
import { Header } from "antd/es/layout/layout";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";

import { LanguageSwitcher } from "components";
import { useAppDispatch } from "hooks";
import { logout } from "store/slices";

import { NavigationBar } from "./NavigationBar";
import styles from "./styles.module.css";

import LogoIcon from "assets/icons/logo.png";

export const AdminHeader = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const dispatch = useAppDispatch();

    const logoutClickHandler = () => {
        localStorage.removeItem("accessToken");
        dispatch(logout());
        window.location.href = "/login";
    };

    const items: MenuProps["items"] = [
        {
            key: "logout",
            label: <div onClick={logoutClickHandler}>{t("Authentication.logout")}</div>,
        },
    ];

    return (
        <Header className={styles.Header}>
            <img src={LogoIcon} alt="Axile logo" onClick={() => navigate("/wallboard")} className={styles.Logo} />
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
