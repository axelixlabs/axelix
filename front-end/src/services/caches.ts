/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import apiFetch from "api/apiFetch";
import type { IClearCacheRequestData } from "models";

export const getCachesData = (instanceId: string) => {
    return apiFetch.get(`caches/${instanceId}`);
};

export const clearAllCachesData = (instanceId: string) => {
    return apiFetch.delete(`caches/${instanceId}`);
};

export const clearCacheData = (data: IClearCacheRequestData) => {
    const { instanceId, cacheName, cacheManager } = data;

    return apiFetch.delete(`caches/${instanceId}/cache/${cacheName}`, {
        params: {
            cacheManager: cacheManager,
        },
    });
};

interface IUpdateCacheStatusRequestData {
    /**
     * Instance id of service
     */
    instanceId: string;

    /**
     * Name of the cache manager
     */
    cacheManagerName: string;

    /**
     * Name of the cache
     */
    cacheName: string;
}

export const enableCache = (data: IUpdateCacheStatusRequestData) => {
    const { instanceId, cacheManagerName, cacheName } = data;

    return apiFetch.post(`caches/${instanceId}/${cacheManagerName}/${cacheName}/enable`);
};

export const disableCache = (data: IUpdateCacheStatusRequestData) => {
    const { instanceId, cacheManagerName, cacheName } = data;

    return apiFetch.post(`caches/${instanceId}/${cacheManagerName}/${cacheName}/disable`);
};
