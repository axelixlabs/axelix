import { Button, message } from "antd";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { clearAllCachesThunk, getCachesThunk } from "store/thunks";
import { IClearOperationType, type ICachesManager } from "models";
import { EmptyHandler, Loader, PageSearch } from "components";
import { CacheManagerSection } from "./CacheManagerSection";
import { useAppDispatch, useAppSelector } from "hooks";
import { filterCacheManagers } from "helpers";

import styles from './styles.module.css'

export const Caches = () => {
    const { t } = useTranslation()
    const dispatch = useAppDispatch()
    const { instanceId } = useParams()
    const [messageApi, contextHolder] = message.useMessage();

    const [isSearched, setIsSearched] = useState<boolean>(false)
    const [filteredCacheManagers, setFilteredCacheManagers] = useState<ICachesManager[]>([])

    const {
        cacheManagers,
        success,
        loading,
        clearOperationLoading,
        error
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

    const noSearchResults = isSearched && !filteredCacheManagers.length;
    const cacheManagersData = isSearched ? filteredCacheManagers : cacheManagers;

    const handleSearchChange = (search: string): void => {
        const isSearching = Boolean(search);
        setIsSearched(isSearching);

        if (!isSearching) {
            setFilteredCacheManagers([]);
            return;
        }

        setFilteredCacheManagers(filterCacheManagers(cacheManagers, search));
    };

    return (
        <>
            {contextHolder}
            <div className={styles.TopSection}>
                <PageSearch onChange={handleSearchChange} />

                <Button
                    type="primary"
                    onClick={clearAllCachesClickHandler}
                    loading={clearOperationLoading === IClearOperationType.CLEAR_ALL_CACHES}>
                    {t("clearAllCaches")}
                </Button>
            </div>

            <EmptyHandler isEmpty={noSearchResults}>
                {cacheManagersData.map((cacheManager) => (
                    <CacheManagerSection key={cacheManager.name} cacheManager={cacheManager} />
                ))}
            </EmptyHandler>
        </>
    )
};

export default Caches;
