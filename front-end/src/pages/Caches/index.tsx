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
import { Button, message } from "antd";
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
    const [messageApi, contextHolder] = message.useMessage();

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
                        messageApi.success(t("Caches.cleared"));
                    } else {
                        setClearAllCaches(StatelessRequest.error(""));
                    }
                })
                .catch((error: AxiosError<IErrorResponse>) => {
                    setClearAllCaches(StatelessRequest.error(extractErrorCode(error?.response?.data)));
                });
        }
    };

    const requiredCacheManagers = cacheData.response!.cacheManager;
    const effectiveCacheManagers = search ? filterCacheManagers(requiredCacheManagers, search) : requiredCacheManagers;

    return (
        <>
            {contextHolder}
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
