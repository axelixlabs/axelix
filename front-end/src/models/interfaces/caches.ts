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
export interface ICacheData {
    /**
     * Name of the cache
     */
    name: string;
    /**
     * Target of the cache
     */
    target: string;
    /**
     * When true, caching is active; when false, caching is inactive
     */
    enabled: boolean;
    /**
     * Number of cache hits
     */
    hitsCount: number;
    /**
     * Number of cache misses
     */
    missesCount: number;
    /**
     * Estimated size of a single cache entry
     */
    estimatedEntrySize?: number;
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
    cacheManager: ICachesManager[];
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
