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
import { SearchOutlined } from "@ant-design/icons";

import { App, Button, Input } from "antd";
import type { AxiosError } from "axios";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";

import { ActionInstancesSelector, EmptyHandler, Loader, NoRequiredAuthorityTooltip } from "@/components";
import { extractErrorCode, fetchData, filterCacheManagers } from "@/helpers";
import { useActionInstanceSelection, useAuthority, useConfirmableAction } from "@/hooks";
import {
    EAuthorities,
    type ICacheData,
    type ICachesResponseBody,
    type IErrorResponse,
    StatefulRequest,
    StatelessRequest,
} from "@/models";
import { clearAllCachesData, getCachesData } from "@/services";

import { CacheAccordionBody } from "./CacheAccordionBody";
import { CacheManagerSection } from "./CacheManagerSection";
import { SingleCacheHeader } from "./SingleCacheHeader";
import styles from "./styles.module.css";

const Caches = () => {
    const cachesClearAccess = useAuthority(EAuthorities.CACHES_CLEAR);

    const { t } = useTranslation();
    const { instanceId } = useParams();
    const instanceSelection = useActionInstanceSelection(instanceId!);
    const { message } = App.useApp();
    const confirmAction = useConfirmableAction();

    const [search, setSearch] = useState("");
    const [selection, setSelection] = useState<ICacheData>();

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
        confirmAction({
            title: t("Caches.clearAllCachesTitle"),
            content: t("Caches.clearAllCachesDescription"),
            onOk() {
                if (instanceId) {
                    setClearAllCaches(StatelessRequest.loading());

                    clearAllCachesData(instanceSelection.selectedInstanceIds)
                        .then(() => {
                            setClearAllCaches(StatelessRequest.success());
                            message.success(t("Caches.cleared"));
                        })
                        .catch((error: AxiosError<IErrorResponse>) => {
                            setClearAllCaches(StatelessRequest.error(extractErrorCode(error?.response?.data)));
                        });
                }
            },
        });
    };

    const query = search.trim().toLowerCase();
    const managers = filterCacheManagers(cacheData.response!.cacheManagers, query);
    const matchingManager = managers.find((manager) => manager.caches.some((cache) => cache === selection));
    const selectedManager = matchingManager ?? managers[0];
    const selectedCache = matchingManager ? selection : selectedManager?.caches[0];

    return (
        <>
            <div className={styles.TopSection}>
                <ActionInstancesSelector key={instanceId} {...instanceSelection} />
                <div className={styles.Actions}>
                    <NoRequiredAuthorityTooltip disabled={!cachesClearAccess}>
                        <Button
                            type="primary"
                            onClick={clearAllCachesClickHandler}
                            loading={clearAllCaches.loading}
                            disabled={!cachesClearAccess}
                        >
                            {t("Caches.clearAll")}
                        </Button>
                    </NoRequiredAuthorityTooltip>
                </div>
            </div>

            <div className={styles.Layout}>
                <section className={styles.List} aria-labelledby="caches-list-title">
                    <div className={styles.ListHeader}>
                        <h2 id="caches-list-title" className={styles.ListTitle}>
                            {t("Sider.caches")}
                        </h2>
                        <Input
                            className={styles.Search}
                            prefix={<SearchOutlined />}
                            allowClear
                            value={search}
                            onChange={(event) => setSearch(event.target.value)}
                            aria-label={t("search")}
                            placeholder={t("search")}
                        />
                    </div>
                    <div className={`ThinScrollbar ${styles.Groups}`} key={query ? "search" : "browse"}>
                        <EmptyHandler isEmpty={managers.length === 0}>
                            {managers.map((manager) => (
                                <CacheManagerSection
                                    key={manager.name}
                                    cacheManager={manager}
                                    selectedCacheName={manager === selectedManager ? selectedCache?.name : undefined}
                                    onSelect={setSelection}
                                />
                            ))}
                        </EmptyHandler>
                    </div>
                </section>

                {selectedManager && selectedCache && (
                    <section
                        className={styles.Detail}
                        key={JSON.stringify([instanceId, selectedManager.name, selectedCache.name])}
                    >
                        <SingleCacheHeader
                            cacheManagerName={selectedManager.name}
                            cache={selectedCache}
                            instanceIds={instanceSelection.selectedInstanceIds}
                        />
                        {selectedCache.containsStats && (
                            <CacheAccordionBody cache={selectedCache} cacheManagerName={selectedManager.name} />
                        )}
                    </section>
                )}
            </div>
        </>
    );
};

export default Caches;
