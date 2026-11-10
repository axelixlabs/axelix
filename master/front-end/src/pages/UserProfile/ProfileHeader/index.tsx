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
import { Button } from "antd";
import { useTranslation } from "react-i18next";
import { Link } from "react-router";

import { getUserDisplayStatus, getUserFullName, getUserInitials } from "@/helpers";
import { EUserDisplayStatus, EUserOrigin, EUserStatus, type IUser } from "@/models";

import styles from "./styles.module.css";

const statusClassNames: Record<EUserDisplayStatus, string> = {
    [EUserDisplayStatus.ACTIVE]: styles.StatusActive,
    [EUserDisplayStatus.DORMANT]: styles.StatusDormant,
    [EUserDisplayStatus.SUSPENDED]: styles.StatusSuspended,
};

interface IProps {
    /**
     * User being shown
     */
    user: IUser;

    /**
     * Name shown in the breadcrumb and the page heading
     */
    displayName: string;

    /**
     * Opens the edit dialog
     */
    onEdit: () => void;

    /**
     * Opens the suspend or reactivate dialog
     */
    onSuspend: () => void;
}

export const ProfileHeader = ({ user, displayName, onEdit, onSuspend }: IProps) => {
    const { t } = useTranslation();
    const status = getUserDisplayStatus(user);
    const canSignIn = user.status !== EUserStatus.SUSPENDED;

    return (
        <>
            <div className={styles.Crumb}>
                <Link to="/administration/users">{t("Users.title")}</Link>
                <span>/</span>
                <b>{displayName}</b>
            </div>

            <div className={styles.Identity}>
                <span className={`${styles.Avatar} ${getUserFullName(user) ? "" : styles.AvatarAnonymous}`}>
                    {getUserInitials(user)}
                </span>
                <div className={styles.IdentityText}>
                    <div className={styles.NameRow}>
                        <h1 className={styles.Name}>{displayName}</h1>
                        <span className={`${styles.Status} ${statusClassNames[status]}`}>
                            <i />
                            {t(`Users.Statuses.${status}`)}
                        </span>
                    </div>
                    <p className={styles.Meta}>
                        <span className={styles.Mono}>{user.username}</span>
                        {user.email && <span> · {user.email}</span>}
                    </p>
                </div>
                <div className={styles.Actions}>
                    {user.userOrigin === EUserOrigin.LOCAL && (
                        <>
                            <Button onClick={onEdit}>{t("Users.Profile.editUser")}</Button>
                            <Button danger={canSignIn} onClick={onSuspend}>
                                {t(canSignIn ? "Users.Profile.suspend" : "Users.Profile.reactivate")}
                            </Button>
                        </>
                    )}
                </div>
            </div>
        </>
    );
};
