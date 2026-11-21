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
import { Checkbox, Input } from "antd";
import { useTranslation } from "react-i18next";

import type { IFilterOption } from "../types";

import styles from "./styles.module.css";

interface IProps<T extends string> {
    allLabel: string;
    options: IFilterOption<T>[];
    selected: T[];
    onToggle: (value: T) => void;
    onClear: () => void;
    searchField?: { value: string; onChange: (value: string) => void };
}

export const FilterOptions = <T extends string>({
    allLabel,
    options,
    selected,
    onToggle,
    onClear,
    searchField,
}: IProps<T>) => {
    const { t } = useTranslation();
    const query = searchField?.value.trim().toLowerCase() ?? "";
    const visibleOptions = options.filter(({ label }) => label.toLowerCase().includes(query));

    return (
        <div className={styles.MainWrapper} data-searchable={Boolean(searchField) || undefined}>
            {searchField && (
                <Input
                    className={styles.Search}
                    aria-label={t("search")}
                    placeholder={t("search")}
                    value={searchField.value}
                    onChange={(event) => searchField.onChange(event.target.value)}
                />
            )}
            <div className={`ThinScrollbar ${styles.Options}`}>
                {visibleOptions.map(({ value, label }) => (
                    <Checkbox
                        key={value}
                        checked={selected.includes(value)}
                        onChange={() => onToggle(value)}
                        className={`TextSmall ${styles.Option}`}
                    >
                        {label}
                    </Checkbox>
                ))}
                {visibleOptions.length === 0 && <span className={styles.Empty}>{t("noData")}</span>}
            </div>

            <div className={`TextUltraSmall ${styles.Footer}`}>
                <span>
                    {selected.length
                        ? t("MultiSelectFilter.selectedOfTotal", { count: selected.length, total: options.length })
                        : allLabel}
                </span>
                <button type="button" className={styles.Clear} onClick={onClear}>
                    {t("MultiSelectFilter.clear")}
                </button>
            </div>
        </div>
    );
};
