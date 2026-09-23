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

import sharedStyles from "../shared.module.css";

import styles from "./styles.module.css";

interface IProps {
    startYear: number;
    endYear: number;
    position: (time: number) => number;
}

export const MaintenanceLadderCardHeader = ({ startYear, endYear, position }: IProps) => {
    const { t } = useTranslation();
    const years = Array.from({ length: endYear - startYear }, (_, index) => startYear + index);

    return (
        <>
            <div className={`TextUltraSmall ${sharedStyles.Row} ${styles.HeaderRow}`}>
                <span className={styles.HeaderCell}>{t("Dashboard.SpringPortfolio.line")}</span>
                <span className={styles.Axis}>
                    {years.map((year) => (
                        <span
                            key={year}
                            className={styles.AxisTick}
                            style={{ left: `${position(new Date(year, 0, 1).getTime())}%` }}
                        >
                            {year}
                        </span>
                    ))}
                </span>
                <span className={`${styles.HeaderCell} ${styles.HeaderRight}`}>
                    {t("Dashboard.SpringPortfolio.ossEnds")}
                </span>
                <span className={`${styles.HeaderCell} ${styles.HeaderRight}`}>
                    {t("Dashboard.SpringPortfolio.apps")}
                </span>
            </div>
        </>
    );
};
