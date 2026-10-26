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

import type { EDependencyEcosystem } from "@/models";
import { DEPENDENCY_ECOSYSTEM_ORDER, dependencyEcosystemLabelKey } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    activeEcosystems: EDependencyEcosystem[];
    onToggle: (ecosystem: EDependencyEcosystem) => void;
    onClear: () => void;
}

export const CategoryOptions = ({ activeEcosystems, onToggle, onClear }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.MainWrapper}>
                {DEPENDENCY_ECOSYSTEM_ORDER.map((ecosystem) => (
                    <Checkbox
                        key={ecosystem}
                        checked={activeEcosystems.includes(ecosystem)}
                        onChange={() => onToggle(ecosystem)}
                        className={`TextSmall ${styles.Option}`}
                    >
                        {t(dependencyEcosystemLabelKey[ecosystem])}
                    </Checkbox>
                ))}

                <div className={`TextUltraSmall ${styles.Footer}`}>
                    <span>
                        {activeEcosystems.length
                            ? t("Dashboard.Dependencies.categoriesSelected", {
                                  count: activeEcosystems.length,
                                  total: DEPENDENCY_ECOSYSTEM_ORDER.length,
                              })
                            : t("Dashboard.Dependencies.categoryPlaceholder")}
                    </span>
                    <button type="button" className={styles.Clear} onClick={onClear}>
                        {t("Dashboard.Dependencies.clear")}
                    </button>
                </div>
            </div>
        </>
    );
};
