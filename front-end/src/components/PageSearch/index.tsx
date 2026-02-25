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
import { AutoComplete, type AutoCompleteProps, Input, Space } from "antd";
import { type Dispatch, type SetStateAction, useRef } from "react";
import { useTranslation } from "react-i18next";

import styles from "./styles.module.css";

interface IProps {
    /**
     * SetState to update the search string
     */
    setSearch: Dispatch<SetStateAction<string>>;

    /**
     * Whether to add a bottom gutter to the search field
     */
    removeBottomGutter?: boolean;

    /**
     * Тext to display after the search field
     */
    addonAfter?: string;

    /**
     * Options for the autocomplete input.
     */
    autocompleteOptions?: AutoCompleteProps["options"];
}

export const PageSearch = ({ setSearch, addonAfter, autocompleteOptions, removeBottomGutter }: IProps) => {
    const { t } = useTranslation();

    const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const scheduleSetSearch = (value: string): void => {
        if (debounceRef.current) {
            clearTimeout(debounceRef.current);
        }

        debounceRef.current = setTimeout(() => setSearch(value), 500);
    };

    if (autocompleteOptions) {
        return (
            <>
                <Space.Compact
                    className={`${styles.SearchWrapper} ${removeBottomGutter ? styles.RemovedBottomGutter : ""} `}
                >
                    <AutoComplete
                        options={autocompleteOptions}
                        onChange={(value) => scheduleSetSearch(value)}
                        onSelect={(value) => setSearch(value)}
                        className={`${styles.Search}`}
                    >
                        <Input placeholder={t("search")} />
                    </AutoComplete>
                    {addonAfter && <Space.Addon className={styles.AddonAfter}>{addonAfter}</Space.Addon>}
                </Space.Compact>
            </>
        );
    }

    return (
        <>
            <Space.Compact
                className={`${styles.SearchWrapper} ${removeBottomGutter ? styles.RemovedBottomGutter : ""} `}
            >
                <Input placeholder={t("search")} onChange={(e) => scheduleSetSearch(e.target.value)} />
                {addonAfter && <Space.Addon className={styles.AddonAfter}>{addonAfter}</Space.Addon>}
            </Space.Compact>
        </>
    );
};
