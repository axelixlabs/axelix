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
 * @author Sergey Cherkasov
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

    @Override
    public long getHitsCount() {
        return 0;
    }

    @Override
    public long getMissesCount() {
        return 0;
    }
}
