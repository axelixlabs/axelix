package com.nucleonforge.axile.sbs.spring.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.cache.Cache;

/**
 * Cache implementation that can be dynamically enabled or disabled at runtime.
 *
 * @since 25.11.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public class DefaultEnhancedCache implements EnhancedCache {

    private final Cache delegate;
    private final AtomicBoolean enabled;

    public DefaultEnhancedCache(@NonNull Cache delegate) {
        this.delegate = delegate;
        this.enabled = new AtomicBoolean(true);
    }

    @Override
    public void disable() {
        this.enabled.compareAndSet(true, false);
    }

    @Override
    public void enable() {
        this.enabled.compareAndSet(false, true);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled.get();
    }

    @Override
    @NonNull
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    @Nullable
    public ValueWrapper get(@NonNull Object key) {
        return getIfEnabledOrElseNull(() -> delegate.get(key));
    }

    @Override
    @Nullable
    public <T> T get(@NonNull Object key, @Nullable Class<T> type) {
        return getIfEnabledOrElseNull(() -> delegate.get(key, type));
    }

    @Override
    @Nullable
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        return getIfEnabledOrElseNull(() -> delegate.get(key, valueLoader));
    }

    @Override
    public void put(@NonNull Object key, @Nullable Object value) {
        executeIfEnabled(() -> delegate.put(key, value));
    }

    @Override
    public void evict(@NonNull Object key) {
        executeIfEnabled(() -> delegate.evict(key));
    }

    @Override
    public void clear() {
        executeIfEnabled(delegate::clear);
    }

    @Nullable
    private <T> T getIfEnabledOrElseNull(Supplier<T> supplier) {
        return enabled.get() ? supplier.get() : null;
    }

    private void executeIfEnabled(Runnable runnable) {
        if (enabled.get()) {
            runnable.run();
        }
    }
}
