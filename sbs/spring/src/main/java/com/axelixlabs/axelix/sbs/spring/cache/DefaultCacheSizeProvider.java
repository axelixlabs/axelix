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
