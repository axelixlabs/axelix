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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.cache.CacheManager;

import com.axelixlabs.axelix.common.api.caches.CachesFeed;
import com.axelixlabs.axelix.common.api.caches.SingleCache;

/**
 * Default implementation of {@link CacheOperationsDispatcher}.
 *
 * @since 26.06.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
// TODO:
//  We need to migrate to more recent versions of jspecify. Current version incorrectly
//  reports the nullability issue with type-use annotations on generics
@SuppressWarnings("NullAway")
public class DefaultCacheOperationsDispatcher implements CacheOperationsDispatcher {

    private final Map<String, EnhancedCacheManager> cacheManagers;
    private final CacheSizeProvider cacheSizeProvider;

    public DefaultCacheOperationsDispatcher(Map<String, CacheManager> managers, CacheSizeProvider cacheSizeProvider) {
        this.cacheManagers = onlyEnhancedCacheManagers(managers);
        this.cacheSizeProvider = cacheSizeProvider;
    }

    private static Map<String, EnhancedCacheManager> onlyEnhancedCacheManagers(Map<String, CacheManager> managers) {
        return managers.entrySet().stream()
                .filter(e -> e.getValue() instanceof EnhancedCacheManager)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> ((EnhancedCacheManager) e.getValue())));
    }

    @Override
    public SingleCache get(String cacheManagerName, String cacheName) {
        return get(cacheManagerName, cacheManager -> {
            EnhancedCache cache = cacheManager.getCache(cacheName);

            if (cache == null) {
                throw new CacheNotFoundException(cacheName, cacheManagerName);
            }

            return new SingleCache(
                    cache.getName(),
                    cache.getNativeCache().getClass().getName(),
                    cacheManager.getUnderlyingCacheManagerBeanName(),
                    toApiCacheLookups(cache.getHits()),
                    toApiCacheLookups(cache.getMisses()),
                    cacheSizeProvider.getEstimatedCacheSize(cache.getNativeCache()),
                    cache.isEnabled());
        });
    }

    @Override
    public CachesFeed getAll() {
        List<CachesFeed.CacheManager> feed = new ArrayList<>();

        this.cacheManagers.forEach((cacheManagerName, enhancedCacheManager) -> {
            feed.add(new CachesFeed.CacheManager(
                    cacheManagerName,
                    enhancedCacheManager.getAll().stream()
                            .map(enhancedCache -> new CachesFeed.Cache(
                                    enhancedCache.getName(),
                                    enhancedCache.getNativeCache().getClass().getName(),
                                    cacheSizeProvider.getEstimatedCacheSize(enhancedCache.getNativeCache()),
                                    enhancedCache.isEnabled()))
                            .collect(Collectors.toList())));
        });

        return new CachesFeed(feed);
    }

    @Override
    public void clear(String cacheManagerName, String cacheName) {
        execute(cacheManagerName, cacheManager -> cacheManager.clear(cacheName));
    }

    @Override
    public void clear(String cacheManagerName, String cacheName, Object key) {
        execute(cacheManagerName, cacheManager -> cacheManager.clear(cacheName, key));
    }

    @Override
    public void clear(String cacheManagerName) {
        execute(cacheManagerName, EnhancedCacheManager::clearAll);
    }

    @Override
    public void clearAll() throws CacheManagerNotFoundException {
        cacheManagers.forEach((cacheManagerName, cacheManager) -> cacheManager.clearAll());
    }

    @Override
    public void enableCacheManager(String cacheManagerName) {
        execute(cacheManagerName, EnhancedCacheManager::enableAll);
    }

    @Override
    public void disableCacheManager(String cacheManagerName) {
        execute(cacheManagerName, EnhancedCacheManager::disableAll);
    }

    @Override
    public void enableCache(String cacheManagerName, String cacheName) {
        execute(cacheManagerName, adapter -> adapter.enable(cacheName));
    }

    @Override
    public void disableCache(String cacheManagerName, String cacheName) {
        execute(cacheManagerName, adapter -> adapter.disable(cacheName));
    }

    public <T> T get(String cacheManagerName, Function<EnhancedCacheManager, @Nullable T> providerFunction) {
        EnhancedCacheManager enhancedCacheManager = findCacheManager(cacheManagerName);

        return providerFunction.apply(enhancedCacheManager);
    }

    public <T> void execute(String cacheManagerName, Consumer<EnhancedCacheManager> action) {
        EnhancedCacheManager enhancedCacheManager = findCacheManager(cacheManagerName);

        action.accept(enhancedCacheManager);
    }

    private EnhancedCacheManager findCacheManager(String cacheManagerName) {
        EnhancedCacheManager enhancedCacheManager = cacheManagers.get(cacheManagerName);

        if (enhancedCacheManager == null) {
            throw new CacheManagerNotFoundException(String.format("Cache manager '%s' is not found", cacheManagerName));
        }

        return enhancedCacheManager;
    }

    private static List<SingleCache.CacheLookup> toApiCacheLookups(List<CacheLookup> cache) {
        return cache.stream()
                .map(it -> new SingleCache.CacheLookup(it.timestamp().toEpochMilli()))
                .collect(Collectors.toList());
    }
}
