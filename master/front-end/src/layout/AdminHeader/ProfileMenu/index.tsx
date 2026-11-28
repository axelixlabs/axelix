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
import { Dropdown, type MenuProps } from "antd";
import type { AxiosError } from "axios";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router";

import { extractErrorCode, getUserInitials } from "@/helpers";
import { type ICurrentUser, type IErrorResponse, StatelessRequest } from "@/models";
import { logout } from "@/services";

import styles from "./styles.module.css";

export const ProfileMenu = ({ account }: { account: ICurrentUser | null }) => {
    const navigate = useNavigate();
    const { t } = useTranslation();

    const [logoutData, setLogoutData] = useState(StatelessRequest.inactive());

    const logoutClickHandler = () => {
        setLogoutData(StatelessRequest.loading());

        logout()
            .then(() => {
                setLogoutData(StatelessRequest.success());
                window.location.href = "/login";
            })
            // TODO: We need to decide whether we need the code below, since our errors are already being handled through Axios interceptors, and basically the code below isn’t used at all.
            .catch((error: AxiosError<IErrorResponse>) => {
                setLogoutData(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    const items: MenuProps["items"] = [
        { key: "profile", label: t("Authentication.viewProfile"), onClick: () => navigate("/profile") },
        { key: "logout", label: t("Authentication.logout"), disabled: logoutData.loading, onClick: logoutClickHandler },
    ];

    return (
        <Dropdown menu={{ items }} trigger={["click"]}>
            <button
                type="button"
                className={styles.Avatar}
                aria-haspopup="menu"
                aria-label={account?.username ?? t("Users.Profile.account")}
                title={account?.username}
            >
                {account ? getUserInitials(account) : "…"}
            </button>
        </Dropdown>
    );
};
