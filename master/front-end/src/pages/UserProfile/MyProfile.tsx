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
import { Alert } from "antd";
import { useTranslation } from "react-i18next";
import { useOutletContext } from "react-router";

import { Loader } from "@/components";
import { getUserFullName, getUserInitials } from "@/helpers";
import type { ICurrentUserResponseBody, StatefulRequest } from "@/models";

import { ProfileCard } from "./ProfileCard";
import { ProfileField } from "./ProfileField";
import header from "./ProfileHeader/styles.module.css";
import styles from "./styles.module.css";

const MyProfile = () => {
    const { t } = useTranslation();
    const { loading, response: user } = useOutletContext<StatefulRequest<ICurrentUserResponseBody>>();
    if (loading) return <Loader />;
    if (!user) return <Alert type="error" title={t("unknownError")} />;

    return (
        <>
            <div className={header.Identity}>
                <span className={header.Avatar}>{getUserInitials(user)}</span>
                <div className={header.IdentityText}>
                    <h1 className={header.Name}>{getUserFullName(user) ?? user.username}</h1>
                    <p className={header.Meta}>{t("Users.Profile.profile")}</p>
                </div>
            </div>
            <div className={styles.Columns}>
                <ProfileCard title={t("Users.Profile.account")}>
                    <ProfileField label={t("username")} value={user.username} mono />
                    {user.firstName && <ProfileField label={t("Users.Create.firstName")} value={user.firstName} />}
                    {user.lastName && <ProfileField label={t("Users.Create.lastName")} value={user.lastName} />}
                    {user.email && <ProfileField label={t("Users.Profile.email")} value={user.email} />}
                </ProfileCard>
                <ProfileCard title={t("Header.roles")}>
                    <ProfileField label={t("Users.roles")} value={user.roles.join(", ")} />
                    <ProfileField label={t("Users.Profile.permissions")} value={user.permissions.join(", ")} />
                </ProfileCard>
            </div>
        </>
    );
};

export default MyProfile;
