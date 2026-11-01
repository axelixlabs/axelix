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
import { ERoles, EUserDisplayStatus, EUserOrigin, type IUser } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * All users, regardless of the applied search and filters
     */
    users: IUser[];
}

export const UsersMetrics = ({ users }: IProps) => {
    const { t } = useTranslation();

    const localCount = users.filter((user) => user.userOrigin === EUserOrigin.LOCAL).length;
    const adminsCount = users.filter((user) => user.roles.includes(ERoles.ADMIN)).length;
    const countByStatus = (status: EUserDisplayStatus): number => {
        return users.filter((user) => getUserDisplayStatus(user) === status).length;
    };

    const metrics = [
        {
            label: t("Users.Metrics.totalUsers"),
            value: users.length,
            hint: t("Users.originSplit", { oidc: users.length - localCount, local: localCount }),
        },
        {
            label: t("Users.Metrics.admins"),
            value: adminsCount,
            hint: t("Users.Metrics.adminsHint"),
        },
        {
            label: t("Users.Metrics.suspended"),
            value: countByStatus(EUserDisplayStatus.SUSPENDED),
            hint: t("Users.Metrics.suspendedHint"),
            valueClassName: styles.Suspended,
        },
        {
            label: t("Users.Metrics.dormant"),
            value: countByStatus(EUserDisplayStatus.DORMANT),
            hint: t("Users.Metrics.dormantHint"),
            valueClassName: styles.Dormant,
        },
    ];

    return (
        <>
            <div className={styles.MainWrapper}>
                {metrics.map(({ label, value, hint, valueClassName }) => (
                    <div key={label} className={styles.Metric}>
                        <div className={`TextCaps ${styles.Label}`}>{label}</div>
                        <div className={`TextLarge ${valueClassName ?? ""}`}>{value}</div>
                        <div className={`TextUltraSmall ${styles.Hint}`}>{hint}</div>
                    </div>
                ))}
            </div>
        </>
    );
};
