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

import { ELookupOutcome, type IGetSingleCacheResponseBody } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single cache data
     */
    cacheData: IGetSingleCacheResponseBody;
}

export const CacheChartStats = ({ cacheData }: IProps) => {
    const { t } = useTranslation();

    const hitsCount = cacheData.lookupHistory.filter((value) => value.outcome === ELookupOutcome.HIT).length;
    const missCount = cacheData.lookupHistory.filter((value) => value.outcome === ELookupOutcome.MISS).length;

    const hitsPercentage = (hitsCount / cacheData.lookupHistory.length) * 100;
    const missesPercentage = (missCount / cacheData.lookupHistory.length) * 100;

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.Percentage}>{t("Caches.hits")}</div>
                <div className={styles.Count}>{t("Caches.misses")}</div>
                {cacheData.estimatedEntrySize && (
                    <div className={styles.EstimatedEntrySize}>{t("Caches.estimatedEntrySize")}</div>
                )}
                <div className={styles.HeaderLine} />
                <div className={styles.Statistics}>
                    <div>{t("Caches.statistics")}:</div>
                </div>
                <div className={styles.PercentageValue}>
                    {hitsCount} ({Number(hitsPercentage.toFixed(1))}%)
                </div>
                <div className={styles.CountValue}>
                    {missCount} ({Number(missesPercentage.toFixed(1))}%)
                </div>

                {cacheData.estimatedEntrySize && (
                    <div className={styles.EstimatedEntrySizeValue}>{cacheData.estimatedEntrySize}</div>
                )}
            </div>
        </>
    );
};
