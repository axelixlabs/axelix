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

export interface ICachesResponseBody {
    /**
     * List of cache managers
     */
    cacheManagers: ICachesManager[];
}

export interface IClearCacheRequestData {
    /**
     * Instance ID of the service
     */
    instanceId: string;
    /**
     * Name of the cache
     */
    cacheName: string;
    /**
     * Name of the cache manager associated with the cache
     */
    cacheManager: string;
}
