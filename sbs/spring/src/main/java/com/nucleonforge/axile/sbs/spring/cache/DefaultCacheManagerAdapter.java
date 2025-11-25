package com.nucleonforge.axile.sbs.spring.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * Implementation of {@link CacheManagerAdapter} that delegates to Spring's {@link CacheManager}.
 *
 * @since 23.06.2025
 * @author Nikita Kirillov
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

    public void enableCacheManager() {
        if (cacheManager instanceof EnhancedCacheManager enhancedCacheManager) {
            enhancedCacheManager.enableCacheManager();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void disableCacheManager() {
        if (cacheManager instanceof EnhancedCacheManager enhancedCacheManager) {
            enhancedCacheManager.disableCacheManager();
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

    @Override
    public void enableAllCache() {
        if (cacheManager instanceof EnhancedCacheManager enhancedCacheManager) {
            enhancedCacheManager.enableAllCache();
        }
    }
}
