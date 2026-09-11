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

import type { IPropertyOccurrence } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Every occurrence of the property across the property sources, highest precedence first
     */
    chain: IPropertyOccurrence[];
}

/**
 * Shows which property sources define a property and which of the values actually wins. The first
 * occurrence is the effective one; every following occurrence is struck through, because Spring
 * resolves the property from the highest precedence source that defines it and never looks further.
 */
export const EnvironmentPrecedenceChain = ({ chain }: IProps) => {
    const { t } = useTranslation();

    return (
        <div className={styles.MainWrapper}>
            <div className={styles.Cells}>
                {chain.map(({ propertySourceName, value }, index) => {
                    const isWinning = index === 0;

                    return (
                        <div
                            className={`${styles.Cell} ${isWinning ? styles.WinningCell : ""}`}
                            key={`${propertySourceName}-${index}`}
                        >
                            <div className={styles.CellHeader}>
                                <span className={styles.SourceName}>{propertySourceName}</span>
                                {isWinning && <span className={styles.WinsBadge}>{t("Environments.wins")}</span>}
                            </div>
                            <span className={styles.Value} title={value}>
                                {value}
                            </span>
                        </div>
                    );
                })}
            </div>

            <div className={styles.Rail}>
                <span>{t("Environments.highestPrecedence")}</span>
                <span className={styles.RailLine} />
                <span>{t("Environments.lowestPrecedence")}</span>
            </div>
        </div>
    );
};
