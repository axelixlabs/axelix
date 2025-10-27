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
