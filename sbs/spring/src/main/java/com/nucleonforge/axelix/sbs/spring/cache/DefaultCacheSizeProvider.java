/*
 * Copyright 2025-present, Nucleon Forge Software.
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
package com.nucleonforge.axelix.sbs.spring.cache;

import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import org.springframework.util.ClassUtils;

/**
 * Default implementation {@link CacheSizeProvider}.
 *
 * @author Sergey Cherkasov
 */
public class DefaultCacheSizeProvider implements CacheSizeProvider {
    private static final ClassLoader CLASS_LOADER = DefaultCacheSizeProvider.class.getClassLoader();

    private static final boolean CAFFEINE_CACHE_PRESENT =
            ClassUtils.isPresent("com.github.benmanes.caffeine.cache.Cache", CLASS_LOADER);

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable Long getEstimatedCacheSize(@Nullable Object nativeCache) {
        if (nativeCache == null) {
            return null;
        }

        if (CAFFEINE_CACHE_PRESENT && nativeCache instanceof com.github.benmanes.caffeine.cache.Cache<?, ?>) {
            com.github.benmanes.caffeine.cache.Cache<Object, Object> cache =
                    (com.github.benmanes.caffeine.cache.Cache<Object, Object>) nativeCache;
            return cache.estimatedSize();
        }

        if (nativeCache instanceof ConcurrentHashMap<?, ?>) {
            ConcurrentHashMap<Object, Object> cache = (ConcurrentHashMap<Object, Object>) nativeCache;
            return cache.mappingCount();
        }

        return null;
    }
}
