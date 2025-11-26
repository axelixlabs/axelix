package com.nucleonforge.axile.sbs.spring.cache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * CacheManager implementation that provides dynamic control over cache operations.
 * Allows enabling/disabling individual caches or the entire manager at runtime.
 *
 * @since 24.11.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public final class EnhancedCacheManager implements CacheManager {

    private final CacheManager delegate;
    private final Map<String, EnhancedCache> caches = new ConcurrentHashMap<>();

    public EnhancedCacheManager(CacheManager delegate) {
        this.delegate = delegate;
    }

    @Override
    @Nullable
    public Cache getCache(@NonNull String name) {
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
    public void enableCache(String cacheName) {
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
    public void disableCache(String cacheName) {
        Cache cache = this.getCache(cacheName);

        if (cache != null) {
            ((EnhancedCache) cache).disable();
        }
    }

    /**
     * Enable all caches managed by this cache manager.
     */
    public void enableAllCaches() {
        for (String cacheName : getCacheNames()) {
            enableCache(cacheName);
        }
    }

    /**
     * Disable all caches managed by this cache manager.
     */
    public void disableAllCaches() {
        for (String cacheName : getCacheNames()) {
            disableCache(cacheName);
        }
    }

    /**
     * Check if the specified cache is currently enabled.
     *
     * @param cacheName the name of the cache to check.
     * @return {@code true} if the cache exists and is enabled, {@code false} otherwise.
     */
    public boolean isEnabled(String cacheName) {
        Cache cache = getCache(cacheName);

        if (cache != null) {
            return ((EnhancedCache) cache).isEnabled();
        } else {
            return false;
        }
    }
}
