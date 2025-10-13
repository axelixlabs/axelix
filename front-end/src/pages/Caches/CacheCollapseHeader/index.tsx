import { Button } from 'antd';
import type { MouseEvent } from 'react';
import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ReloadOutlined } from "@ant-design/icons";

import { useAppDispatch, useAppSelector } from 'hooks';
import { clearCacheThunk } from 'store/thunks';
import { TooltipWithCopy } from 'components';
import {type ICacheData, IClearOperationType} from 'models';

import styles from './styles.module.css'

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
    const dispatch = useAppDispatch()
    const { instanceId } = useParams()
    const { t } = useTranslation()

    const { clearOperationLoading } = useAppSelector((state) => state.caches)

    const clearCacheClickHandler = (e: MouseEvent<HTMLElement>): void => {
        e.stopPropagation();
        if (instanceId) {
            dispatch(clearCacheThunk({
                instanceId,
                data: {
                    cacheName: cache.name,
                    cacheManager: cacheManagerName
                }
            }))
        }
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
                loading={clearOperationLoading === IClearOperationType.CLEAR_SINGLE_CACHE}
                className={styles.ClearCacheButton}
                onClick={clearCacheClickHandler}
            />
        </div>
    )
};