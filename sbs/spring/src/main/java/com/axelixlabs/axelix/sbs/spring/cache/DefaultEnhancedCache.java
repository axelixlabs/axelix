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
package com.axelixlabs.axelix.sbs.spring.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BooleanSupplier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.cache.Cache;

/**
 * Cache implementation that can be dynamically enabled or disabled at runtime.
 *
 * @since 25.11.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public class DefaultEnhancedCache implements EnhancedCache {

    private final Cache delegate;
    private final AtomicBoolean enabled;
    private final LongAdder hitsCount;
    private final LongAdder missesCount;

    public DefaultEnhancedCache(@NonNull Cache delegate) {
        this.delegate = delegate;
        this.enabled = new AtomicBoolean(true);
        this.hitsCount = new LongAdder();
        this.missesCount = new LongAdder();
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
    @NonNull
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    @Nullable
    public ValueWrapper get(@NonNull Object key) {
        if (!enabled.get()) {
            return null;
        }

        ValueWrapper result = delegate.get(key);

        if (result == null) {
            missesCount.increment();
        } else {
            hitsCount.increment();
        }
        return result;
    }

    @Override
    @Nullable
    public <T> T get(@NonNull Object key, @Nullable Class<T> type) {
        if (enabled.get()) {
            return delegate.get(key, type);
        } else {
            return null;
        }
    }

    @Override
    @Nullable
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        T value = null;

        if (enabled.get()) {
            AtomicBoolean miss = new AtomicBoolean();

            value = delegate.get(key, () -> {
                miss.set(true);
                return valueLoader.call();
            });

            (miss.get() ? missesCount : hitsCount).increment();
        }

        return value;
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

    @Override
    public boolean invalidate() {
        return executeIfEnabledOrElseFalse(delegate::invalidate);
    }

    @Override
    public boolean evictIfPresent(@NonNull Object key) {
        return executeIfEnabledOrElseFalse(() -> delegate.evictIfPresent(key));
    }

    @Override
    public long getHitsCount() {
        return hitsCount.sum();
    }

    @Override
    public long getMissesCount() {
        return missesCount.sum();
    }

    private boolean executeIfEnabledOrElseFalse(BooleanSupplier supplier) {
        return enabled.get() && supplier.getAsBoolean();
    }

    private void executeIfEnabled(Runnable runnable) {
        if (enabled.get()) {
            runnable.run();
        }
    }
}
