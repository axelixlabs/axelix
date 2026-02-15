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

import { InfoTooltip } from "components";
import { formatTransactionDuration } from "helpers";
import type { IExecutionStats } from "models";

import styles from "./styles.module.css";

interface IProps {
    stats: IExecutionStats;
}

export const TransactionalMethodExecutionStats = ({ stats }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.Max}>{t("Transactional.max")}</div>
                <div className={styles.Average}>{t("Transactional.average")}</div>
                <div className={styles.Median}>{t("Transactional.median")}</div>
                <div className={styles.HeaderLine}></div>
                <div className={styles.Duration}>
                    <div>{t("Transactional.duration")}:</div>
                    <InfoTooltip text={t("Transactional.durationTooltip")}></InfoTooltip>
                </div>
                <div className={styles.MaxValue}>{formatTransactionDuration(stats.maxDurationMs)}</div>
                <div className={styles.AvgValue}>{formatTransactionDuration(stats.averageDurationMs)}</div>
                <div className={styles.MeanValue}>{formatTransactionDuration(stats.medianDurationMs)}</div>
            </div>
        </>
    );
};
