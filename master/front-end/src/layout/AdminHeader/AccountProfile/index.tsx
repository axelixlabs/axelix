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

import { Button, Tag } from "antd";
import { useEffect, useRef } from "react";
import { useTranslation } from "react-i18next";

import { getUserFullName } from "@/helpers";
import type { ICurrentUserResponseBody } from "@/models";

import styles from "./styles.module.css";

export const AccountProfile = ({ account, onClose }: { account: ICurrentUserResponseBody; onClose: () => void }) => {
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
