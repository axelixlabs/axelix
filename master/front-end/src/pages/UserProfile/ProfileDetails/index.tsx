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

import { getUserDisplayStatus } from "@/helpers";
import { EUserOrigin, EUserStatus, type IUser } from "@/models";

import { ProfileCard } from "../ProfileCard";
import { ProfileField } from "../ProfileField";
import { UserProfileActions } from "../UserProfileActions";

import styles from "./styles.module.css";

interface IProps {
    /**
     * User being shown
     */
    user: IUser;
}

const formatSignIn = (value: string, language: string): string => {
    return new Intl.DateTimeFormat(language, {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    }).format(new Date(value));
};

export const ProfileDetails = ({ user }: IProps) => {
    const { t, i18n } = useTranslation();
    const status = getUserDisplayStatus(user);
    const canSignIn = user.status !== EUserStatus.SUSPENDED;
    const isLocal = user.userOrigin === EUserOrigin.LOCAL;
    const lastSignIn = user.lastLoginAt ? formatSignIn(user.lastLoginAt, i18n.language) : t("Users.never");

    return (
        <div>
            <ProfileCard title={t("Users.Profile.profile")}>
                <ProfileField label={t("Users.Create.firstName")} value={user.firstName} />
                <ProfileField label={t("Users.Create.lastName")} value={user.lastName} />
                <ProfileField label={t("username")} value={user.username} mono />
                <ProfileField label={t("Users.Profile.email")} value={user.email} />
            </ProfileCard>

            <ProfileCard title={t("Users.Profile.account")}>
                <ProfileField
                    label={t("Users.origin")}
                    value={
                        <>
                            {t(isLocal ? "Users.originLocal" : "Users.originOidc")}
                            <span className={styles.Muted}>
                                {" "}
                                · {t(isLocal ? "Users.Profile.originLocalDetail" : "Users.Profile.originOidcDetail")}
                            </span>
                        </>
                    }
                />
                <ProfileField
                    label={t("Users.status")}
                    value={
                        <>
                            {t(`Users.Statuses.${status}`)}
                            <span className={styles.Muted}>
                                {" "}
                                · {t(canSignIn ? "Users.Profile.canSignIn" : "Users.Profile.signInBlocked")}
                            </span>
                        </>
                    }
                />
                <ProfileField label={t("Users.Profile.lastSignIn")} value={lastSignIn} />
            </ProfileCard>

            <UserProfileActions userId={user.id} userOrigin={user.userOrigin} />
        </div>
    );
};
