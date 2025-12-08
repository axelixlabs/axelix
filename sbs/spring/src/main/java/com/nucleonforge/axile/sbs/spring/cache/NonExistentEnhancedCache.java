/*
 * Copyright 2025-present the original author or authors.
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
