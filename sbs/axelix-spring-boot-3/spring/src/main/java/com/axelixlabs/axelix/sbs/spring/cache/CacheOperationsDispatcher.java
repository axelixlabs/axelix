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
package com.axelixlabs.axelix.sbs.spring.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.axelixlabs.axelix.common.api.caches.CachesFeed;
import com.axelixlabs.axelix.common.api.caches.SingleCache;
import com.axelixlabs.axelix.sbs.spring.cache.exception.CacheManagerNotFoundException;
import com.axelixlabs.axelix.sbs.spring.cache.exception.CacheNotFoundException;

/**
 * Dispatcher interface for executing cache operations across different {@link CacheManager CacheManagers}.
 * Serves as the entrypoint for all cache-related operations in Axelix.
 *
 * @since 26.06.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public interface CacheOperationsDispatcher {

    /**
     * Get the profile of the specific cache.
     *
     * @param cacheManagerName the name of the cache manager that contains the provided cache.
     * @param cacheName the target cache to inspect.
     * @return profile of the specific cache.
     *
     * @throws CacheManagerNotFoundException in case the requested CacheManager is not found.
     * @throws CacheNotFoundException in case the requested cache is not found.
     */
    SingleCache get(String cacheManagerName, String cacheName)
            throws CacheManagerNotFoundException, CacheNotFoundException;

    /**
     * Get feed of all caches along with their cache manager that are used inside the application.
     *
     * @return caches feed.
     */
    CachesFeed getAll();

    /**
     * Clears the entire cache with the given name from the specified {@code CacheManager}.
     *
     * @param cacheManagerName the name (bean name) of the {@code CacheManager}
     * @param cacheName        the name of the cache to clear
     *
     * @throws CacheManagerNotFoundException in case the requested CacheManager is not found.
     */
    void clear(String cacheManagerName, String cacheName) throws CacheManagerNotFoundException;

    /**
     * Evicts a specific key from the given cache managed by the specified {@code CacheManager}.
     *
     * @param cacheManagerName the name (bean name) of the {@code CacheManager}
     * @param cacheName        the name of the cache
     * @param key              the key to remove
     *
     * @throws CacheManagerNotFoundException in case the requested CacheManager is not found.
     */
    void clear(String cacheManagerName, String cacheName, Object key) throws CacheManagerNotFoundException;

    /**
     * Clears all caches managed by the specified {@code CacheManager}.
     *
     * @param cacheManagerName the name (bean name) of the {@code CacheManager}
     * @throws CacheManagerNotFoundException in case the requested CacheManager is not found.
     */
    void clear(String cacheManagerName) throws CacheManagerNotFoundException;

    /**
     * Clears all cache managers in the application {@code CacheManager}.
     */
    void clearAll() throws CacheManagerNotFoundException;

    /**
     * Enables all caches in the specified cache manager by name.
     * This activates caching operations for all caches in the given cache manager.
     *
     * @param cacheManagerName the name of the cache manager to enable.
     * @throws CacheManagerNotFoundException in case the requested CacheManager is not found.
     */
    void enableCacheManager(String cacheManagerName) throws CacheManagerNotFoundException;

    /**
     * Disables all caches in the specified cache manager by name.
     * This deactivates caching operations for all caches in the given cache manager.
     * <p>
     * Please note, that this API disabled all the caches inside the given cache manager
     * that are only known by the time of this exact invocation. Some underlying {@link CacheManager}
     * implementations (such as {@link ConcurrentMapCacheManager} for instance) support the dynamic
     * addition of {@link org.springframework.cache.Cache caches}. The caches that are going to be added
     * dynamically later after the given invocation of this method will not be disabled.
     *
     * @param cacheManagerName the name of the cache manager to disable.
     * @throws CacheManagerNotFoundException in case the requested CacheManager is not found.
     */
    void disableCacheManager(String cacheManagerName) throws CacheManagerNotFoundException;

    /**
     * Enables a specific cache within the specified cache manager.
     * This activates caching operations for the given cache only.
     *
     * @param cacheManagerName the name of the cache manager
     * @param cacheName the name of the cache to enable
     * @throws CacheManagerNotFoundException in case the requested CacheManager is not found.
     */
    void enableCache(String cacheManagerName, String cacheName) throws CacheManagerNotFoundException;

    /**
     * Disables a specific cache within the specified cache manager.
     * This deactivates caching operations for the given cache only.
     *
     * @param cacheManagerName the name of the cache manager
     * @param cacheName the name of the cache to disable
     * @throws CacheManagerNotFoundException in case the requested CacheManager is not found.
     */
    void disableCache(String cacheManagerName, String cacheName) throws CacheManagerNotFoundException;
}
