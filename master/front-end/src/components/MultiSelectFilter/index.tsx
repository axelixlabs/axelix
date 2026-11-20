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
import { Button, Checkbox, Input, Popover, Tag } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { toggleItem } from "@/helpers";

import styles from "./styles.module.css";

interface IProps<T extends string> {
    /** The name of the filter shown on the trigger. */
    label: string;
    /** The footer message shown when no option is selected. */
    allLabel: string;
    /** The available choices and their localized labels. */
    options: { value: T; label: string }[];
    /** The currently selected values. */
    selected: T[];
    /** Replaces the selection when an option is toggled or the filter is cleared. */
    onChange: (values: T[]) => void;
    /** Whether users can search the available choices by label. */
    searchable?: boolean;
}

export const MultiSelectFilter = <T extends string>({
    label,
    allLabel,
    options,
    selected,
    onChange,
    searchable = false,
}: IProps<T>) => {
    const { t } = useTranslation();
    const [open, setOpen] = useState(false);
    const [search, setSearch] = useState("");
    const query = search.trim().toLowerCase();
    const visibleOptions = options.filter((option) => option.label.toLowerCase().includes(query));
    const toggle = (value: T): void => onChange(toggleItem(selected, value));

    return (
        <>
            <Popover
                trigger="click"
                placement="bottomLeft"
                arrow={false}
                open={open}
                onOpenChange={(nextOpen) => {
                    setOpen(nextOpen);
                    if (!nextOpen) {
                        setSearch("");
                    }
                }}
                align={{ offset: [0, 6] }}
                styles={{ container: { padding: 6, borderRadius: 9 } }}
                content={
                    <div className={styles.MainWrapper} data-searchable={searchable || undefined}>
                        {searchable && (
                            <Input
                                className={styles.Search}
                                aria-label={t("search")}
                                placeholder={t("search")}
                                value={search}
                                onChange={(event) => setSearch(event.target.value)}
                            />
                        )}
                        <div className={`ThinScrollbar ${styles.Options}`}>
                            {visibleOptions.map((option) => (
                                <Checkbox
                                    key={option.value}
                                    checked={selected.includes(option.value)}
                                    onChange={() => toggle(option.value)}
                                    className={`TextSmall ${styles.Option}`}
                                >
                                    {option.label}
                                </Checkbox>
                            ))}
                            {visibleOptions.length === 0 && <span className={styles.Empty}>{t("noData")}</span>}
                        </div>

                        <div className={`TextUltraSmall ${styles.Footer}`}>
                            <span>
                                {selected.length
                                    ? t("MultiSelectFilter.selectedOfTotal", {
                                          count: selected.length,
                                          total: options.length,
                                      })
                                    : allLabel}
                            </span>
                            <button type="button" className={styles.Clear} onClick={() => onChange([])}>
                                {t("MultiSelectFilter.clear")}
                            </button>
                        </div>
                    </div>
                }
            >
                <Button
                    className={`${styles.Trigger} ${selected.length ? styles.Filtered : ""}`}
                    data-searchable={searchable || undefined}
                    aria-expanded={open}
                >
                    {label}
                    <span className={styles.TriggerValue}>
                        {selected.length
                            ? t("MultiSelectFilter.selected", { count: selected.length })
                            : t("MultiSelectFilter.all")}
                    </span>
                    <span className={styles.Caret} aria-hidden="true">
                        ▾
                    </span>
                </Button>
            </Popover>

            {selected.map((value) => (
                <Tag
                    key={value}
                    closable
                    onClose={(event) => {
                        event.preventDefault();
                        toggle(value);
                    }}
                    className={`TextUltraSmall ${styles.Selected}`}
                >
                    {options.find((option) => option.value === value)?.label ?? value}
                </Tag>
            ))}
        </>
    );
};
