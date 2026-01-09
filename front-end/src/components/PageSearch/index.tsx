/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        );
    }

    return (
        <Space.Compact className={`${styles.SearchWrapper} ${removeBottomGutter ? styles.RemovedBottomGutter : ""} `}>
            <Input placeholder={t("search")} onChange={(e) => scheduleSetSearch(e.target.value)} />
            {addonAfter && <Space.Addon className={styles.AddonAfter}>{addonAfter}</Space.Addon>}
        </Space.Compact>
    );
};
