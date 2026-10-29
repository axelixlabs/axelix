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
import { Checkbox } from "antd";
import { useTranslation } from "react-i18next";

import { EExportableComponent } from "@/models";
import { EXPORTABLE_COMPONENT_GROUPS } from "@/utils";

import styles from "../styles.module.css";

interface IProps {
    /**
     * The currently selected components
     */
    selected: EExportableComponent[];

    /**
     * Callback to select or deselect the given components
     */
    onToggle: (components: EExportableComponent[], on: boolean) => void;
}

export const StateChecklist = ({ selected, onToggle }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.Groups}>
                {EXPORTABLE_COMPONENT_GROUPS.map(({ key, components }) => {
                    const count = components.filter((component) => selected.includes(component)).length;

                    return (
                        <div key={key} className={styles.Group}>
                            <Checkbox
                                className={styles.GroupHeader}
                                checked={count === components.length}
                                indeterminate={count > 0 && count < components.length}
                                onChange={(e) => onToggle(components, e.target.checked)}
                            >
                                <span className={`TextCaps ${styles.GroupTitle}`}>
                                    {t(`Details.DownloadState.groups.${key}`)}
                                </span>
                                <span className={styles.GroupCount}>
                                    {t("Details.DownloadState.count", { selected: count, total: components.length })}
                                </span>
                            </Checkbox>

                            {components.map((component) => {
                                const isHeap = component === EExportableComponent.HEAP_DUMP;
                                const checked = selected.includes(component);

                                return (
                                    <div key={component} className={styles.ItemWrapper}>
                                        <Checkbox
                                            className={styles.Item}
                                            checked={checked}
                                            onChange={(e) => onToggle([component], e.target.checked)}
                                        >
                                            <span className={styles.ItemText}>
                                                <span className={styles.ItemName}>
                                                    {t(`Details.Components.${component}`)}
                                                </span>
                                                <span className={styles.ItemDescription}>
                                                    {t(`Details.DownloadState.descriptions.${component}`)}
                                                </span>
                                            </span>
                                            <span className={styles.ItemSize}>
                                                {t(`Details.DownloadState.itemSize.${isHeap ? "heap" : "small"}`)}
                                            </span>
                                        </Checkbox>
                                        {isHeap && checked && (
                                            <div className={styles.Warning}>
                                                <span className={styles.WarningTitle}>
                                                    {t("Details.DownloadState.heapWarning.title")}
                                                </span>
                                                <span>{t("Details.DownloadState.heapWarning.body")}</span>
                                            </div>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    );
                })}
            </div>
        </>
    );
};
