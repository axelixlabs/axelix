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
    /**
     * The total number of analyzed transactions.
     */
    analyzed: number;

    /**
     * The number of transactions with at least one detected problem.
     */
    problematic: number;

    /**
     * The number of clean transactions.
     */
    clean: number;
}

export const TransactionControlSummary = ({ analyzed, problematic, clean }: IProps) => {
    const { t } = useTranslation();

    return (
        <div className={styles.Summary}>
            <div className={styles.Card}>
                <span className={styles.Label}>{t("Transactional.summary.analyzed")}</span>
                <span className={styles.Value}>{analyzed}</span>
            </div>
            <div className={`${styles.Card} ${styles.Problematic}`}>
                <span className={styles.Label}>{t("Transactional.summary.withProblems")}</span>
                <span className={styles.Value}>{problematic}</span>
            </div>
            <div className={`${styles.Card} ${styles.Clean}`}>
                <span className={styles.Label}>{t("Transactional.summary.clean")}</span>
                <span className={styles.Value}>{clean}</span>
            </div>
        </div>
    );
};
