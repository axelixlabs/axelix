import { Button, message } from 'antd';
import { type MouseEvent, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ReloadOutlined } from "@ant-design/icons";

import { TooltipWithCopy } from 'components';
import { type ICacheData, StatelessRequest } from 'models';

import styles from './styles.module.css'
import { clearCacheData } from "services";

interface IProps {
    /**
     * Name of the cache manager
     */
    cacheManagerName: string;
    /**
     * Single cache data
     */
    cache: ICacheData
}

export const CacheCollapseHeader = ({ cacheManagerName, cache }: IProps) => {
    const { instanceId } = useParams()
    const { t } = useTranslation()

    const [clearSingleCache, setClearSingleCache] = useState(StatelessRequest.inactive())

    const clearCacheClickHandler = (e: MouseEvent<HTMLElement>): void => {
        e.stopPropagation();
        setClearSingleCache(StatelessRequest.loading())
        clearCacheData(instanceId!, {
            cacheName: cache.name,
            cacheManager: cacheManagerName
        })
            .then(() => {
                setClearSingleCache(StatelessRequest.success())
                message.success(t("cleared"))
            })
            .catch(() => setClearSingleCache(StatelessRequest.error("")))
    }

    return (
        <div className={styles.CollapseHeader}>
            <div>
                <span>{t("cacheName")}: </span>
                <span className={styles.CacheName}>{cache.name}</span>
                <div className={styles.Target}>
                    {t("cacheTarget")}: <TooltipWithCopy text={cache.target} />
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
    )
};