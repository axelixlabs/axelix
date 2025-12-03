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
            <div className={`RowChunk ${styles.ContentRowChunkWrapper}`}>
                <div className={styles.ContentWrapper}>
                    <span>{t("Caches.name")}: </span>
                    <span className={styles.CacheName}>{cache.name}</span>
                    <div className={styles.TargetWrapper}>
                        {/* TODO: This part we need to be fix after tooltip PR merge */}
                        <div className={styles.Target}>{t("Caches.target")}:</div>
                        <TooltipWithCopy text={cache.target} />
                    </div>
                </div>
            </div>
            <div className={`RowChunk ${styles.RowChunk}`}>
                <Button
                    icon={<ReloadOutlined />}
                    type="primary"
                    loading={clearSingleCache.loading}
                    onClick={clearCacheClickHandler}
                />
            </div>
            <div className={`RowChunk ${styles.RowChunk}`}>
                <CacheStatusSwitch cacheManagerName={cacheManagerName} cache={cache} />
            </div>
        </div>
    );
};
