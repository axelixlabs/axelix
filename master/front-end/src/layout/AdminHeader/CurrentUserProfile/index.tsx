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
import { UserOutlined } from "@ant-design/icons";

import { Dropdown, Popover } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { getUserInitials } from "@/helpers";
import { useAppSelector } from "@/hooks";
import { logout } from "@/services";

import { AccountProfile } from "../AccountProfile";

import styles from "./styles.module.css";

export const CurrentUserProfile = () => {
    const { t } = useTranslation();
    const account = useAppSelector((state) => state.currentUser);
    const [profileOpen, setProfileOpen] = useState(false);
    const [loggingOut, setLoggingOut] = useState(false);

    const logOut = () => {
        setLoggingOut(true);
        logout().then(
            () => window.location.assign("/login"),
            () => setLoggingOut(false),
        );
    };

    const avatar = (
        <button
            type="button"
            className={styles.Avatar}
            aria-haspopup={profileOpen ? "dialog" : "menu"}
            aria-label={account?.username ?? t("Users.Profile.account")}
            title={account?.username}
        >
            {account ? getUserInitials(account.firstName, account.lastName, account.username) : <UserOutlined />}
        </button>
    );

    if (profileOpen && account) {
        return (
            <Popover
                open
                trigger="click"
                placement="bottomRight"
                arrow={false}
                styles={{ container: { padding: 0 } }}
                onOpenChange={setProfileOpen}
                content={<AccountProfile account={account} onClose={() => setProfileOpen(false)} />}
            >
                {avatar}
            </Popover>
        );
    }

    return (
        <Dropdown
            menu={{
                items: [
                    {
                        key: "profile",
                        label: t("Authentication.viewProfile"),
                        disabled: !account,
                        onClick: () => setProfileOpen(true),
                    },
                    { key: "logout", label: t("Authentication.logout"), disabled: loggingOut, onClick: logOut },
                ],
            }}
            placement="bottomLeft"
            trigger={["hover"]}
        >
            {avatar}
        </Dropdown>
    );
};
