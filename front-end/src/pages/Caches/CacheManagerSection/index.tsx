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

import { Accordion } from "components";
import type { ICachesManager } from "models";

import { CacheAccordionBody } from "../CacheAccordionBody";
import { CacheAccordionHeader } from "../CacheAccordionHeader";

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
        <>
            <div className={styles.CacheManagerWrapper}>
                <div className="CustomizedTable">
                    <div className={`TextMedium TableHeader ${styles.CacheManagerHeader}`}>
                        <div className={`RowChunk ${styles.CacheManagerName}`}>
                            {t("Caches.name")}: {cacheManager.name}
                        </div>
                        <div className={`RowChunk ${styles.RowChunk}`}>{t("Caches.clear")}</div>
                        <div className={`RowChunk ${styles.RowChunk}`}>{t("status")}</div>
                    </div>
                    {cacheManager.caches.map((cache) => (
                        <Accordion
                            header={<CacheAccordionHeader cacheManagerName={cacheManager.name} cache={cache} />}
                            children={<CacheAccordionBody cache={cache} />}
                            key={cache.name}
                            headerStyles={styles.HeaderStyles}
                        />
                    ))}
                </div>
            </div>
        </>
    );
};
