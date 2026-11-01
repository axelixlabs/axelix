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
import { Select } from "antd";
import type { Dispatch, SetStateAction } from "react";
import { useTranslation } from "react-i18next";

import { PageSearch } from "@/components";
import { useAppSelector } from "@/hooks";
import { ERoles, EUserDisplayStatus, EUserOrigin, type IUser, type IUsersFilters } from "@/models";
import { LOCAL_AUTH_OPTION_TYPE_NAME, roleOptions } from "@/utils";

import { CreateUser } from "./CreateUser";
import { UsersMetrics } from "./UsersMetrics";
import styles from "./styles.module.css";

interface IProps {
    /**
     * All users, regardless of the applied search and filters
     */
    users: IUser[];

    /**
     * Range of the currently shown users, e.g. "1–10 of 42"
     */
    rangeLabel: string;

    /**
     * Setter to update the search
     */
    setSearch: Dispatch<SetStateAction<string>>;

    /**
     * Filters for data filtering
     */
    filters: IUsersFilters;

    /**
     * Filters setter
     */
    setFilters: Dispatch<SetStateAction<IUsersFilters>>;

    /**
     * Function to fetch all users
     */
    fetchUsers: () => void;
}

export const UsersFirstSection = ({ users, rangeLabel, setSearch, filters, setFilters, fetchUsers }: IProps) => {
    const { t } = useTranslation();
    const settings = useAppSelector((state) => state.settings);

    const isLocalAuthEnabled = settings.authenticationOptions.some(
        (value) => value.type === LOCAL_AUTH_OPTION_TYPE_NAME,
    );

    const statusOptions = Object.values(EUserDisplayStatus).map((status) => ({
        value: status,
        label: t(`Users.Statuses.${status}`),
    }));

    const userOriginOptions = [
        { value: EUserOrigin.OIDC, label: t("Users.originOidc") },
        { value: EUserOrigin.LOCAL, label: t("Users.originLocal") },
    ];

    const updateFilter = <K extends keyof IUsersFilters>(key: K, value: IUsersFilters[K]): void => {
        setFilters((prev) => ({
            ...prev,
            [key]: value,
        }));
    };

    return (
        <>
            <div className={styles.Header}>
                <div>
                    <div className="TextLarge">{t("Users.title")}</div>
                    <p className={`TextSmall ${styles.Subtitle}`}>{t("Users.subtitle")}</p>
                </div>
                {isLocalAuthEnabled && <CreateUser fetchUsers={fetchUsers} />}
            </div>

            <UsersMetrics users={users} />

            <div className={styles.Toolbar}>
                <PageSearch setSearch={setSearch} placeholder={t("Users.searchPlaceholder")} removeBottomGutter />
                <div className={styles.FiltersWrapper}>
                    <Select
                        mode="multiple"
                        showSearch={false}
                        placeholder={t("Users.status")}
                        maxTagCount={1}
                        value={filters.statuses}
                        onChange={(value: EUserDisplayStatus[]) => updateFilter("statuses", value)}
                        options={statusOptions}
                        className={styles.Filter}
                    />
                    <Select
                        mode="multiple"
                        showSearch={false}
                        placeholder={t("Users.roles")}
                        maxTagCount={1}
                        value={filters.roles}
                        onChange={(value: ERoles[]) => updateFilter("roles", value)}
                        options={roleOptions}
                        className={styles.Filter}
                    />
                    <Select
                        mode="multiple"
                        showSearch={false}
                        placeholder={t("Users.origin")}
                        maxTagCount={1}
                        value={filters.userOrigins}
                        onChange={(value: EUserOrigin[]) => updateFilter("userOrigins", value)}
                        options={userOriginOptions}
                        className={styles.Filter}
                    />
                </div>
                <span className={`TextSmall ${styles.Range}`}>{rangeLabel}</span>
            </div>
        </>
    );
};
