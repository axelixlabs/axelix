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
import { type ERoles, EUserOrigin, type IUsersFilters } from "@/models";
import { roleOptions } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Text to display after the search field
     */
    addonAfter: string;

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
}

export const UsersSearchBar = ({ addonAfter, setSearch, filters, setFilters }: IProps) => {
    const { t } = useTranslation();

    const userOriginOptions = Object.values(EUserOrigin).map((origin) => ({
        value: origin,
    }));

    const rolesHandleChange = (value: ERoles[]): void => {
        setFilters((prev) => ({
            ...prev,
            roles: value,
        }));
    };

    const userOriginsHandleChange = (value: EUserOrigin[]): void => {
        setFilters((prev) => ({
            ...prev,
            userOrigins: value,
        }));
    };

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.SearchBarWrapper}>
                    <PageSearch addonAfter={addonAfter} setSearch={setSearch} removeBottomGutter />
                    <div className={styles.FiltersWrapper}>
                        <Select
                            // TODO: Determine whether we need mode="multiple"
                            mode="multiple"
                            showSearch={false}
                            placeholder={t("status")}
                            maxTagCount={1}
                        />
                        <Select
                            mode="multiple"
                            showSearch={false}
                            placeholder={t("Users.role")}
                            maxTagCount={1}
                            value={filters.roles}
                            onChange={rolesHandleChange}
                            options={roleOptions}
                        />
                        <Select
                            mode="multiple"
                            showSearch={false}
                            placeholder={t("Users.origin")}
                            maxTagCount={1}
                            value={filters.userOrigins}
                            onChange={userOriginsHandleChange}
                            options={userOriginOptions}
                        />
                    </div>
                </div>
                <div className={styles.ShownItemsInfo}>1-10 of 412</div>
            </div>
        </>
    );
};
