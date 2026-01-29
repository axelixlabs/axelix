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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Unit tests for {@link EnhancedCacheManager}.
 *
 * @since 23.06.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
class EnhancedCacheManagerTest {

    private EnhancedCacheManager subject;

    @BeforeEach
    void setUp() {
        CacheManager cacheManager = new ConcurrentMapCacheManager();
        subject = new EnhancedCacheManager("testCacheManagerBeanName", cacheManager);
    }

    @Test
    void clear_shouldCallClearOnCache() {
        // given.
        String key = "key";
        String cacheName = "cache";
        Cache cache = subject.getCache(cacheName);
        assertThat(cache).isNotNull();

        cache.put(key, "value");
        assertThat(cache.get(key)).isNotNull();

        // when.
        subject.clear(cacheName);

        // then.
        assertThat(cache.get(key)).isNull();
    }

    @Test
    void clearAll_shouldClearAllCaches() {
        // given.
        String key1 = "key1", key2 = "key2";
        Cache cache1 = subject.getCache("cache1");
        Cache cache2 = subject.getCache("cache2");
        assertThat(cache1).isNotNull();
        assertThat(cache2).isNotNull();

        cache1.put(key1, "value1");
        cache2.put(key2, "value2");
        assertThat(cache1.get(key1)).isNotNull();
        assertThat(cache2.get(key2)).isNotNull();

        // when.
        subject.clearAll();

        // then.
        assertThat(cache1.get(key1)).isNull();
        assertThat(cache2.get(key2)).isNull();
    }

    @Test
    void clearWithKey_shouldEvictOnlySpecifiedKey() {
        // given.
        String cacheName = "cache";
        String keyToRemove = "keyToRemove", keyToKeep = "keyToKeep";
        Cache cache = subject.getCache(cacheName);
        assertThat(cache).isNotNull();

        cache.put(keyToRemove, "value1");
        cache.put(keyToKeep, "value2");
        assertThat(cache.get(keyToRemove)).isNotNull();
        assertThat(cache.get(keyToKeep)).isNotNull();

        // when.
        subject.clear(cacheName, keyToRemove);

        // then.
        assertThat(cache.get(keyToRemove)).isNull();
        assertThat(cache.get(keyToKeep)).isNotNull().satisfies(cacheValue -> assertThat(cacheValue.get())
                .isEqualTo("value2"));
    }

    @Test
    void clearAll_shouldDoNothing() {
        assertThat(subject.getCacheNames()).isEmpty();
        assertThatNoException().isThrownBy(() -> subject.clearAll());
    }

    @Test
    void clear_shouldDoNothing() {
        String cacheName = "nonExistentCache";
        assertThatNoException().isThrownBy(() -> subject.clear(cacheName));
    }

    @Test
    void clearWithKey_shouldDoNothing() {
        String cacheName = "nonExistentCache";
        assertThatNoException().isThrownBy(() -> subject.clear(cacheName, "key"));
    }

    @Test
    void enableCacheManager_shouldEnableAllCachesWhenEnhancedCacheManager() {
        Cache cache = subject.getCache("cache");
        assert cache != null;

        subject.disableAll();

        cache.put("key", "value");
        assertThat(cache.get("key")).isNull();

        // when.
        subject.enableAll();

        // then.
        cache.put("key2", "value2");
        assertThat(cache.get("key2")).isNotNull();
    }

    @Test
    void disableCacheManager_shouldDisableAllCachesWhenEnhancedCacheManager() {
        // given.
        Cache cache = subject.getCache("cache");
        cache.put("key", "value");

        assertThat(cache.get("key")).isNotNull();

        // when.
        subject.disableAll();

        // then.
        cache.put("key2", "value2");
        assertThat(cache.get("key2")).isNull();
    }

    @Test
    void enableCache_shouldEnableSpecificCacheWhenEnhancedCacheManager() {
        // given.
        Cache cache = subject.getCache("cache");
        assert cache != null;
        subject.disable("cache");

        cache.put("key", "value");
        assertThat(cache.get("key")).isNull();

        // when.
        subject.enable("cache");

        // then.
        cache.put("key2", "value2");
        assertThat(cache.get("key2"))
                .isNotNull()
                .extracting(Cache.ValueWrapper::get)
                .isEqualTo("value2");
    }

    @Test
    void disableCache_shouldDisableSpecificCacheWhenEnhancedCacheManager() {
        // given.
        Cache cache = subject.getCache("cache");
        cache.put("key", "value");
        assertThat(cache.get("key")).isNotNull();

        // when.
        subject.disable("cache");

        // then.
        cache.put("key2", "value2");
        assertThat(cache.get("key2")).isNull();
    }

    @Test
    void enableCache_shouldWorkWhenCacheDoesNotExistInEnhancedCacheManager() {
        assertThatNoException().isThrownBy(() -> subject.enable("nonExistentCache"));
    }

    @Test
    void disableCache_shouldWorkWhenCacheDoesNotExistInEnhancedCacheManager() {
        assertThatNoException().isThrownBy(() -> subject.disable("nonExistentCache"));
    }

    @Test
    void enableCache_shouldNotAffectOtherCachesInEnhancedCacheManager() {
        // given.
        Cache cache1 = subject.getCache("cache1");
        Cache cache2 = subject.getCache("cache2");

        // when.
        subject.disable("cache1");

        // then.
        cache1.put("key1", "value1");
        cache2.put("key2", "value2");
        assertThat(cache1.get("key1")).isNull();
        assertThat(cache2.get("key2")).isNotNull();

        // and also when.
        subject.enable("cache1");

        // then.
        cache1.put("key3", "value3");
        cache2.put("key4", "value4");

        assertThat(cache1.get("key3")).isNotNull();
        assertThat(cache2.get("key4")).isNotNull();
    }

    @Test
    void isCacheEnabled_shouldReturnTrueForEnabledCacheInEnhancedCacheManager() {
        // when.
        String cacheName = "cache";
        Cache cache = subject.getCache(cacheName);

        // then.
        assertThat(cache).isNotNull();
        assertThat(subject.isEnabled(cacheName)).isTrue();
    }

    @Test
    void isCacheEnabled_shouldReturnFalseForDisabledCacheInEnhancedCacheManager() {
        // given.
        String cacheName = "cache";
        Cache cache = subject.getCache(cacheName);

        // when.
        subject.disable(cacheName);

        // then.
        assertThat(cache).isNotNull();
        assertThat(subject.isEnabled(cacheName)).isFalse();
    }

    @Test
    void isCacheEnabled_shouldReturnTrueAfterEnableDisableCacheInEnhancedCacheManager() {
        // given.
        String cacheName = "cache";

        // when.
        subject.disable(cacheName);
        assertThat(subject.isEnabled(cacheName)).isFalse();

        // then.
        subject.enable(cacheName);
        assertThat(subject.isEnabled(cacheName)).isTrue();
    }

    @Test
    void isCacheEnabled_shouldReturnFalseWhenCacheManagerDisabledInEnhancedCacheManager() {
        // given.
        String cacheName1 = "cache1";
        String cacheName2 = "cache2";
        subject.getCache(cacheName1);
        subject.getCache(cacheName2);

        // when.
        subject.disableAll();

        // then.
        assertThat(subject.isEnabled(cacheName1)).isFalse();
        assertThat(subject.isEnabled(cacheName2)).isFalse();
    }

    @Test
    void isCacheEnabled_shouldWorkWithMultipleCachesInEnhancedCacheManager() {
        // given.
        String[] cacheNames = {"cache1", "cache2", "cache3"};
        for (String cacheName : cacheNames) {
            subject.getCache(cacheName);
        }

        // when.
        subject.disable("cache2");

        // then.
        assertThat(subject.isEnabled("cache1")).isTrue();
        assertThat(subject.isEnabled("cache2")).isFalse();
        assertThat(subject.isEnabled("cache3")).isTrue();
    }

    @Test
    void shouldReturnCacheMetaInfo() {
        String cacheName = "cache";
        String key = "key";

        Cache cache = subject.getCache(cacheName);

        cache.put(key, "value");
        cache.get(key);
        cache.get("notCache");
        cache.get("notCache");

        assertThat(subject.getHitsCount(cacheName)).isEqualTo(1L);
        assertThat(subject.getMissesCount(cacheName)).isEqualTo(2L);
        assertThat(subject.getNativeCache(cacheName)).isEqualTo(cache.getNativeCache());
    }
}
