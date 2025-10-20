import { Button, message } from "antd";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { StatefulRequest, type ICachesData, StatelessRequest } from "models";
import { EmptyHandler, Loader, PageSearch } from "components";
import { CacheManagerSection } from "./CacheManagerSection";
import { fetchData, filterCacheManagers } from "helpers";

import styles from './styles.module.css'
import { clearAllCachesData, getCachesData } from "services";

export const Caches = () => {
    const { t } = useTranslation()
    const { instanceId } = useParams()
    const [messageApi, contextHolder] = message.useMessage();

    const [search, setSearch] = useState<string>("")
    const [clearAllCaches, setClearAllCaches] = useState(StatelessRequest.inactive())
    const [cacheData, setCacheData] = useState(StatefulRequest.loading<ICachesData>())

    useEffect(() => {
        fetchData(setCacheData, () => getCachesData(instanceId!));
    }, [])

    useEffect(() => {
      if (clearAllCaches.completedSuccessfully()) {
        messageApi.success(t("cleared"))
      }
      // TODO: handle failure on clear
    }, [clearAllCaches])

    if (cacheData.loading) {
        return <Loader />
    }

    // todo fix this in future
    if (cacheData.error) {
        return cacheData.error
    }

    const clearAllCachesClickHandler = (): void => {
        if (instanceId) {
          setClearAllCaches(StatelessRequest.loading())

          clearAllCachesData(instanceId)
              .then(value => {
                if (value.status === 200) {
                  setClearAllCaches(StatelessRequest.success())
                } else {
                  setClearAllCaches(StatelessRequest.error(""))
                }
              })
              .catch(() => {
                setClearAllCaches(StatelessRequest.error(""))
              })
        }
    }

    const requiredCacheManagers = cacheData.response!.cacheManagers
    const effectiveCacheManagers = search
      ? filterCacheManagers(requiredCacheManagers, search)
      : requiredCacheManagers

    return (
        <>
            {contextHolder}
            <div className={styles.TopSection}>
                <PageSearch onChange={(e) => setSearch(e)} />

                <Button
                    type="primary"
                    onClick={clearAllCachesClickHandler}
                    loading={clearAllCaches.loading}>
                    {t("clearAllCaches")}
                </Button>
            </div>

            <EmptyHandler isEmpty={effectiveCacheManagers.length === 0}>
                {effectiveCacheManagers.map((cacheManager) => (
                    <CacheManagerSection key={cacheManager.name} cacheManager={cacheManager} />
                ))}
            </EmptyHandler>
        </>
    )
};

export default Caches;
