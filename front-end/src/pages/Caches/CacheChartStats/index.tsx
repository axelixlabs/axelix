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

import type { IGetSingleCacheResponseBody } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single cache data
     */
    singleCacheData: IGetSingleCacheResponseBody;
}

export const CacheChartStats = ({ singleCacheData }: IProps) => {
    const { t } = useTranslation();

    // TODO: Refactore this code in future
    const hitsCount = singleCacheData.hits.length;
    const missesCount = singleCacheData.misses.length;
    const allCountOfMissesAndHits = hitsCount + missesCount;
    const hitsPercentage = allCountOfMissesAndHits > 0 ? (hitsCount / allCountOfMissesAndHits) * 100 : 0;
    const missesPercentage = allCountOfMissesAndHits > 0 ? (missesCount / allCountOfMissesAndHits) * 100 : 0;

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.Percentage}>{t("Caches.hits")}</div>
                <div className={styles.Count}>{t("Caches.misses")}</div>
                {!!singleCacheData.estimatedEntrySize && (
                    <div className={styles.EstimatedEntrySize}>{t("Caches.estimatedEntrySize")}</div>
                )}
                <div className={styles.HeaderLine} />
                <div className={styles.PercentageValue}>
                    {hitsCount} ({Number(hitsPercentage.toFixed(1))}%)
                </div>
                <div className={styles.CountValue}>
                    {missesCount} ({Number(missesPercentage.toFixed(1))}%)
                </div>

                {!!singleCacheData.estimatedEntrySize && (
                    <div className={styles.EstimatedEntrySizeValue}>{singleCacheData.estimatedEntrySize}</div>
                )}
            </div>
        </>
    );
};
