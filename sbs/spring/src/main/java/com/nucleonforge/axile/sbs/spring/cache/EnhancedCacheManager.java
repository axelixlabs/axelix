package com.nucleonforge.axile.sbs.spring.cache;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCache;

/**
 * CacheManager implementation that provides dynamic control over cache operations.
 * Allows enabling/disabling individual caches or the entire manager at runtime.
 *
 * @since 24.11.2025
 * @author Nikita Kirillov
 */
public class EnhancedCacheManager implements CacheManager {

    private final CacheManager delegate;

    private final Set<String> cacheNamesDisabled = ConcurrentHashMap.newKeySet();

    private volatile boolean enabled = true;

    public EnhancedCacheManager(CacheManager delegate) {
        this.delegate = delegate;
    }

    @Override
    @Nullable
    public Cache getCache(@NonNull String name) {
        if (!enabled) {
            return new NoOpCache(name);
        }

        if (cacheNamesDisabled.contains(name)) {
            return new NoOpCache(name);
        }

        Cache result = delegate.getCache(name);
        if (result != null) {
            return new SwitchableCache(result, name, this);
        }

        return null;
    }

    @Override
    @NonNull
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }

    public void enableCacheManager() {
        this.enabled = true;
    }

    public void disableCacheManager() {
        this.enabled = false;
    }

    public void enableCache(String cacheName) {
        cacheNamesDisabled.remove(cacheName);
    }

    public void disableCache(String cacheName) {
        cacheNamesDisabled.add(cacheName);
    }

    public void enableAllCache() {
        this.enabled = true;
        this.cacheNamesDisabled.clear();
    }

    public boolean isCacheDisabled(String cacheName) {
        return !enabled && cacheNamesDisabled.contains(cacheName);
    }
}
