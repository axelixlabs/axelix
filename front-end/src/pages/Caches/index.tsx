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

export const Caches = () => {
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

    const requiredCacheManagers = cacheData.response!.cacheManagers;
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
