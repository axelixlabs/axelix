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
    applicationsFullyOssSupported: number;
}

export const PortfolioSummary = ({ applicationsTotal, applicationsFullyOssSupported }: IProps) => {
    const { t } = useTranslation();

    const notFullySupported = Math.max(applicationsTotal - applicationsFullyOssSupported, 0);

    return (
        <>
            <div className={styles.MainWrapper}>
                <div>
                    <div className="TextLarge">{applicationsTotal}</div>
                    <div className={`TextUltraSmall ${styles.TotalLabel}`}>
                        {t("Dashboard.SpringPortfolio.applications")}
                    </div>
                </div>
                <div className={styles.Breakdown}>
                    <span className={styles.BreakdownRow}>
                        <span className={styles.SupportedCount}>{applicationsFullyOssSupported}</span>
                        <span className={`${styles.Chip} ${styles.ChipOk}`}>
                            {t("Dashboard.SpringPortfolio.ossOk")}
                        </span>
                        <span className={`TextUltraSmall ${styles.BreakdownHint}`}>
                            {t("Dashboard.SpringPortfolio.supportedHint")}
                        </span>
                    </span>
                    <span className={styles.BreakdownRow}>
                        <span className={styles.UnsupportedCount}>{notFullySupported}</span>
                        <span className={`${styles.Chip} ${styles.ChipNo}`}>
                            {t("Dashboard.SpringPortfolio.ossNo")}
                        </span>
                        <span className={`TextUltraSmall ${styles.BreakdownHint}`}>
                            {t("Dashboard.SpringPortfolio.unsupportedHint")}
                        </span>
                    </span>
                </div>
            </div>
        </>
    );
};
