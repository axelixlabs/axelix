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
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

import { EmptyHandler, Loader } from "components";
import { fetchData } from "helpers";
import { type ICacheData, type IGetSingleCacheResponseBody, StatefulRequest } from "models";
import { getSingleCacheData } from "services";

import { CacheChart } from "./CacheChart";

interface IProps {
    /**
     * Single cache data
     */
    cache: ICacheData;

    /**
     * Name of the cache manager
     */
    cacheManagerName: string;
}

export const CacheAccordionBody = ({ cache, cacheManagerName }: IProps) => {
    const { instanceId } = useParams();

    const [singleCache, setSingleCache] = useState(StatefulRequest.loading<IGetSingleCacheResponseBody>());

    useEffect(() => {
        fetchData(setSingleCache, () =>
            getSingleCacheData({
                instanceId: instanceId!,
                cacheName: cache.name,
                cacheManagerName: cacheManagerName,
            }),
        );
    }, []);

    if (singleCache.loading) {
        return <Loader />;
    }

    if (singleCache.error) {
        return <EmptyHandler isEmpty />;
    }

    const singleCacheData = singleCache.response!;

    return <CacheChart singleCacheData={singleCacheData} />;
};
