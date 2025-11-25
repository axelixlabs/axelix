package com.nucleonforge.axile.sbs.spring.cache;

import java.util.concurrent.Callable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.cache.Cache;

/**
 * Cache implementation that can be dynamically enabled or disabled at runtime.
 *
 * @since 25.11.2025
 * @author Nikita Kirillov
 */
public class SwitchableCache implements Cache {
    private final Cache delegate;
    private final String cacheName;
    private final EnhancedCacheManager cacheManager;

    public SwitchableCache(Cache delegate, String cacheName, EnhancedCacheManager cacheManager) {
        this.delegate = delegate;
        this.cacheName = cacheName;
        this.cacheManager = cacheManager;
    }

    @Override
    @NonNull
    public String getName() {
        return delegate.getName();
    }

    @Override
    @NonNull
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    @Nullable
    public ValueWrapper get(@NonNull Object key) {
        if (cacheManager.isCacheDisabled(cacheName)) {
            return null;
        }
        return delegate.get(key);
    }

    @Override
    @Nullable
    public <T> T get(@NonNull Object key, @Nullable Class<T> type) {
        if (cacheManager.isCacheDisabled(cacheName)) {
            return null;
        }
        return delegate.get(key, type);
    }

    @Override
    @Nullable
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        if (cacheManager.isCacheDisabled(cacheName)) {
            return null;
        }
        return delegate.get(key, valueLoader);
    }

    @Override
    public void put(@NonNull Object key, @Nullable Object value) {
        if (cacheManager.isCacheDisabled(cacheName)) {
            return;
        }
        delegate.put(key, value);
    }

    @Override
    public void evict(@NonNull Object key) {
        if (cacheManager.isCacheDisabled(cacheName)) {
            return;
        }
        delegate.evict(key);
    }

    @Override
    public void clear() {
        if (cacheManager.isCacheDisabled(cacheName)) {
            return;
        }
        delegate.clear();
    }
}
