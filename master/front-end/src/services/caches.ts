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
import { apiFetch } from "@/api";
import type { IClearCacheRequestData, IGetSingleCacheData, IUpdateCacheStatusRequestData } from "@/models";

export const getCachesData = (instanceId: string) => {
    return apiFetch.get(`caches/${instanceId}`);
};

export const getSingleCacheData = (data: IGetSingleCacheData) => {
    const { instanceId, cacheName, cacheManagerName } = data;

    return apiFetch.get(`caches/${instanceId}/cache/${cacheName}`, {
        params: {
            cacheManager: cacheManagerName,
        },
    });
};

export const clearAllCachesData = (instanceIds: string[]) => {
    return apiFetch.delete("caches/clear", {
        data: {
            instanceIds: instanceIds,
        },
    });
};

export const clearCacheData = (data: IClearCacheRequestData) => {
    return apiFetch.delete("caches/cache/clear", { data });
};

export const enableCache = (data: IUpdateCacheStatusRequestData) => {
    return apiFetch.post("caches/cache/enable", data);
};

export const disableCache = (data: IUpdateCacheStatusRequestData) => {
    return apiFetch.post("caches/cache/disable", data);
};
