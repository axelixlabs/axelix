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

import org.jspecify.annotations.Nullable;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

/**
 * Dispatcher interface for executing cache operations (such as evicting entries or clearing caches)
 * and for retrieving information across different CacheManager instances.
 *
 * @since 26.06.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public interface CacheDispatcher {

    /**
     * Clears the entire cache with the given name from the specified {@code CacheManager}.
     *
     * @param cacheManagerName the name (bean name) of the {@code CacheManager}
     * @param cacheName        the name of the cache to clear
     * @return {@code true} if the cache was found and cleared; {@code false} if the manager or cache was not found
     */
    boolean clear(String cacheManagerName, String cacheName);

    /**
     * Evicts a specific key from the given cache managed by the specified {@code CacheManager}.
     *
     * @param cacheManagerName the name (bean name) of the {@code CacheManager}
     * @param cacheName        the name of the cache
     * @param key              the key to remove
     * @return {@code true} if the key existed in the cache and was removed;
     * {@code false} if the manager, cache, or key was not found
     */
    boolean clear(String cacheManagerName, String cacheName, Object key);

    /**
     * Clears all caches managed by the specified {@code CacheManager}.
     *
     * @param cacheManagerName the name (bean name) of the {@code CacheManager}
     * @return {@code true} if at least one cache was found and cleared;
     * {@code false} if the manager was not found or no caches could be cleared
     */
    boolean clearAll(String cacheManagerName);

    /**
     * Enables all caches in the specified cache manager by name.
     * This activates caching operations for all caches in the given cache manager.
     *
     * @param managerName the name of the cache manager to enable
     */
    void enableCacheManager(String managerName);

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
     * @param managerName the name of the cache manager to disable
     */
    void disableCacheManager(String managerName);

    /**
     * Enables a specific cache within the specified cache manager.
     * This activates caching operations for the given cache only.
     *
     * @param managerName the name of the cache manager
     * @param cacheName the name of the cache to enable
     */
    void enableCache(String managerName, String cacheName);

    /**
     * Disables a specific cache within the specified cache manager.
     * This deactivates caching operations for the given cache only.
     *
     * @param managerName the name of the cache manager
     * @param cacheName the name of the cache to disable
     */
    void disableCache(String managerName, String cacheName);

    /**
     * Checks whether a specific cache is currently enabled.
     *
     * @param managerName the name of the cache manager
     * @param cacheName the name of the cache to check
     * @return {@code true} if the cache is enabled, {@code false} if it's disabled
     */
    boolean isCacheEnabled(String managerName, String cacheName);

    /**
     * @param managerName the name of the cache manager
     * @param cacheName the name of the cache to check
     * @return the estimated number of cache hits, or {@code null} if the cache manager is not found.
     */
    @Nullable
    Long getHitsCount(String managerName, String cacheName);

    /**
     * @param managerName the name of the cache manager
     * @param cacheName the name of the cache to check
     * @return the estimated number of cache misses, or {@code null} if the cache manager is not found.
     */
    @Nullable
    Long getMissesCount(String managerName, String cacheName);

    /**
     * @param managerName the name of the cache manager
     * @param cacheName the name of the cache to check
     * @return the estimated cache size, or {@code null} if the value cannot be determined,
     *         or if the provided cache is not supported, or if the cache manager is not found.
     */
    @Nullable
    Long getEstimatedCacheSize(String managerName, String cacheName);
}
