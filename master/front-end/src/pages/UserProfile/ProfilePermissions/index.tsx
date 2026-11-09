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

import { EmptyHandler, Loader } from "@/components";
import type { IUserAuthorities, StatefulRequest } from "@/models";

import { ProfileCard } from "../ProfileCard";

import { PermissionRow } from "./PermissionRow";
import styles from "./styles.module.css";

interface IProps {
    /** User-specific authority catalog from the backend. */
    authoritiesResponse: StatefulRequest<IUserAuthorities>;
}

export const ProfilePermissions = ({ authoritiesResponse }: IProps) => {
    const { t } = useTranslation();
    const authorities = authoritiesResponse.response;

    return (
        <ProfileCard title={t("Users.Profile.permissions")} hint={t("Users.Profile.permissionsHint")}>
            {authoritiesResponse.loading ? (
                <Loader />
            ) : authoritiesResponse.error || !authorities ? (
                <EmptyHandler isEmpty />
            ) : (
                <div className={styles.Grid}>
                    {authorities.effectiveAuthorities.map((authority) => (
                        <PermissionRow key={authority.id} authority={authority} granted />
                    ))}
                    {authorities.ungrantedAuthorities.map((authority) => (
                        <PermissionRow key={authority.id} authority={authority} granted={false} />
                    ))}
                </div>
            )}
        </ProfileCard>
    );
};
