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
import type { AxiosError } from "axios";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { extractErrorCode } from "helpers";
import { type IErrorResponse, StatelessRequest } from "models";
import { logout } from "services";
import { IS_AUTH } from "utils";

import styles from "./styles.module.css";

export const ProfileMenu = () => {
    const { t } = useTranslation();

    const [logoutData, setLogoutData] = useState(StatelessRequest.inactive());

    const logoutClickHandler = () => {
        setLogoutData(StatelessRequest.loading());

        logout()
            .then(() => {
                setLogoutData(StatelessRequest.success());
                localStorage.removeItem(IS_AUTH);
                window.location.href = "/login";
            })
            // TODO: We need to decide whether we need the code below, since our errors are already being handled through Axios interceptors, and basically the code below isn’t used at all.
            .catch((error: AxiosError<IErrorResponse>) => {
                setLogoutData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    const items: MenuProps["items"] = [
        {
            key: "logout",
            disabled: logoutData.loading,
            label: <div onClick={logoutClickHandler}>{t("Authentication.logout")}</div>,
        },
    ];

    return (
        <Dropdown menu={{ items }}>
            <Avatar size={32} icon={<UserOutlined />} className={styles.Avatar} />
        </Dropdown>
    );
};
