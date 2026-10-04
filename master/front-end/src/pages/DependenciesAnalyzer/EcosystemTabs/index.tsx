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
import { useTranslation } from "react-i18next";

import { matchesDependencyFilters } from "@/helpers";
import type { EDependencyEcosystem, ESupportStatus, IResolvedDependency } from "@/models";
import { DEPENDENCY_ECOSYSTEM_ORDER, dependencyEcosystemLabelKey } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Every resolved dependency, unfiltered — the tab counters are computed over all of them.
     */
    dependencies: IResolvedDependency[];

    /**
     * The current free-text search, which the tab counters take into account.
     */
    search: string;

    activeStatuses: ESupportStatus[];

    /**
     * The selected ecosystem, or null for the leading "All" tab.
     */
    activeEcosystem: EDependencyEcosystem | null;

    /**
     * Selects an ecosystem, null meaning the leading "All" tab.
     */
    onPick: (ecosystem: EDependencyEcosystem | null) => void;
}

/* TODO: Consider using antd tabs in the future */
export const EcosystemTabs = ({ dependencies, search, activeStatuses, activeEcosystem, onPick }: IProps) => {
    const { t } = useTranslation();

    const tabs = [null, ...DEPENDENCY_ECOSYSTEM_ORDER]
        .map((ecosystem) => ({
            ecosystem,
            count: dependencies.filter((dependency) =>
                matchesDependencyFilters(dependency, search, ecosystem, activeStatuses),
            ).length,
        }))
        .filter((tab) => tab.ecosystem === null || tab.count > 0);

    return (
        <>
            <div className={`TextSmall ${styles.MainWrapper}`}>
                {tabs.map(({ ecosystem, count }) => {
                    const active = ecosystem === activeEcosystem;

                    return (
                        <button
                            key={ecosystem ?? "ALL"}
                            type="button"
                            onClick={() => onPick(ecosystem)}
                            className={`${styles.Tab} ${active ? styles.ActiveTab : ""}`}
                        >
                            {ecosystem === null
                                ? t("DependenciesAnalyzer.ecosystems.ALL")
                                : t(dependencyEcosystemLabelKey[ecosystem])}
                            <span className={`TextUltraSmall ${styles.Count}`}>{count}</span>
                        </button>
                    );
                })}
            </div>
        </>
    );
};
