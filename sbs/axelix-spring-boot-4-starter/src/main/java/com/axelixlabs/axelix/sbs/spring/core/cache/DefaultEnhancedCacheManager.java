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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.ConcurrentReferenceHashMap;

import com.axelixlabs.axelix.sbs.spring.core.SlidingWindow;
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
    // Collect metrics only for caches present at startup to avoid metrics for dynamically created caches.
    private final Set<String> cacheNamesWithMetrics;

    // state is separated between two maps. The first map is the state, whose allocation remains under our control,
    // and we will not cause the OOM in case of any dynamic caches creation. The second map (lookup history) is the
    // one that is created dynamically and thus may lead to uncontrollable allocation. That is why we stick to
    // SoftReference here to avoid OOM in case of unexpected dynamic caches creation event.
    private final Map<String, Boolean> cacheStates = new ConcurrentHashMap<>();
    private final Map<String, SlidingWindow<@NonNull CacheLookup>> cacheLookupHistories =
            new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.SOFT);

    private final @Nullable AxelixMetricsPublisher metricsPublisher;

    public DefaultEnhancedCacheManager(
            String cacheManagerBeanName, CacheManager delegate, @Nullable AxelixMetricsPublisher metricsPublisher) {
        this.delegate = delegate;
        this.cacheManagerBeanName = cacheManagerBeanName;
        this.metricsPublisher = metricsPublisher;
        this.cacheNamesWithMetrics = Set.copyOf(delegate.getCacheNames());
        if (metricsPublisher != null) {
            cacheNamesWithMetrics.forEach(cacheName -> metricsPublisher.registerCacheStatusGauge(
                    cacheName, () -> cacheStates.getOrDefault(cacheName, true)));
        }
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

        return currentCacheNames.stream()
                .map(this::getCache)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Nullable
    public EnhancedCache getCache(@NonNull String name) {
        Cache cache = delegate.getCache(name);

        if (cache == null) {
            return null;
        }

        return new DefaultEnhancedCache(
                cache,
                cacheNamesWithMetrics.contains(name) ? metricsPublisher : null,
                () -> cacheStates.getOrDefault(name, true),
                this);
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

    @Override
    public SlidingWindow<CacheLookup> getLookupHistory(String cacheName) {
        return cacheLookupHistories.computeIfAbsent(cacheName, name -> new SlidingWindow<>(200));
    }
}
