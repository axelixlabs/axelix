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

import { Dropdown } from "antd";
import { useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router";

import { LogoIcon } from "@/assets";
import { LanguageSwitcher } from "@/components";
import { getUserInitials } from "@/helpers";
import type { ICurrentUserResponseBody } from "@/models";
import { getCurrentUser, logout } from "@/services";

import { AccountProfile } from "./AccountProfile";
import { Help } from "./Help";
import { NavigationBar } from "./NavigationBar";
import styles from "./styles.module.css";

export const AdminHeader = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();
    const [account, setAccount] = useState<ICurrentUserResponseBody | null>(null);
    const accountRequested = useRef(false);
    const avatarRef = useRef<HTMLButtonElement>(null);
    const [panel, setPanel] = useState<"menu" | "profile" | null>(null);
    const [loggingOut, setLoggingOut] = useState(false);

    const loadAccount = () => {
        if (accountRequested.current) return;
        accountRequested.current = true;
        getCurrentUser().then(
            ({ data }) => setAccount(data),
            () => {
                accountRequested.current = false;
            },
        );
    };

    const logOut = () => {
        setPanel(null);
        setLoggingOut(true);
        logout().then(
            () => window.location.assign("/login"),
            () => setLoggingOut(false),
        );
    };

    const closeProfile = () => {
        setPanel(null);
        avatarRef.current?.focus();
    };

    const profile = panel === "profile" && account ? <AccountProfile account={account} onClose={closeProfile} /> : null;

    return (
        <div className={styles.Header}>
            <LogoIcon onClick={() => navigate("/wallboard")} className={styles.Logo} />
            <div className={styles.LinksAndAvatarWrapper}>
                <NavigationBar />
                <Help />
                <Dropdown
                    menu={{
                        items: [
                            {
                                key: "profile",
                                label: t("Authentication.viewProfile"),
                                disabled: !account,
                                onClick: () => setPanel("profile"),
                            },
                            { key: "logout", label: t("Authentication.logout"), disabled: loggingOut, onClick: logOut },
                        ],
                    }}
                    placement="bottomRight"
                    trigger={profile ? ["click"] : ["hover", "click"]}
                    open={panel !== null}
                    onOpenChange={(open, { source }) => {
                        if (source === "trigger") setPanel(open ? "menu" : null);
                    }}
                    popupRender={(menu) => profile ?? menu}
                >
                    <button
                        ref={avatarRef}
                        onMouseEnter={loadAccount}
                        onClick={loadAccount}
                        type="button"
                        className={styles.Avatar}
                        aria-haspopup={profile ? "dialog" : "menu"}
                        aria-expanded={panel !== null}
                        aria-label={account?.username ?? t("Users.Profile.account")}
                        title={account?.username}
                    >
                        {account ? getUserInitials(account) : <UserOutlined />}
                    </button>
                </Dropdown>
                <LanguageSwitcher />
            </div>
        </div>
    );
};
