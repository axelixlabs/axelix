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
import type { EDependencyEcosystem, ESupportSignal, IResolvedDependency } from "@/models";
import { SUPPORT_SIGNAL_ORDER, supportSignalClassToken, supportSignalLabelKey } from "@/utils";

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

    /**
     * The currently active support-signal filters.
     */
    activeSignals: ESupportSignal[];

    /**
     * Toggles a support signal in the active filters.
     */
    onToggle: (signal: ESupportSignal) => void;

    /**
     * Drops every active support-signal filter.
     */
    onClear: () => void;
}

export const SupportSignalFilter = ({
    dependencies,
    search,
    activeEcosystem,
    activeSignals,
    onToggle,
    onClear,
}: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={`TextUltraSmall ${styles.MainWrapper}`}>
                {SUPPORT_SIGNAL_ORDER.map((signal) => {
                    const active = activeSignals.includes(signal);
                    const count = dependencies.filter(
                        (dependency) =>
                            dependency.signal?.kind === signal &&
                            matchesDependencyFilters(dependency, search, activeEcosystem, []),
                    ).length;

                    return (
                        <button
                            key={signal}
                            type="button"
                            onClick={() => onToggle(signal)}
                            className={`${styles.Chip} ${styles[supportSignalClassToken[signal]]} ${active ? styles.ActiveChip : ""}`}
                        >
                            <span className={styles.ChipDot} />
                            {t(supportSignalLabelKey[signal])}
                            <span className={styles.Count}>{count}</span>
                        </button>
                    );
                })}

                {activeSignals.length > 0 && (
                    <button type="button" onClick={onClear} className={styles.Clear}>
                        {t("DependenciesAnalyzer.feed.clear")}
                    </button>
                )}
            </div>
        </>
    );
};
