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

import { rankEcosystems, sumOfRisky } from "@/helpers";
import type { EDependencyEcosystem, TEcosystemSupportStatusCounts, TSupportStatusCounts } from "@/models";

import { HealthCard } from "../HealthCard";

import { CategoryBar } from "./CategoryBar";
import { StatusLegend } from "./StatusLegend";
import styles from "./styles.module.css";

interface IProps {
    totalLibraries: number;
    librariesByStatus: TSupportStatusCounts;
    riskyLibrariesByEcosystem: TEcosystemSupportStatusCounts;
    activeEcosystems: EDependencyEcosystem[];
    onToggle: (ecosystem: EDependencyEcosystem) => void;
    onShowAll: () => void;
    lastScan: string;
}

const TOP_CATEGORIES = 5;

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

    const ranked = rankEcosystems(riskyLibrariesByEcosystem);
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
                        values={{ risky: sumOfRisky(librariesByStatus), total: totalLibraries }}
                        components={[<b key="0" />, <b key="1" />]}
                    />
                }
                footerLeft={t("Dashboard.Dependencies.categoriesFooter")}
                footerRight={lastScan}
            >
                <div className={styles.Body}>
                    <StatusLegend />

                    <div className={styles.Bars}>
                        {top.length === 0 && (
                            <span className={`TextSmall ${styles.Muted}`}>
                                {t("Dashboard.Dependencies.noRiskyCategories")}
                            </span>
                        )}

                        {top.map((category) => (
                            <CategoryBar
                                key={category.ecosystem}
                                category={category}
                                max={max}
                                active={activeEcosystems.includes(category.ecosystem)}
                                onToggle={() => onToggle(category.ecosystem)}
                            />
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
