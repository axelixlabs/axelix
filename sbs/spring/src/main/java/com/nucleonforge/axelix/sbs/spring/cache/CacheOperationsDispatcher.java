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
package com.nucleonforge.axelix.sbs.spring.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.nucleonforge.axelix.common.api.caches.CachesFeed;
import com.nucleonforge.axelix.common.api.caches.SingleCache;
import com.nucleonforge.axelix.sbs.spring.cache.exception.CacheManagerNotFoundException;
import com.nucleonforge.axelix.sbs.spring.cache.exception.CacheNotFoundException;

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
