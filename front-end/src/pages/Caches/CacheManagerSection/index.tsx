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

import type { ICachesManager } from "models";

import { CacheTableRow } from "../CacheTableRow";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Single cache manager data
     */
    cacheManager: ICachesManager;
}

export const CacheManagerSection = ({ cacheManager }: IProps) => {
    const { t } = useTranslation();

    return (
        <div className={styles.CacheManagerWrapper}>
            <div className="CustomizedAntdTable">
                <div className={`TableHeader ${styles.CacheManagerHeader}`}>
                    <div className={`RowChunk ${styles.CacheManagerName}`}>
                        {t("Caches.name")}: {cacheManager.name}
                    </div>
                    <div className={`RowChunk ${styles.RowChunk}`}>{t("Caches.clear")}</div>
                    <div className={`RowChunk ${styles.RowChunk}`}>{t("status")}</div>
                </div>
                {cacheManager.caches.map((cache) => (
                    <CacheTableRow cacheManagerName={cacheManager.name} cache={cache} key={cache.name} />
                ))}
            </div>
        </div>
    );
};
