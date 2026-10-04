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

import styles from "./styles.module.css";

interface IProps {
    applicationsTotal: number;
    applicationsOnLts: number;
    applicationsOnNonLts: number;
}

export const LanguagesSummaryCard = ({ applicationsTotal, applicationsOnLts, applicationsOnNonLts }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.TotalWrapper}>
                    <span className={`TextLarge ${styles.TotalValue}`}>{applicationsTotal}</span>
                    <span className={`TextUltraSmall ${styles.TotalLabel}`}>
                        {t("Dashboard.Languages.applications")}
                    </span>
                </div>
                <div className={styles.Breakdown}>
                    <span className={styles.BreakdownRow}>
                        <span className={styles.Count}>{applicationsOnLts}</span>
                        <span className={`${styles.Badge} ${styles.BadgeLts}`}>{t("Dashboard.Languages.lts")}</span>
                        <span className={`TextUltraSmall ${styles.Hint}`}>{t("Dashboard.Languages.onLtsRelease")}</span>
                    </span>
                    <span className={styles.BreakdownRow}>
                        <span className={`${styles.Count} ${applicationsOnNonLts === 0 ? styles.CountMuted : ""}`}>
                            {applicationsOnNonLts}
                        </span>
                        <span className={`${styles.Badge} ${styles.BadgeNonLts}`}>
                            {t("Dashboard.Languages.nonLts")}
                        </span>
                        <span className={`TextUltraSmall ${styles.Hint}`}>
                            {t("Dashboard.Languages.onNonLtsRelease")}
                        </span>
                    </span>
                </div>
            </div>
        </>
    );
};
