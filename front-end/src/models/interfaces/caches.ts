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
     * Estimated amount of entries inside the cache. May be absent.
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

export interface IUpdateCacheStatusRequestData {
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

export interface IGetSingleCacheData {
    /**
     * Instance id of service
     */
    instanceId: string;

    /**
     * Name of the cache
     */
    cacheName: string;

    /**
     * Name of the cache manager
     */
    cacheManagerName: string;
}

interface ICacheLookup {
    /**
     * Timestamp of the given cache lookup.
     */
    timestamp: number;
}

export interface IGetSingleCacheResponseBody {
    /**
     * Name of the cache
     */
    name: string;

    /**
     * Target of the cache
     */
    target: string;

    /**
     * Name of the cache manager associated with the cache
     */
    cacheManager: string;

    /**
     * Recorded history of hits.
     */
    hits: ICacheLookup[];

    /**
     * Recorded history of misses.
     */
    misses: ICacheLookup[];

    /**
     * Estimated amount of entries inside the cache. May be absent.
     */
    estimatedEntrySize?: number;

    /**
     * When true, caching is active; when false, caching is inactive
     */
    enabled: boolean;
}

export interface ISingleCacheChartEntity {
    /**
     * Timestamp entry.
     */
    timestamp: number;

    /**
     * List of hit timestamps.
     */
    hits: number;

    /**
     * List of miss timestamps.
     */
    misses: number;
}
