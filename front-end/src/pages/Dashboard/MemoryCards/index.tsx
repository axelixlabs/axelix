/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        <div className={styles.MainWrapper}>
            <div className={`TextMedium ${styles.Title}`}>{t("Dashboard.statistics")}</div>
            <div className={styles.CardsWrapper}>
                <div className={styles.CardWrapper}>
                    <div className={styles.CardTitle}>{t("Dashboard.totalServicesCount")}</div>
                    <div className={`TextMedium ${styles.CardValue}`}>{statusesTotalCount}</div>
                </div>
                <div className={styles.CardWrapper}>
                    <div className={styles.CardTitle}>Average Heap Size</div>
                    <div className={`TextMedium ${styles.CardValue}`}>
                        {memoryUsage.averageHeapSize.value} {memoryUsage.averageHeapSize.unit}
                    </div>
                </div>
                <div className={styles.CardWrapper}>
                    <div className={styles.CardTitle}>Total Heap Size</div>
                    <div className={`TextMedium ${styles.CardValue}`}>
                        {memoryUsage.totalHeapSize.value} {memoryUsage.totalHeapSize.unit}
                    </div>
                </div>
            </div>
        </div>
    );
};
