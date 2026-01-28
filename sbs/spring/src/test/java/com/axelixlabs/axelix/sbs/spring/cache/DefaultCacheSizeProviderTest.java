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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultCacheSizeProvider}
 *
 * @author Sergey Cherkasov
 */
public class DefaultCacheSizeProviderTest {
    private final CacheSizeProvider provider = new DefaultCacheSizeProvider();

    @Test
    void caffeineCache_shouldReturnEstimatedSize() {
        Cache<Object, Object> cache = Caffeine.newBuilder().maximumSize(3).build();
        cache.put("key1", "value");
        cache.put("key2", "value");
        cache.put("key3", "value");
        cache.put("key4", "value");
        cache.put("key5", "value");
        cache.cleanUp();

        assertThat(provider.getEstimatedCacheSize(cache)).isEqualTo(3L);
    }

    @Test
    void concurrentHashMap_shouldReturnEstimatedSize() {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        map.put("key1", "value");
        map.put("key2", "value");
        map.put("key3", "value");

        assertThat(provider.getEstimatedCacheSize(map)).isEqualTo(3L);
    }

    @Test
    void unsupportedCacheProvider_shouldReturnNull() {
        Object unknownCacheProvider = new Object();

        assertThat(provider.getEstimatedCacheSize(unknownCacheProvider)).isNull();
    }
}
