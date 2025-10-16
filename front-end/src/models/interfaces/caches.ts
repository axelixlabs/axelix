import type { ICommonSliceState } from "./globals"

export interface ICacheData {
    /**
     * Name of the cache
     */
    name: string;
    /**
     * Target of the cache
     */
    target: string;
}

export interface ICachesManager {
    /**
     * Name of the cache manager
     */
    name: string;
    /**
     * List of caches associated with the cache manager
     */
    caches: ICacheData[];
}

export interface ICachesData {
    /**
     * List of cache managers
     */
    cacheManagers: ICachesManager[];
}

export enum IClearOperationType {
    CLEAR_SINGLE_CACHE = 0,
    CLEAR_ALL_CACHES = 1
}

export interface ICachesSliceState extends ICommonSliceState, ICachesData {
    /**
     * Success status for cache clear operations
     */
    success: boolean,

    /**
     * Optional clear cache operation that is currently in progress
     */
    clearOperationLoading: IClearOperationType | null,
}

export interface IClearCacheData {
    /**
     * Name of the cache
     */
    cacheName: string;
    /**
     * Name of the cache manager associated with the cache
     */
    cacheManager: string
}

export interface IClearCachePayload {
    /**
     * Instance ID of the service
     */
    instanceId: string;
    /**
     * Data containing cache name and cache manager
     */
    data: IClearCacheData;
}
