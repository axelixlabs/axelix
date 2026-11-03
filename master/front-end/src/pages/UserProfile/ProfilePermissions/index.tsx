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
import { useTranslation } from "react-i18next";

import type { IUser } from "@/models";

import { ProfileCard } from "../ProfileCard";
import { PERMISSION_GROUPS, effectiveAuthorities } from "../permissions";

import { PermissionRow } from "./PermissionRow";
import styles from "./styles.module.css";

interface IProps {
    /**
     * User whose effective permissions are shown
     */
    user: IUser;
}

export const ProfilePermissions = ({ user }: IProps) => {
    const { t } = useTranslation();
    const granted = effectiveAuthorities(user.roles);

    return (
        <ProfileCard title={t("Users.Profile.permissions")} hint={t("Users.Profile.permissionsHint")}>
            {PERMISSION_GROUPS.map((group) => (
                <div key={group.id} className={styles.GroupBlock}>
                    <div className={`TextCaps ${styles.Group}`}>{t(`Users.Profile.Groups.${group.id}`)}</div>
                    {group.authorities.map((authority) => (
                        <PermissionRow key={authority} authority={authority} source={granted.get(authority)} />
                    ))}
                </div>
            ))}
        </ProfileCard>
    );
};
