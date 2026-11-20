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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.axelixlabs.axelix.sbs.spring.core.SlidingWindow;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.ConcurrentReferenceHashMap;

import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;

/**
 * Default {@link EnhancedCacheManager} implementation that delegates the core
 * {@link CacheManager} contract to an underlying manager and shares management state by cache name across wrappers of the underlying caches.
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

    // state is separated between two maps. The first map is the state, whose allocation remains under our control,
    // and we will not cause the OOM in case of any dynamic caches creation. The second map (lookup history) is the
    // one that is created dynamically and thus may lead to uncontrollable allocation. That is why we stick to SoftReference
    // here to avoid OOM in case of unexpected dynamic caches creation event.
    private final Map<String, Boolean> cacheStates = new ConcurrentHashMap<>();
    private final Map<String, SlidingWindow<@NonNull CacheLookup>> cacheLookupHistories = new ConcurrentReferenceHashMap<>(
        16, ConcurrentReferenceHashMap.ReferenceType.SOFT);

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
        this.getCacheNames().forEach(this::clear);
    }

    @Override
    public Collection<EnhancedCache> getAll() {
        Collection<String> currentCacheNames = delegate.getCacheNames();

        return currentCacheNames.stream().map(this::getCache).filter(Objects::nonNull).toList();
    }

    @Override
    @Nullable
    public EnhancedCache getCache(@NonNull String name) {
        Cache cache = delegate.getCache(name);

        if (cache == null) {
            return null;
        }

        SlidingWindow<CacheLookup> lookupHistory = cacheLookupHistories.computeIfAbsent(
            name,
            cacheName -> new SlidingWindow<@NonNull CacheLookup>(200));

        return new DefaultEnhancedCache(cache, metricsPublisher, () -> cacheStates.get(name), lookupHistory);
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
    @Override
    public void enable(String cacheName) {
        cacheStates.put(cacheName, true);

        // TODO: do we need this metric at all?
        if (metricsPublisher != null) {
            metricsPublisher.registerCacheStatusGauge(cacheName, () -> cacheStates.get(cacheName));
        }
    }

    /**
     * Disable cache, if exists.
     *
     * @param cacheName cache name to enable.
     */
    @Override
    public void disable(String cacheName) {
        cacheStates.put(cacheName, false);
    }

    /**
     * Enable all caches.
     * <p>Best-effort with respect to caches created concurrently; late arrivals may be skipped, so callers should not rely on strong consistency.
     */
    @Override
    public void enableAll() {
        cacheStates.replaceAll((s, v) -> true);
    }

    /**
     * Disable all caches.
     * <p>Best-effort with respect to caches created concurrently; late arrivals may be skipped, so callers should not rely on strong consistency.
     */
    @Override
    public void disableAll() {
        cacheStates.replaceAll((s, v) -> false);
    }
}
