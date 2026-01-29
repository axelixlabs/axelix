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
