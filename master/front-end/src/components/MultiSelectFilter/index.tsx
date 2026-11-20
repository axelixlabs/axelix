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
import { Button, Popover, Tag } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { toggleItem } from "@/helpers";

import { FilterOptions } from "./FilterOptions";
import styles from "./styles.module.css";
import type { IFilterOption } from "./types";

interface IProps<T extends string> {
    /** The name of the filter shown on the trigger. */
    label: string;
    /** The footer message shown when no option is selected. */
    allLabel: string;
    /** The available choices and their localized labels. */
    options: IFilterOption<T>[];
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
                    <FilterOptions
                        allLabel={allLabel}
                        options={options}
                        selected={selected}
                        onToggle={toggle}
                        onClear={() => onChange([])}
                        searchField={searchable ? { value: search, onChange: setSearch } : undefined}
                    />
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
