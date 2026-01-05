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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * CacheManager implementation that provides dynamic control over cache operations.
 * <p>
 * Current implementation stores the map of {@link EnhancedCache EnhancedCache instances}.
 * All the operations, like clear, eviction by key, get by key etc. can be performed of the
 * {@link EnhancedCache}, rather than on the {@link #delegate}, since {@link EnhancedCache}
 * instance by design internally holds the reference to the exact same {@link Cache} object
 * on the heap, as the underlying {@link #delegate} does.
 *
 * @since 24.11.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public class EnhancedCacheManager implements CacheManager {

    private final String cacheManagerBeanName;
    private final CacheManager delegate;
    private final Map<String, EnhancedCache> caches = new ConcurrentHashMap<>();

    public EnhancedCacheManager(String cacheManagerBeanName, CacheManager delegate) {
        this.delegate = delegate;
        this.cacheManagerBeanName = cacheManagerBeanName;
    }

    public String getUnderlyingCacheManagerBeanName() {
        return cacheManagerBeanName;
    }

    public void clear(String cacheName) {
        Optional.ofNullable(this.getCache(cacheName)).ifPresent(Cache::invalidate);
    }

    public void clear(String cacheName, Object key) {
        Optional.ofNullable(this.getCache(cacheName)).ifPresent(cache -> cache.evictIfPresent(key));
    }

    public void clearAll() {
        caches.forEach((cacheManagerName, enhancedCache) -> enhancedCache.clear());
    }

    public Collection<EnhancedCache> getAll() {
        return caches.values();
    }

    @Override
    @Nullable
    public EnhancedCache getCache(@NonNull String name) {
        EnhancedCache enhancedCache = caches.computeIfAbsent(name, s -> {
            Cache cache = delegate.getCache(s);

            if (cache != null) {
                return new DefaultEnhancedCache(cache);
            } else {
                return NonExistentEnhancedCache.INSTANCE;
            }
        });

        if (enhancedCache instanceof NonExistentEnhancedCache) {
            return null;
        } else {
            return enhancedCache;
        }
    }

    @Override
    @NonNull
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }

    /**
     * Enable cache, if exists.
     *
     * @param cacheName cache name to enable.
     */
    public void enable(String cacheName) {
        Cache cache = this.getCache(cacheName);

        if (cache != null) {
            ((EnhancedCache) cache).enable();
        }
    }

    /**
     * Disable cache, if exists.
     *
     * @param cacheName cache name to enable.
     */
    public void disable(String cacheName) {
        Cache cache = this.getCache(cacheName);

        if (cache != null) {
            ((EnhancedCache) cache).disable();
        }
    }

    /**
     * Enable all caches.
     */
    public void enableAll() {
        this.caches.forEach((cacheManagerName, enhancedCache) -> {
            enhancedCache.enable();
        });
    }

    /**
     * Disable all caches.
     */
    public void disableAll() {
        this.caches.forEach((cacheManagerName, enhancedCache) -> {
            enhancedCache.disable();
        });
    }

    /**
     * Check if a specific cache is currently enabled.
     *
     * @param cacheName the name of the cache to check
     * @return {@code true} if the cache exists and is enabled, {@code false} otherwise
     */
    public boolean isEnabled(String cacheName) {
        Cache cache = this.getCache(cacheName);

        if (cache != null) {
            return ((EnhancedCache) cache).isEnabled();
        }

        return false;
    }

    public long getHitsCount(String cacheName) {
        EnhancedCache cache = caches.get(cacheName);
        return cache != null ? cache.getHitsCount() : 0;
    }

    public long getMissesCount(String cacheName) {
        EnhancedCache cache = caches.get(cacheName);
        return cache != null ? cache.getMissesCount() : 0;
    }

    public @Nullable Object getNativeCache(String cacheName) {
        EnhancedCache enhanced = caches.get(cacheName);
        return enhanced != null ? enhanced.getNativeCache() : null;
    }
}
