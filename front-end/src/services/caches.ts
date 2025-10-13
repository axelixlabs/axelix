import apiFetch from "api/apiFetch";
import type { IClearCacheData } from "models";

export const getCachesData = (instanceId: string) => {
    return apiFetch.get(`caches/${instanceId}`);
};

export const clearAllCachesData = (instanceId: string) => {
    return apiFetch.delete(`caches/${instanceId}`);
};

export const clearCacheData = (instanceId: string, data: IClearCacheData) => {
    return apiFetch.delete(`caches/${instanceId}/cache/${data.cacheName}`, {
        params: {
            cacheManager : data.cacheManager
        },
    });
};

