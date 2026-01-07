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
import { ReloadOutlined } from "@ant-design/icons";

import { Button, message } from "antd";
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
    );
};
