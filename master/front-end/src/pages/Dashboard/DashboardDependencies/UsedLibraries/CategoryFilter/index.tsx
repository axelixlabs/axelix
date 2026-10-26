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
import { Button, Checkbox, Popover, Tag } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import type { EDependencyEcosystem } from "@/models";
import { DEPENDENCY_ECOSYSTEM_ORDER, dependencyEcosystemLabelKey } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    activeEcosystems: EDependencyEcosystem[];
    onChange: (ecosystems: EDependencyEcosystem[]) => void;
}

export const CategoryFilter = ({ activeEcosystems, onChange }: IProps) => {
    const { t } = useTranslation();

    const [open, setOpen] = useState<boolean>(false);

    const toggle = (ecosystem: EDependencyEcosystem): void => {
        onChange(
            activeEcosystems.includes(ecosystem)
                ? activeEcosystems.filter((current) => current !== ecosystem)
                : [...activeEcosystems, ecosystem],
        );
    };

    const options = (
        <div className={styles.Options}>
            {DEPENDENCY_ECOSYSTEM_ORDER.map((ecosystem) => (
                <Checkbox
                    key={ecosystem}
                    checked={activeEcosystems.includes(ecosystem)}
                    onChange={() => toggle(ecosystem)}
                    className={`TextSmall ${styles.Option}`}
                >
                    {t(dependencyEcosystemLabelKey[ecosystem])}
                </Checkbox>
            ))}

            <div className={`TextUltraSmall ${styles.OptionsFooter}`}>
                <span>
                    {activeEcosystems.length
                        ? t("Dashboard.Dependencies.categoriesSelected", {
                              count: activeEcosystems.length,
                              total: DEPENDENCY_ECOSYSTEM_ORDER.length,
                          })
                        : t("Dashboard.Dependencies.categoryPlaceholder")}
                </span>
                <button type="button" className={styles.Clear} onClick={() => onChange([])}>
                    {t("Dashboard.Dependencies.clear")}
                </button>
            </div>
        </div>
    );

    return (
        <>
            <Popover
                trigger="click"
                placement="bottomLeft"
                arrow={false}
                open={open}
                onOpenChange={setOpen}
                align={{ offset: [0, 6] }}
                styles={{ container: { padding: 6, borderRadius: 9 } }}
                content={options}
            >
                <Button
                    className={`${styles.Trigger} ${activeEcosystems.length ? styles.Filtered : ""}`}
                    aria-expanded={open}
                >
                    {t("Dashboard.Dependencies.columns.category")}
                    <span className={styles.TriggerValue}>
                        {activeEcosystems.length
                            ? t("Dashboard.Dependencies.selected", { count: activeEcosystems.length })
                            : t("Dashboard.Dependencies.all")}
                    </span>
                    <span className={styles.Caret}>▾</span>
                </Button>
            </Popover>

            {activeEcosystems.map((ecosystem) => (
                <Tag
                    key={ecosystem}
                    closable
                    onClose={(event) => {
                        event.preventDefault();
                        toggle(ecosystem);
                    }}
                    className={`TextUltraSmall ${styles.Selected}`}
                >
                    {t(dependencyEcosystemLabelKey[ecosystem])}
                </Tag>
            ))}
        </>
    );
};
