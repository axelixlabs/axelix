import { useEffect } from "react";
import { Button, message } from "antd";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { clearAllCachesThunk, getCachesThunk } from "store/thunks";
import { CacheManagerSection } from "./CacheManagerSection";
import { useAppDispatch, useAppSelector } from "hooks";
import { filterCacheManagers } from "store/slices";
import {EmptyHandler, Loader, PageSearch} from "components";

import styles from './styles.module.css'
import {IClearOperationType} from "../../models";

export const Caches = () => {
    const { t } = useTranslation()
    const dispatch = useAppDispatch()
    const { instanceId } = useParams()
    const [messageApi, contextHolder] = message.useMessage();

    const {
        cacheManagers,
        success,
        loading,
        clearOperationLoading,
        error,
        cacheManagersSearchText,
        filteredCacheManagers 
    } = useAppSelector((state) => state.caches)

    useEffect(() => {
        if (instanceId) {
            dispatch(getCachesThunk(instanceId))
        }
    }, [])

    useEffect(() => {
        if (success) {
            messageApi.success(t("cleared"))
        }
    }, [success])

    if (loading) {
        return <Loader />
    }

    // todo fix this in future
    if (error) {
        return error
    }

    const clearAllCachesClickHandler = (): void => {
        if (instanceId) {
            dispatch(clearAllCachesThunk(instanceId))
        }
    }

    const noDataAfterSearch = !!cacheManagersSearchText && !filteredCacheManagers.length;
    const cacheManagersData = filteredCacheManagers.length ? filteredCacheManagers : cacheManagers;

    return (
        <>
            {contextHolder}
            <div className={styles.TopSection}>
                <PageSearch
                    onChange={(e) => dispatch(filterCacheManagers(e))}/>

                <Button
                    type="primary"
                    onClick={clearAllCachesClickHandler}
                    loading={clearOperationLoading === IClearOperationType.CLEAR_ALL_CACHES}>
                    {t("clearAllCaches")}
                </Button>
            </div>

            <EmptyHandler isEmpty={noDataAfterSearch}>
                {cacheManagersData.map((cacheManager) => (
                    <CacheManagerSection key={cacheManager.name} cacheManager={cacheManager} />
                ))}
            </EmptyHandler>
        </>
    )
};

export default Caches;
