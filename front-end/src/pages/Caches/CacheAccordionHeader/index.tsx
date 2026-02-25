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
import { ReloadOutlined } from "@ant-design/icons";

import { App, Button } from "antd";
import type { AxiosError } from "axios";
import { type MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { TooltipWithCopy } from "components";
import { extractErrorCode } from "helpers";
import { type ICacheData, type IErrorResponse, StatelessRequest } from "models";
import { clearCacheData } from "services";

import { CacheStatusSwitch } from "../CacheStatusSwitch";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Name of the cache manager
     */
    cacheManagerName: string;

    /**
     * Single cache data
     */
    cache: ICacheData;
}

export const CacheAccordionHeader = ({ cacheManagerName, cache }: IProps) => {
    const { instanceId } = useParams();
    const { t } = useTranslation();
    const { message } = App.useApp();

    const [clearSingleCache, setClearSingleCache] = useState(StatelessRequest.inactive());

    const clearCacheClickHandler = (e: MouseEvent<HTMLElement>): void => {
        e.stopPropagation();
        setClearSingleCache(StatelessRequest.loading());
        clearCacheData({
            instanceId: instanceId!,
            cacheName: cache.name,
            cacheManager: cacheManagerName,
        })
            .then(() => {
                setClearSingleCache(StatelessRequest.success());
                message.success(t("Caches.cleared"));
            })
            .catch((error: AxiosError<IErrorResponse>) => {
                setClearSingleCache(StatelessRequest.error(extractErrorCode(error?.response?.data)));
            });
    };

    return (
        <>
            <div className={styles.AccordionHeader}>
                <div className={styles.ContentRowChunkWrapper}>
                    <div className={styles.ContentWrapper}>
                        <span>{t("Caches.name")}: </span>
                        <span className={styles.CacheName}>{cache.name}</span>
                        <div className={styles.TargetWrapper}>
                            <div className={styles.Target}>{t("Caches.target")}:</div>
                            <TooltipWithCopy text={cache.target} />
                        </div>
                    </div>
                </div>
                <div className={styles.RowChunk}>
                    <Button
                        icon={<ReloadOutlined />}
                        type="primary"
                        loading={clearSingleCache.loading}
                        onClick={clearCacheClickHandler}
                    />
                </div>
                <div className={styles.RowChunk}>
                    <CacheStatusSwitch cacheManagerName={cacheManagerName} cache={cache} />
                </div>
            </div>
        </>
    );
};
