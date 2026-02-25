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

import type { IMemoryUsage } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Memory usage metrics.
     */
    memoryUsage: IMemoryUsage;

    /**
     * Total number of services
     */
    statusesTotalCount: number;
}

export const MemoryCards = ({ memoryUsage, statusesTotalCount }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={`TextLarge ${styles.Title}`}>{t("Dashboard.statistics")}</div>
                <div className={styles.CardsWrapper}>
                    <div className={styles.CardWrapper}>
                        <div className={styles.CardTitle}>{t("Dashboard.totalServicesCount")}</div>
                        <div className={`TextLarge ${styles.CardValue}`}>{statusesTotalCount}</div>
                    </div>
                    <div className={styles.CardWrapper}>
                        <div className={styles.CardTitle}>Average Heap Size</div>
                        <div className={`TextLarge ${styles.CardValue}`}>
                            {memoryUsage.averageHeapSize.value} {memoryUsage.averageHeapSize.unit}
                        </div>
                    </div>
                    <div className={styles.CardWrapper}>
                        <div className={styles.CardTitle}>Total Heap Size</div>
                        <div className={`TextLarge ${styles.CardValue}`}>
                            {memoryUsage.totalHeapSize.value} {memoryUsage.totalHeapSize.unit}
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};
