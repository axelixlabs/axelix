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
package com.axelixlabs.axelix.sbs.spring.core.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;

/**
 * Default {@link EnhancedCacheManager} implementation that delegates the core
 * {@link CacheManager} contract to an underlying manager and maintains a map of
 * {@link EnhancedCache} instances on top of the underlying caches.
 *
 * @since 11.05.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Artemiy Degtyarev
 */
public class DefaultEnhancedCacheManager implements EnhancedCacheManager {

    private final String cacheManagerBeanName;
    private final CacheManager delegate;
    private final Map<String, EnhancedCache> caches = new ConcurrentHashMap<>();
    private final @Nullable AxelixMetricsPublisher metricsPublisher;

    public DefaultEnhancedCacheManager(
            String cacheManagerBeanName, CacheManager delegate, @Nullable AxelixMetricsPublisher metricsPublisher) {
        this.delegate = delegate;
        this.cacheManagerBeanName = cacheManagerBeanName;
        this.metricsPublisher = metricsPublisher;
    }

    @Override
    public String getUnderlyingCacheManagerBeanName() {
        return cacheManagerBeanName;
    }

    @Override
    public void clear(String cacheName) {
        Optional.ofNullable(this.getCache(cacheName)).ifPresent(Cache::clear);
    }

    @Override
    public void clear(String cacheName, Object key) {
        Optional.ofNullable(this.getCache(cacheName)).ifPresent(cache -> cache.evictIfPresent(key));
    }

    /** Best-effort with respect to caches created concurrently; late arrivals may be skipped, so callers should not rely on strong consistency. */
    @Override
    public void clearAll() {
        this.getCacheNames().forEach(this::getCache);

        caches.forEach((cacheManagerName, enhancedCache) -> enhancedCache.clear());
    }

    @Override
    public Collection<EnhancedCache> getAll() {
        return caches.values();
    }

    @Override
    @Nullable
    public EnhancedCache getCache(@NonNull String name) {
        return caches.computeIfAbsent(name, s -> {
            Cache cache = delegate.getCache(s);
            return cache != null ? new DefaultEnhancedCache(cache, metricsPublisher) : null;
        });
    }

    @Override

    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }

    /**
     * Enable cache, if exists.
     *
     * @param cacheName cache name to enable.
     */
    @Override
    public void enable(String cacheName) {
        EnhancedCache cache = this.getCache(cacheName);

        if (cache != null) {
            cache.enable();
        }
    }

    /**
     * Disable cache, if exists.
     *
     * @param cacheName cache name to enable.
     */
    @Override
    public void disable(String cacheName) {
        EnhancedCache cache = this.getCache(cacheName);

        if (cache != null) {
            cache.disable();
        }
    }

    /**
     * Enable all caches.
     * <p>Best-effort with respect to caches created concurrently; late arrivals may be skipped, so callers should not rely on strong consistency.
     */
    @Override
    public void enableAll() {
        this.getCacheNames().forEach(this::getCache);

        this.caches.forEach((cacheManagerName, enhancedCache) -> enhancedCache.enable());
    }

    /**
     * Disable all caches.
     * <p>Best-effort with respect to caches created concurrently; late arrivals may be skipped, so callers should not rely on strong consistency.
     */
    @Override
    public void disableAll() {
        this.getCacheNames().forEach(this::getCache);

        this.caches.forEach((cacheManagerName, enhancedCache) -> enhancedCache.disable());
    }

    /**
     * Check if a specific cache is currently enabled.
     *
     * @param cacheName the name of the cache to check
     * @return {@code true} if the cache exists and is enabled, {@code false} otherwise
     */
    @Override
    public boolean isEnabled(String cacheName) {
        EnhancedCache cache = this.getCache(cacheName);

        if (cache != null) {
            return cache.isEnabled();
        }

        return false;
    }
}
