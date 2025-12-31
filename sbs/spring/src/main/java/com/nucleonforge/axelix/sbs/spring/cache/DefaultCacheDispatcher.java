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

import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.cache.CacheManager;

/**
 * Default implementation of {@link CacheDispatcher}.
 *
 * @since 26.06.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public class DefaultCacheDispatcher implements CacheDispatcher {

    private final Map<String, CacheManagerAdapter> adapters;

    private final CacheSizeProvider cacheSizeProvider;

    public DefaultCacheDispatcher(Map<String, CacheManager> managers, CacheSizeProvider cacheSizeProvider) {
        this.adapters = managers.entrySet().stream()
                .collect(
                        Collectors.toMap(Map.Entry::getKey, entry -> new DefaultCacheManagerAdapter(entry.getValue())));
        this.cacheSizeProvider = cacheSizeProvider;
    }

    @Override
    public boolean clear(String cacheManagerName, String cacheName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        return adapter != null && adapter.clear(cacheName);
    }

    @Override
    public boolean clear(String cacheManagerName, String cacheName, Object key) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        return adapter != null && adapter.clear(cacheName, key);
    }

    @Override
    public boolean clearAll(String cacheManagerName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        return adapter != null && adapter.clearAll();
    }

    @Override
    public void enableCacheManager(String cacheManagerName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        if (adapter != null) {
            adapter.enableCacheManager();
        } else {
            throw new CacheManagerAdapterNotFoundException(String.format(
                    "Adapter for cache manager '%s' not found. Cannot enable cache manager.", cacheManagerName));
        }
    }

    @Override
    public void disableCacheManager(String cacheManagerName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        if (adapter != null) {
            adapter.disableCacheManager();
        } else {
            throw new CacheManagerAdapterNotFoundException(String.format(
                    "Adapter for cache manager '%s' not found. Cannot disable cache manager.", cacheManagerName));
        }
    }

    @Override
    public void enableCache(String cacheManagerName, String cacheName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        if (adapter != null) {
            adapter.enableCache(cacheName);
        } else {
            throw new CacheManagerAdapterNotFoundException(String.format(
                    "Adapter for cache manager '%s' not found. Cannot enable cache '%s'.",
                    cacheManagerName, cacheName));
        }
    }

    @Override
    public void disableCache(String cacheManagerName, String cacheName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        if (adapter != null) {
            adapter.disableCache(cacheName);
        } else {
            throw new CacheManagerAdapterNotFoundException(String.format(
                    "Adapter for cache manager '%s' not found. Cannot disable cache '%s'.",
                    cacheManagerName, cacheName));
        }
    }

    @Override
    public boolean isCacheEnabled(String cacheManagerName, String cacheName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        if (adapter != null) {
            return adapter.isCacheEnabled(cacheName);
        } else {
            throw new CacheManagerAdapterNotFoundException(String.format(
                    "Adapter for cache manager '%s' not found. Cannot check enabled status for cache '%s'.",
                    cacheManagerName, cacheName));
        }
    }

    @Override
    @Nullable
    public Long getHitsCount(String cacheManagerName, String cacheName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        return adapter != null ? adapter.getHitsCount(cacheName) : null;
    }

    @Override
    @Nullable
    public Long getMissesCount(String cacheManagerName, String cacheName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        return adapter != null ? adapter.getMissesCount(cacheName) : null;
    }

    @Override
    @Nullable
    public Long getEstimatedCacheSize(String cacheManagerName, String cacheName) {
        CacheManagerAdapter adapter = adapters.get(cacheManagerName);
        return adapter != null ? cacheSizeProvider.getEstimatedCacheSize(adapter.getNativeCache(cacheName)) : null;
    }
}
