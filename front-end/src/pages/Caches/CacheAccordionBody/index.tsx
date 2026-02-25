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

import type { ICacheData } from "models";

import { CacheChart } from "./CacheChart";
import styles from "./styles.module.css";

interface IProps {
    /**
     * Single cache data
     */
    cache: ICacheData;
}

export const CacheAccordionBody = ({ cache }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.CacheDataWrapper}>
                    <div>{t("Caches.hitsCount")}:</div>
                    <div>{cache.hitsCount}</div>

                    <div>{t("Caches.missesCount")}:</div>
                    <div>{cache.missesCount}</div>

                    {!!cache.estimatedEntrySize && (
                        <>
                            <div>{t("Caches.estimatedEntrySize")}:</div>
                            <div>{cache.estimatedEntrySize}</div>
                        </>
                    )}
                </div>

                <CacheChart cache={cache} />
            </div>
        </>
    );
};
