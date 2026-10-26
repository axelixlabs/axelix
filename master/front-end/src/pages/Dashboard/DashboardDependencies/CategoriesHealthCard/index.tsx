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
import { Trans, useTranslation } from "react-i18next";

import type { EDependencyEcosystem, IDependenciesDashboardResponseBody } from "@/models";
import {
    DEPENDENCY_ECOSYSTEM_ORDER,
    SUPPORT_STATUS_ORDER,
    dependencyEcosystemLabelKey,
    dependencyHealthColor,
    supportSignalLabelKey,
} from "@/utils";

import { HealthCard } from "../HealthCard";

import styles from "./styles.module.css";

interface IProps {
    totalLibraries: number;
    librariesByStatus: IDependenciesDashboardResponseBody["librariesByStatus"];
    riskyLibrariesByEcosystem: IDependenciesDashboardResponseBody["riskyLibrariesByEcosystem"];
    activeEcosystems: EDependencyEcosystem[];
    onToggle: (ecosystem: EDependencyEcosystem) => void;
    onShowAll: () => void;
    lastScan: string;
}

const TOP_CATEGORIES = 5;

const sumOf = (byStatus: IDependenciesDashboardResponseBody["librariesByStatus"]): number =>
    SUPPORT_STATUS_ORDER.reduce((sum, status) => sum + (byStatus[status] ?? 0), 0);

export const CategoriesHealthCard = ({
    totalLibraries,
    librariesByStatus,
    riskyLibrariesByEcosystem,
    activeEcosystems,
    onToggle,
    onShowAll,
    lastScan,
}: IProps) => {
    const { t } = useTranslation();

    // The categories with the most risky libraries first, the ties keep the usual order of the categories
    const ranked = DEPENDENCY_ECOSYSTEM_ORDER.map((ecosystem) => {
        const byStatus = riskyLibrariesByEcosystem[ecosystem] ?? {};

        return { ecosystem, byStatus, total: sumOf(byStatus) };
    })
        .filter(({ total }) => total > 0)
        .sort((left, right) => right.total - left.total);

    const top = ranked.slice(0, TOP_CATEGORIES);
    const hidden = ranked.length - top.length;
    const max = Math.max(1, ...top.map(({ total }) => total));

    return (
        <>
            <HealthCard
                eyebrow={t("Dashboard.Dependencies.categoriesEyebrow")}
                title={t("Dashboard.Dependencies.libraries")}
                descriptor={
                    <Trans
                        t={t}
                        i18nKey="Dashboard.Dependencies.categoriesDescriptor"
                        values={{ risky: sumOf(librariesByStatus), total: totalLibraries }}
                        components={[<b key="0" />, <b key="1" />]}
                    />
                }
                footerLeft={t("Dashboard.Dependencies.categoriesFooter")}
                footerRight={lastScan}
            >
                <div className={styles.Body}>
                    <div className={`TextUltraSmall ${styles.Caption}`}>
                        <span className={styles.CaptionLabel}>{t("Dashboard.Dependencies.topCategories")}</span>
                        <span className={styles.Legend}>
                            {SUPPORT_STATUS_ORDER.map((status) => (
                                <span key={status} className={styles.LegendItem}>
                                    <span
                                        className={styles.LegendSwatch}
                                        style={{ backgroundColor: dependencyHealthColor[status] }}
                                    />
                                    {t(supportSignalLabelKey[status])}
                                </span>
                            ))}
                        </span>
                    </div>

                    <div className={styles.Bars}>
                        {top.length === 0 && (
                            <span className={`TextSmall ${styles.Muted}`}>
                                {t("Dashboard.Dependencies.noRiskyCategories")}
                            </span>
                        )}

                        {top.map(({ ecosystem, byStatus, total }) => (
                            <button
                                key={ecosystem}
                                type="button"
                                onClick={() => onToggle(ecosystem)}
                                className={`${styles.BarRow} ${activeEcosystems.includes(ecosystem) ? styles.ActiveBarRow : ""}`}
                            >
                                <span className={`TextSmall ${styles.BarLabel}`}>
                                    {t(dependencyEcosystemLabelKey[ecosystem])}
                                </span>
                                <span className={styles.BarTrack}>
                                    <span className={styles.Bar} style={{ width: `${(total * 100) / max}%` }}>
                                        {SUPPORT_STATUS_ORDER.map((status) => (
                                            <span
                                                key={status}
                                                style={{
                                                    flex: byStatus[status] ?? 0,
                                                    backgroundColor: dependencyHealthColor[status],
                                                }}
                                            />
                                        ))}
                                    </span>
                                </span>
                                <span className={`TextSmall ${styles.BarCount}`}>{total}</span>
                            </button>
                        ))}

                        {hidden > 0 && (
                            <button type="button" className={`TextUltraSmall ${styles.More}`} onClick={onShowAll}>
                                {t("Dashboard.Dependencies.moreCategories", { count: hidden })}
                            </button>
                        )}
                    </div>
                </div>
            </HealthCard>
        </>
    );
};
