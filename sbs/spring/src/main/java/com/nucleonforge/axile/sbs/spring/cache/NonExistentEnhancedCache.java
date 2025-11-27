package com.nucleonforge.axile.sbs.spring.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The {@link EnhancedCache} for caches that do not actually exist i.e. handling
 * cases when {@link org.springframework.cache.CacheManager#getCache(String)} returned {@code null}.
 * <p>
 * This class mainly exists to cope the {@code null} values limitation of {@link ConcurrentHashMap}.
 *
 * @author Mikhail Polivakha
 */
class NonExistentEnhancedCache implements EnhancedCache {

    public static final NonExistentEnhancedCache INSTANCE = new NonExistentEnhancedCache();

    private NonExistentEnhancedCache() {}

    @Override
    @NonNull
    public String getName() {
        return "";
    }

    @Override
    @NonNull
    public Object getNativeCache() {
        return this;
    }

    @Nullable
    @Override
    public ValueWrapper get(Object key) {
        return null;
    }

    @Nullable
    @Override
    public <T> T get(Object key, @Nullable Class<T> type) {
        return null;
    }

    @Nullable
    @Override
    public <T> T get(Object key, @NonNull Callable<T> valueLoader) {
        return null;
    }

    @Override
    public void put(Object key, @Nullable Object value) {}

    @Override
    public void evict(Object key) {}

    @Override
    public void clear() {}

    @Override
    public void disable() {}

    @Override
    public void enable() {}

    @Override
    public boolean isEnabled() {
        return false;
    }
}
