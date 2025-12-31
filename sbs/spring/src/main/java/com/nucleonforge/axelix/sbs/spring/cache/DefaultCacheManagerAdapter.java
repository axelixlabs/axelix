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

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * Implementation of {@link CacheManagerAdapter} that delegates to Spring's {@link CacheManager}.
 *
 * @since 23.06.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public class DefaultCacheManagerAdapter implements CacheManagerAdapter {

    private final CacheManager cacheManager;

    public DefaultCacheManagerAdapter(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public boolean clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            return cache.invalidate();
        }
        return false;
    }

    @Override
    public boolean clearAll() {
        boolean anyCacheCleared = false;
        for (String cacheName : cacheManager.getCacheNames()) {
            anyCacheCleared |= clear(cacheName);
        }
        return anyCacheCleared;
    }

    @Override
    public boolean clear(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null && cache.get(key) != null) {
            return cache.evictIfPresent(key);
        }
        return false;
    }

    @Override
    public void enableCacheManager() {
        if (cacheManager instanceof EnhancedCacheManager enhancedCacheManager) {
            enhancedCacheManager.enableAllCaches();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void disableCacheManager() {
        if (cacheManager instanceof EnhancedCacheManager enhancedCacheManager) {
            enhancedCacheManager.disableAllCaches();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void enableCache(String cacheName) {
        if (cacheManager instanceof EnhancedCacheManager enhancedCacheManager) {
            Cache cache = enhancedCacheManager.getCache(cacheName);
            if (cache != null) {
                enhancedCacheManager.enableCache(cacheName);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void disableCache(String cacheName) {
        if (cacheManager instanceof EnhancedCacheManager enhancedCacheManager) {
            Cache cache = enhancedCacheManager.getCache(cacheName);
            if (cache != null) {
                enhancedCacheManager.disableCache(cacheName);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean isCacheEnabled(String cacheName) {
        if (cacheManager instanceof EnhancedCacheManager enhancedCacheManager) {
            return enhancedCacheManager.isCacheEnabled(cacheName);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Nullable
    public Long getHitsCount(String cacheName) {
        if (cacheManager instanceof EnhancedCacheManager enhancedCacheManager) {
            return enhancedCacheManager.getHitsCount(cacheName);
        }
        return null;
    }

    @Nullable
    public Long getMissesCount(String cacheName) {
        if (cacheManager instanceof EnhancedCacheManager enhancedCacheManager) {
            return enhancedCacheManager.getMissesCount(cacheName);
        }
        return null;
    }

    @Nullable
    public Object getNativeCache(String cacheName) {
        if (cacheManager instanceof EnhancedCacheManager enhancedCacheManager) {
            return enhancedCacheManager.getNativeCache(cacheName);
        }
        return null;
    }
}
