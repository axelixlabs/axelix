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
import { CloseOutlined } from "@ant-design/icons";

import { Button, Dropdown, type MenuProps, Popover, Tag } from "antd";
import type { AxiosError } from "axios";
import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";

import { extractErrorCode, getUserFullName, getUserInitials } from "@/helpers";
import { type ICurrentUserResponseBody, type IErrorResponse, StatelessRequest } from "@/models";
import { logout } from "@/services";

import styles from "./styles.module.css";

const AccountProfile = ({ account, onClose }: { account: ICurrentUserResponseBody; onClose: () => void }) => {
    const { t } = useTranslation();
    const closeRef = useRef<HTMLButtonElement>(null);
    const fullName = getUserFullName(account);

    useEffect(() => {
        closeRef.current?.focus();
    }, []);

    return (
        <section
            role="dialog"
            aria-label={t("Users.Profile.profile")}
            className={styles.Profile}
            onKeyDown={(event) => {
                if (event.key === "Escape") {
                    event.stopPropagation();
                    onClose();
                }
            }}
        >
            <Button
                ref={closeRef}
                type="text"
                size="small"
                icon={<CloseOutlined />}
                className={styles.Close}
                aria-label={t("Authentication.closeProfile")}
                onClick={onClose}
            />
            <div className={styles.Label}>{t("Authentication.signedInAs")}</div>
            <strong className={styles.Name}>{fullName ?? account.username}</strong>
            {fullName && <div className={styles.Username}>{account.username}</div>}
            <div className={styles.Roles}>
                <div className={styles.Label}>{t("Users.roles")}</div>
                {account.roles.length
                    ? account.roles.map((role) => (
                          <Tag color="green" key={role}>
                              {role}
                          </Tag>
                      ))
                    : "—"}
            </div>
        </section>
    );
};

export const ProfileMenu = ({ account }: { account: ICurrentUserResponseBody | null }) => {
    const { t } = useTranslation();
    const [panel, setPanel] = useState<"menu" | "profile" | null>(null);
    const triggerRef = useRef<HTMLButtonElement>(null);
    const closeProfile = () => {
        setPanel(null);
        requestAnimationFrame(() => triggerRef.current?.focus());
    };

    const [logoutData, setLogoutData] = useState(StatelessRequest.inactive());

    const logoutClickHandler = () => {
        setPanel(null);
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
        {
            key: "profile",
            label: t("Authentication.viewProfile"),
            disabled: !account,
            onClick: () => setPanel("profile"),
        },
        { key: "logout", label: t("Authentication.logout"), disabled: logoutData.loading, onClick: logoutClickHandler },
    ];

    const avatar = (
        <button
            ref={triggerRef}
            type="button"
            className={styles.Avatar}
            aria-haspopup={panel === "profile" ? "dialog" : "menu"}
            aria-expanded={panel !== null}
            aria-label={account?.username ?? t("Users.Profile.account")}
            title={account?.username}
        >
            {account ? getUserInitials(account) : "…"}
        </button>
    );

    if (panel === "profile" && account) {
        return (
            <Popover
                open
                placement="bottomRight"
                trigger="click"
                arrow={false}
                styles={{ container: { padding: 0 } }}
                content={<AccountProfile account={account} onClose={closeProfile} />}
                onOpenChange={(open) => {
                    if (!open) setPanel(null);
                }}
            >
                {avatar}
            </Popover>
        );
    }

    return (
        <Dropdown
            menu={{ items }}
            placement="bottomRight"
            trigger={["hover", "click"]}
            open={panel === "menu"}
            onOpenChange={(open, info) => {
                if (info.source === "trigger") {
                    setPanel(open ? "menu" : null);
                }
            }}
        >
            {avatar}
        </Dropdown>
    );
};
