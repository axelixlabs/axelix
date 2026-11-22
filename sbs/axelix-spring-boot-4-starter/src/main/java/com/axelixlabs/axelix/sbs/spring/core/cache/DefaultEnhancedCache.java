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
package com.axelixlabs.axelix.sbs.spring.core.cache;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.cache.Cache;

import com.axelixlabs.axelix.sbs.spring.core.cache.CacheLookup.Outcome;
import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;

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
    private final @Nullable AxelixMetricsPublisher metricsPublisher;
    private final Supplier<Boolean> isEnabledFunction;
    private final EnhancedCacheManager cacheManager;

    public DefaultEnhancedCache(
            @NonNull Cache delegate,
            @Nullable AxelixMetricsPublisher metricsPublisher,
            Supplier<Boolean> isEnabledFunction,
            EnhancedCacheManager cacheManager) {
        this.delegate = delegate;
        this.isEnabledFunction = isEnabledFunction;
        this.metricsPublisher = metricsPublisher;
        this.cacheManager = cacheManager;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabledFunction.get();
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
        if (!isEnabledFunction.get()) {
            return null;
        }

        ValueWrapper result = delegate.get(key);

        if (result == null) {
            cacheManager.getLookupHistory(getName()).put(CacheLookup.miss());

            if (metricsPublisher != null) {
                metricsPublisher.incrementCacheLookup(delegate.getName(), Outcome.MISS);
            }
        } else {
            cacheManager.getLookupHistory(getName()).put(CacheLookup.hit());

            if (metricsPublisher != null) {
                metricsPublisher.incrementCacheLookup(delegate.getName(), Outcome.HIT);
            }
        }
        return result;
    }

    @Override
    @Nullable
    public <T> T get(@NonNull Object key, @Nullable Class<T> type) {
        if (isEnabledFunction.get()) {
            return delegate.get(key, type);
        } else {
            return null;
        }
    }

    @Override
    @Nullable
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        T value = null;

        if (isEnabledFunction.get()) {
            AtomicBoolean miss = new AtomicBoolean();

            value = delegate.get(key, () -> {
                miss.set(true);
                return valueLoader.call();
            });

            if (miss.get()) {
                cacheManager.getLookupHistory(getName()).put(CacheLookup.miss());

                if (metricsPublisher != null) {
                    metricsPublisher.incrementCacheLookup(delegate.getName(), Outcome.MISS);
                }
            } else {
                cacheManager.getLookupHistory(getName()).put(CacheLookup.hit());

                if (metricsPublisher != null) {
                    metricsPublisher.incrementCacheLookup(delegate.getName(), Outcome.HIT);
                }
            }
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
    public List<CacheLookup> getCacheLookups() {
        return cacheManager.getLookupHistory(getName()).get();
    }

    private boolean executeIfEnabledOrElseFalse(BooleanSupplier supplier) {
        return isEnabledFunction.get() && supplier.getAsBoolean();
    }

    private void executeIfEnabled(Runnable runnable) {
        if (isEnabledFunction.get()) {
            runnable.run();
        }
    }
}
