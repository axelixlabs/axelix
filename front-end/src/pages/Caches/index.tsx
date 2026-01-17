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
import { App, Button } from "antd";
import type { AxiosError } from "axios";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader, PageSearch } from "components";
import { extractErrorCode, fetchData, filterCacheManagers } from "helpers";
import { type ICachesResponseBody, type IErrorResponse, StatefulRequest, StatelessRequest } from "models";
import { clearAllCachesData, getCachesData } from "services";

import { CacheManagerSection } from "./CacheManagerSection";
import styles from "./styles.module.css";

const Caches = () => {
    const { t } = useTranslation();
    const { instanceId } = useParams();
    const { message } = App.useApp();

    const [search, setSearch] = useState<string>("");

    const [clearAllCaches, setClearAllCaches] = useState(StatelessRequest.inactive());

    const [cacheData, setCacheData] = useState(StatefulRequest.loading<ICachesResponseBody>());
    useEffect(() => {
        fetchData(setCacheData, () => getCachesData(instanceId!));
    }, []);

    if (cacheData.loading) {
        return <Loader />;
    }

    if (cacheData.error) {
        return <EmptyHandler isEmpty />;
    }

    const clearAllCachesClickHandler = (): void => {
        if (instanceId) {
            setClearAllCaches(StatelessRequest.loading());

            clearAllCachesData(instanceId)
                .then((value) => {
                    if (value.status === 200) {
                        setClearAllCaches(StatelessRequest.success());
                        message.success(t("Caches.cleared"));
                    } else {
                        setClearAllCaches(StatelessRequest.error(""));
                    }
                })
                .catch((error: AxiosError<IErrorResponse>) => {
                    setClearAllCaches(StatelessRequest.error(extractErrorCode(error?.response?.data)));
                });
        }
    };

    const requiredCacheManagers = cacheData.response!.cacheManagers;
    const effectiveCacheManagers = search ? filterCacheManagers(requiredCacheManagers, search) : requiredCacheManagers;

    return (
        <>
            <div className={styles.TopSection}>
                <PageSearch setSearch={setSearch} />
                <Button type="primary" onClick={clearAllCachesClickHandler} loading={clearAllCaches.loading}>
                    {t("Caches.clearAll")}
                </Button>
            </div>

            <EmptyHandler isEmpty={effectiveCacheManagers.length === 0}>
                {effectiveCacheManagers.map((cacheManager) => (
                    <CacheManagerSection key={cacheManager.name} cacheManager={cacheManager} />
                ))}
            </EmptyHandler>
        </>
    );
};

export default Caches;
