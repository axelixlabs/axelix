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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.axelixlabs.axelix.common.api.caches.SingleCache;
import com.axelixlabs.axelix.sbs.spring.cache.exception.CacheManagerNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link DefaultCacheOperationsDispatcher}.
 *
 * @since 24.06.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
class DefaultCacheOperationsDispatcherTest {

    // Cache Manager 1
    private final String TEST_CACHE_MANAGER_1 = "cacheManager1";
    private final String TEST_CACHE_1 = "cache1";
    private final String TEST_CACHE_2 = "cache2";

    // Cache Manager 2
    private final String TEST_CACHE_MANAGER_2 = "cacheManager2";

    private CacheManager cacheManager1;
    private CacheManager cacheManager2;
    private CacheOperationsDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        Map<String, CacheManager> managers = new HashMap<>();

        cacheManager1 = new EnhancedCacheManager(
                TEST_CACHE_MANAGER_1, new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2));
        cacheManager2 = new EnhancedCacheManager(TEST_CACHE_MANAGER_2, new ConcurrentMapCacheManager(TEST_CACHE_2));
        managers.put(TEST_CACHE_MANAGER_1, cacheManager1);
        managers.put(TEST_CACHE_MANAGER_2, cacheManager2);

        CacheSizeProvider cacheSizeProvider = new DefaultCacheSizeProvider();
        dispatcher = new DefaultCacheOperationsDispatcher(managers, cacheSizeProvider);
    }

    @Test
    void clear_shouldRemoveAllEntriesInCache() {
        // given.
        String key = "key";
        String cacheName = TEST_CACHE_1;
        Cache cache = cacheManager1.getCache(cacheName);
        assertThat(cache).isNotNull();

        cache.put(key, "value");
        assertThat(cache.get(key)).isNotNull();

        // when.
        dispatcher.clear(TEST_CACHE_MANAGER_1, cacheName);

        // then.
        assertThat(cache.get(key)).isNull();
    }

    @Test
    void shouldNoOpOnClearingNonExistentCacheManager() {
        assertThatThrownBy(() -> dispatcher.clear("nonExistentCacheManager", "cache"))
                .isInstanceOf(CacheManagerNotFoundException.class);
    }

    @Test
    void clearKey_shouldEvictSingleEntry() {
        // given.
        String cacheName = TEST_CACHE_1;
        String keyToRemove = "keyToRemove", keyToKeep = "keyToKeep";
        Cache cache = cacheManager1.getCache(cacheName);
        assertThat(cache).isNotNull();

        cache.put(keyToRemove, "value1");
        cache.put(keyToKeep, "value2");
        assertThat(cache.get(keyToRemove)).isNotNull();
        assertThat(cache.get(keyToKeep)).isNotNull();

        // when.
        dispatcher.clear(TEST_CACHE_MANAGER_1, cacheName, keyToRemove);

        // then.
        assertThat(cache.get(keyToRemove)).isNull();
        assertThat(cache.get(keyToKeep)).isNotNull().satisfies(cacheValue -> assertThat(cacheValue.get())
                .isEqualTo("value2"));
    }

    @Test
    void clearAll_shouldClearAllCaches() {
        // given.
        String key1 = "key1", key2 = "key2";
        Cache cache1 = cacheManager1.getCache(TEST_CACHE_1);
        Cache cache2 = cacheManager1.getCache(TEST_CACHE_2);
        cache1.put(key1, "value1");
        cache2.put(key2, "value2");

        // when.
        dispatcher.clear(TEST_CACHE_MANAGER_1);

        // then.
        assertThat(cache1.get(key1)).isNull();
        assertThat(cache2.get(key2)).isNull();
    }

    @Test
    void disableCacheManager_shouldDisableSpecificManager() {
        Cache cache1 = cacheManager1.getCache(TEST_CACHE_1);

        cache1.put("key1", "value1");
        assertThat(cache1.get("key1")).isNotNull();

        dispatcher.disableCacheManager(TEST_CACHE_MANAGER_1);

        cache1.put("key2", "value2");
        assertThat(cache1.get("key2")).isNull();
    }

    @Test
    void enableCacheManager_shouldEnableSpecificManager() {
        Cache cache = cacheManager1.getCache(TEST_CACHE_1);

        dispatcher.disableCacheManager(TEST_CACHE_MANAGER_1);

        cache.put("key", "value");
        assertThat(cache.get("key")).isNull();

        dispatcher.enableCacheManager(TEST_CACHE_MANAGER_1);

        cache.put("key2", "value2");
        assertThat(cache.get("key2")).isNotNull();
    }

    @Test
    void disableCache_shouldDisableSpecificCache() {
        Cache cache1 = cacheManager1.getCache(TEST_CACHE_1);

        cache1.put("key1", "value1");
        assertThat(cache1.get("key1")).isNotNull();

        dispatcher.disableCache(TEST_CACHE_MANAGER_1, TEST_CACHE_1);

        cache1.put("key2", "value2");
        assertThat(cache1.get("key2")).isNull();
    }

    @Test
    void enableCache_shouldEnableSpecificCache() {
        Cache cache1 = cacheManager1.getCache(TEST_CACHE_1);
        cache1.put("key", "value");
        dispatcher.disableCache(TEST_CACHE_MANAGER_1, TEST_CACHE_1);

        cache1.put("key", "value");
        assertThat(cache1.get("key")).isNull();

        dispatcher.enableCache(TEST_CACHE_MANAGER_1, TEST_CACHE_1);

        cache1.put("key2", "value2");
        assertThat(cache1.get("key2")).isNotNull();
    }

    @Test
    void disableCache_shouldNotAffectOtherCachesInSameManager() {
        Cache cache1 = cacheManager1.getCache(TEST_CACHE_1);
        Cache cache2 = cacheManager1.getCache(TEST_CACHE_2);

        cache1.put("key1", "value1");
        cache2.put("key2", "value2");
        assertThat(cache1.get("key1")).isNotNull();
        assertThat(cache2.get("key2")).isNotNull();

        dispatcher.disableCache(TEST_CACHE_MANAGER_1, TEST_CACHE_1);

        cache1.put("key3", "value3");
        cache2.put("key4", "value4");

        assertThat(cache1.get("key3")).isNull();
        assertThat(cache2.get("key4")).isNotNull();
    }

    @Test
    void disableCacheManager_shouldNotAffectOtherManagers() {
        Cache cache1 = cacheManager1.getCache(TEST_CACHE_1);
        Cache cache2 = cacheManager2.getCache(TEST_CACHE_2);

        cache1.put("key1", "value1");
        cache2.put("key2", "value2");
        assertThat(cache1.get("key1")).isNotNull();
        assertThat(cache2.get("key2")).isNotNull();

        dispatcher.disableCacheManager(TEST_CACHE_MANAGER_1);

        cache1.put("key3", "value3");
        cache2.put("key4", "value4");

        assertThat(cache1.get("key3")).isNull();
        assertThat(cache2.get("key4")).isNotNull();
    }

    @Test
    void isCacheEnabled_shouldReturnTrueForEnabledCache() {
        assertThat(dispatcher.get(TEST_CACHE_MANAGER_1, TEST_CACHE_1).enabled()).isTrue();
    }

    @Test
    void isCacheEnabled_shouldReturnFalseForDisabledCache() {
        // given.
        String cacheManagerName = TEST_CACHE_MANAGER_1;
        String cacheName = TEST_CACHE_1;

        // when.
        dispatcher.disableCache(cacheManagerName, cacheName);

        // then.
        assertThat(dispatcher.get(cacheManagerName, cacheName).enabled()).isFalse();
    }

    @Test
    void isCacheEnabled_shouldReturnTrueAfterDisableEnableCache() {
        // given.
        String cacheManagerName = TEST_CACHE_MANAGER_1;
        String cacheName = TEST_CACHE_1;

        // when.
        dispatcher.disableCache(cacheManagerName, cacheName);
        dispatcher.enableCache(cacheManagerName, cacheName);

        // then.
        assertThat(dispatcher.get(cacheManagerName, cacheName).enabled()).isTrue();
    }

    @Test
    void isCacheEnabled_shouldReturnFalseWhenCacheManagerDisabled() {
        // given.
        String cacheManagerName = TEST_CACHE_MANAGER_1;

        // when.
        dispatcher.disableCacheManager(cacheManagerName);

        // then.
        assertThat(dispatcher.get(cacheManagerName, TEST_CACHE_1).enabled()).isTrue();
        assertThat(dispatcher.get(cacheManagerName, TEST_CACHE_2).enabled()).isTrue();
    }

    @Test
    void isCacheEnabled_shouldReturnTrueWhenCacheManagerDisableEnable() {
        // given.
        String cacheManagerName = TEST_CACHE_MANAGER_1;
        String cacheName = TEST_CACHE_1;

        dispatcher.get(cacheManagerName, cacheName); // to initialize the cache

        // when.
        dispatcher.disableCacheManager(cacheManagerName);

        // then.
        assertThat(dispatcher.get(cacheManagerName, cacheName).enabled()).isFalse();

        // and also when.
        dispatcher.enableCacheManager(cacheManagerName);

        // then.
        assertThat(dispatcher.get(cacheManagerName, cacheName).enabled()).isTrue();
    }

    @Test
    void shouldReturnCacheInformation_FromDifferentManagers() {
        cacheManager1.getCache(TEST_CACHE_2).put("key1", "value");
        cacheManager1.getCache(TEST_CACHE_2).put("key2", "value");
        cacheManager1.getCache(TEST_CACHE_2).get("key1");
        cacheManager1.getCache(TEST_CACHE_2).get("key2");
        cacheManager1.getCache(TEST_CACHE_2).get("notCache");
        cacheManager1.getCache(TEST_CACHE_2).get("notCache");

        SingleCache first = dispatcher.get(TEST_CACHE_MANAGER_1, TEST_CACHE_2);

        assertThat(first.missesCount()).isEqualTo(2L);
        assertThat(first.estimatedEntrySize()).isEqualTo(2L);
        assertThat(first.hitsCount()).isEqualTo(2L);

        cacheManager2.getCache(TEST_CACHE_2).put("key3", "value");
        cacheManager2.getCache(TEST_CACHE_2).get("key3");
        cacheManager2.getCache(TEST_CACHE_2).get("notCache2");

        SingleCache second = dispatcher.get(TEST_CACHE_MANAGER_2, TEST_CACHE_2);

        assertThat(second.missesCount()).isEqualTo(1L);
        assertThat(second.estimatedEntrySize()).isEqualTo(1L);
        assertThat(second.hitsCount()).isEqualTo(1L);
    }

    @Test
    void shouldReturnNull_ForNonExistentManager() {
        assertThatThrownBy(() -> dispatcher.get("nonExistentManager", TEST_CACHE_1))
                .isInstanceOf(CacheManagerNotFoundException.class);
    }

    @Test
    void enableCacheManager_shouldThrowExceptionForNonExistentManager() {
        assertThatThrownBy(() -> dispatcher.enableCacheManager("nonExistentManager"))
                .isInstanceOf(CacheManagerNotFoundException.class)
                .hasMessageContaining("nonExistentManager");
    }

    @Test
    void disableCacheManager_shouldThrowExceptionForNonExistentManager() {
        assertThatThrownBy(() -> dispatcher.disableCacheManager("nonExistentManager"))
                .isInstanceOf(CacheManagerNotFoundException.class)
                .hasMessageContaining("nonExistentManager");
    }

    @Test
    void enableCache_shouldThrowExceptionForNonExistentManager() {
        assertThatThrownBy(() -> dispatcher.enableCache("nonExistentManager", TEST_CACHE_1))
                .isInstanceOf(CacheManagerNotFoundException.class)
                .hasMessageContaining("nonExistentManager");
    }

    @Test
    void disableCache_shouldThrowExceptionForNonExistentManager() {
        assertThatThrownBy(() -> dispatcher.disableCache("nonExistentManager", TEST_CACHE_1))
                .isInstanceOf(CacheManagerNotFoundException.class)
                .hasMessageContaining("nonExistentManager");
    }

    @Test
    void enableCache_shouldWorkWhenCacheDoesNotExist() {
        assertThatNoException().isThrownBy(() -> dispatcher.enableCache(TEST_CACHE_MANAGER_1, "nonExistentCache"));
    }

    @Test
    void disableCache_shouldWorkWhenCacheDoesNotExist() {
        assertThatNoException().isThrownBy(() -> dispatcher.disableCache(TEST_CACHE_MANAGER_1, "nonExistentCache"));
    }
}
