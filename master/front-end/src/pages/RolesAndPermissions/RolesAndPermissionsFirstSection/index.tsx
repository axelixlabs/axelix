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

import { PageSearch } from "@/components";

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
}

export const RolesAndPermissionsFirstSection = ({ addonAfter, setSearch }: IProps) => {
    return (
        <>
            <div className={styles.MainWrapper}>
                <PageSearch addonAfter={addonAfter} setSearch={setSearch} removeBottomGutter />
                <div className={styles.FiltersWrapper}>
                    <Select
                        // TODO: Determine whether we need mode="multiple"
                        mode="multiple"
                        showSearch={false}
                        placeholder="Source"
                        maxTagCount={1}
                    />
                    <Select mode="multiple" showSearch={false} placeholder="Grants" maxTagCount={1} />
                </div>
            </div>
        </>
    );
};
