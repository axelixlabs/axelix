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
