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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.cache.CacheManager;

import com.nucleonforge.axelix.common.api.caches.CachesFeed;
import com.nucleonforge.axelix.common.api.caches.SingleCache;
import com.nucleonforge.axelix.sbs.spring.cache.exception.CacheManagerNotFoundException;
import com.nucleonforge.axelix.sbs.spring.cache.exception.CacheNotFoundException;

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
                    cache.getHitsCount(),
                    cache.getMissesCount(),
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
                                    enhancedCache.getHitsCount(),
                                    enhancedCache.getMissesCount(),
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
}
