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
import { SUPPORT_STATUS_ORDER, supportSignalLabelKey } from "@/utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Every resolved dependency, unfiltered — the chip counters are computed over all of them.
     */
    dependencies: IResolvedDependency[];

    /**
     * The current free-text search, which the chip counters take into account.
     */
    search: string;

    /**
     * The selected ecosystem, which the chip counters take into account.
     */
    activeEcosystem: EDependencyEcosystem | null;

    activeStatuses: ESupportStatus[];

    onToggle: (status: ESupportStatus) => void;

    onClear: () => void;
}

export const SupportSignalFilter = ({
    dependencies,
    search,
    activeEcosystem,
    activeStatuses,
    onToggle,
    onClear,
}: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={`TextUltraSmall ${styles.MainWrapper}`}>
                {SUPPORT_STATUS_ORDER.map((status) => {
                    const active = activeStatuses.includes(status);

                    const filteredDependencies = dependencies.filter(
                        (dependency) =>
                            dependency.softwareProject?.status === status &&
                            matchesDependencyFilters(dependency, search, activeEcosystem, []),
                    );

                    const count = filteredDependencies.length;

                    return (
                        <button
                            key={status}
                            type="button"
                            onClick={() => onToggle(status)}
                            className={`${styles.Chip} ${styles[status]} ${active ? styles.ActiveChip : ""}`}
                        >
                            <span className={styles.ChipDot} />
                            {t(supportSignalLabelKey[status])}
                            <span className={styles.Count}>{count}</span>
                        </button>
                    );
                })}

                {activeStatuses.length > 0 && (
                    <button type="button" onClick={onClear} className={styles.Clear}>
                        {t("DependenciesAnalyzer.feed.clear")}
                    </button>
                )}
            </div>
        </>
    );
};
