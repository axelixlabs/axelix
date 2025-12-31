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

import jakarta.annotation.Nullable;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

/**
 * Adapter to Spring's {@link CacheManager}.
 *
 * @since 23.06.2025
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public interface CacheManagerAdapter {

    /**
     * Clear the cache with the given name.
     *
     * @param cacheName the name of the cache to clear
     */
    boolean clear(String cacheName);

    /**
     * Clear all caches managed by this {@link CacheManager}.
     */
    boolean clearAll();

    /**
     * Clear the specific entry in the specific cache.
     *
     * @param cacheName the name of the cache to clear
     * @param key       the key to evict
     */
    boolean clear(String cacheName, Object key);

    /**
     * Enable all caches in this cache manager, allowing them to perform caching operations.
     */
    void enableCacheManager();

    /**
     * Disable all caches in this cache manager, preventing them from performing caching operations.
     * <p>
     * Please note, that this API disabled all the caches inside the given cache manager
     * that are only known by the time of this exact invocation. Some underlying {@link CacheManager}
     * implementations (such as {@link ConcurrentMapCacheManager} for instance) support the dynamic
     * addition of {@link org.springframework.cache.Cache caches}. The caches that are going to be added
     * dynamically later after the given invocation of this method will not be disabled.
     */
    void disableCacheManager();

    /**
     * Enable a specific cache by name.
     * This activates caching operations for the specified cache only.
     *
     * @param cacheName the name of the cache to enable
     */
    void enableCache(String cacheName);

    /**
     * Disable a specific cache by name.
     * This deactivates caching operations for the specified cache only.
     *
     * @param cacheName the name of the cache to disable
     */
    void disableCache(String cacheName);

    /**
     * Check if a specific cache is currently enabled.
     *
     * @param cacheName the name of the cache to check
     * @return {@code true} if the cache is enabled, {@code false} if it's disabled
     */
    boolean isCacheEnabled(String cacheName);

    /**
     * @param cacheName the name of the cache to check
     * @return the estimated number of cache hits, or {@code null} if the cache is not found.
     */
    @Nullable
    Long getHitsCount(String cacheName);

    /**
     * @param cacheName the name of the cache to check
     * @return the estimated number of cache misses, or {@code null} if the cache is not found.
     */
    @Nullable
    Long getMissesCount(String cacheName);

    /**
     * @param cacheName the name of the cache to check
     * @return the underlying native cache provider, or {@code null} if the cache is not found.
     */
    @Nullable
    Object getNativeCache(String cacheName);
}
