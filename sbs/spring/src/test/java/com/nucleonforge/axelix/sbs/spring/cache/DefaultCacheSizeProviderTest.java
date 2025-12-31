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
