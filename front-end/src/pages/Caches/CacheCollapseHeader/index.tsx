import { ReloadOutlined } from "@ant-design/icons";

import { Button, message } from "antd";
import { type MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";

import { TooltipWithCopy } from "components";
import { type ICacheData, StatelessRequest } from "models";
import { clearCacheData } from "services";

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

export const CacheCollapseHeader = ({ cacheManagerName, cache }: IProps) => {
    const { instanceId } = useParams();
    const { t } = useTranslation();

    const [clearSingleCache, setClearSingleCache] = useState(StatelessRequest.inactive());

    const clearCacheClickHandler = (e: MouseEvent<HTMLElement>): void => {
        e.stopPropagation();
        setClearSingleCache(StatelessRequest.loading());
        clearCacheData(instanceId!, {
            cacheName: cache.name,
            cacheManager: cacheManagerName,
        })
            .then(() => {
                setClearSingleCache(StatelessRequest.success());
                message.success(t("Caches.cleared"))
            })
            .catch(() => setClearSingleCache(StatelessRequest.error("")));
    };

    return (
        <div className={styles.CollapseHeader}>
            <div>
                <span>{t("Caches.cacheName")}: </span>
                <span className={styles.CacheName}>{cache.name}</span>
                <div className={styles.Target}>
                    {t("Caches.cacheTarget")}: <TooltipWithCopy text={cache.target} />
                </div>
            </div>
            <Button
                icon={<ReloadOutlined />}
                type="primary"
                loading={clearSingleCache.loading}
                className={styles.ClearCacheButton}
                onClick={clearCacheClickHandler}
            />
        </div>
    );
};
