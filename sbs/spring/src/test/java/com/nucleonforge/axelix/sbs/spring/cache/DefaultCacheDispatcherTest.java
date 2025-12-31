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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link DefaultCacheDispatcher}.
 *
 * @since 24.06.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
class DefaultCacheDispatcherTest {

    // Cache Manager 1
    private final String TEST_CACHE_MANAGER_1 = "cacheManager1";
    private final String TEST_CACHE_1 = "cache1";
    private final String TEST_CACHE_2 = "cache2";

    // Cache Manager 2
    private final String TEST_CACHE_MANAGER_2 = "cacheManager2";

    private CacheManager cacheManager1;
    private CacheManager cacheManager2;
    private CacheDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        Map<String, CacheManager> managers = new HashMap<>();

        cacheManager1 = new EnhancedCacheManager(new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2));
        cacheManager2 = new EnhancedCacheManager(new ConcurrentMapCacheManager(TEST_CACHE_2));
        managers.put(TEST_CACHE_MANAGER_1, cacheManager1);
        managers.put(TEST_CACHE_MANAGER_2, cacheManager2);

        CacheSizeProvider cacheSizeProvider = new DefaultCacheSizeProvider();
        dispatcher = new DefaultCacheDispatcher(managers, cacheSizeProvider);
    }

    @Test
    void clear_shouldRemoveAllEntriesInCache() {
        String key = "key";
        String cacheName = TEST_CACHE_1;
        Cache cache = cacheManager1.getCache(cacheName);
        assertThat(cache).isNotNull();

        cache.put(key, "value");
        assertThat(cache.get(key)).isNotNull();

        assertThat(dispatcher.clear(TEST_CACHE_MANAGER_1, cacheName)).isTrue();
        assertThat(cache.get(key)).isNull();
    }

    @Test
    void clear_shouldReturnFalse() {
        assertThat(dispatcher.clear("nonExistentCacheManager", "cache")).isFalse();
    }

    @Test
    void clearKey_shouldEvictSingleEntry() {
        String cacheName = TEST_CACHE_1;
        String keyToRemove = "keyToRemove", keyToKeep = "keyToKeep";
        Cache cache = cacheManager1.getCache(cacheName);
        assertThat(cache).isNotNull();

        cache.put(keyToRemove, "value1");
        cache.put(keyToKeep, "value2");
        assertThat(cache.get(keyToRemove)).isNotNull();
        assertThat(cache.get(keyToKeep)).isNotNull();

        assertThat(dispatcher.clear(TEST_CACHE_MANAGER_1, cacheName, keyToRemove))
                .isTrue();

        assertThat(cache.get(keyToRemove)).isNull();
        assertThat(cache.get(keyToKeep)).isNotNull().satisfies(cacheValue -> assertThat(cacheValue.get())
                .isEqualTo("value2"));
    }

    @Test
    void clearKey_shouldReturnFalse() {
        assertThat(dispatcher.clear("nonExistentCacheManager", "cache", "key")).isFalse();
    }

    @Test
    void clearAll_shouldClearAllCaches() {
        String key1 = "key1", key2 = "key2";
        Cache cache1 = cacheManager1.getCache(TEST_CACHE_1);
        Cache cache2 = cacheManager1.getCache(TEST_CACHE_2);
        assertThat(cache1).isNotNull();
        assertThat(cache2).isNotNull();

        cache1.put(key1, "value1");
        cache2.put(key2, "value2");
        assertThat(cache1.get(key1)).isNotNull();
        assertThat(cache2.get(key2)).isNotNull();

        assertThat(dispatcher.clearAll(TEST_CACHE_MANAGER_1)).isTrue();

        assertThat(cache1.get(key1)).isNull();
        assertThat(cache2.get(key2)).isNull();
    }

    @Test
    void clearAll_shouldReturnFalse() {
        assertThat(dispatcher.clearAll("nonExistentCacheManager")).isFalse();
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
        assertThat(dispatcher.isCacheEnabled(TEST_CACHE_MANAGER_1, TEST_CACHE_1))
                .isTrue();
    }

    @Test
    void isCacheEnabled_shouldReturnFalseForDisabledCache() {
        String cacheManagerName = TEST_CACHE_MANAGER_1;
        String cacheName = TEST_CACHE_1;

        dispatcher.disableCache(cacheManagerName, cacheName);

        assertThat(dispatcher.isCacheEnabled(cacheManagerName, cacheName)).isFalse();
    }

    @Test
    void isCacheEnabled_shouldReturnTrueAfterDisableEnableCache() {
        String cacheManagerName = TEST_CACHE_MANAGER_1;
        String cacheName = TEST_CACHE_1;

        dispatcher.disableCache(cacheManagerName, cacheName);
        dispatcher.enableCache(cacheManagerName, cacheName);

        assertThat(dispatcher.isCacheEnabled(cacheManagerName, cacheName)).isTrue();
    }

    @Test
    void isCacheEnabled_shouldReturnFalseWhenCacheManagerDisabled() {
        String cacheManagerName = TEST_CACHE_MANAGER_1;

        dispatcher.disableCacheManager(cacheManagerName);

        assertThat(dispatcher.isCacheEnabled(cacheManagerName, TEST_CACHE_1)).isFalse();
        assertThat(dispatcher.isCacheEnabled(cacheManagerName, TEST_CACHE_2)).isFalse();
    }

    @Test
    void isCacheEnabled_shouldReturnTrueWhenCacheManagerDisableEnable() {
        String cacheManagerName = TEST_CACHE_MANAGER_1;
        String cacheName = TEST_CACHE_1;

        dispatcher.disableCacheManager(cacheManagerName);

        assertThat(dispatcher.isCacheEnabled(cacheManagerName, cacheName)).isFalse();

        dispatcher.enableCacheManager(cacheManagerName);

        assertThat(dispatcher.isCacheEnabled(cacheManagerName, cacheName)).isTrue();
    }

    @Test
    void shouldReturnCacheInformation_FromDifferentManagers() {
        cacheManager1.getCache(TEST_CACHE_2).put("key1", "value");
        cacheManager1.getCache(TEST_CACHE_2).put("key2", "value");
        cacheManager1.getCache(TEST_CACHE_2).get("key1");
        cacheManager1.getCache(TEST_CACHE_2).get("key2");
        cacheManager1.getCache(TEST_CACHE_2).get("notCache");
        cacheManager1.getCache(TEST_CACHE_2).get("notCache");

        assertThat(dispatcher.getMissesCount(TEST_CACHE_MANAGER_1, TEST_CACHE_2))
                .isEqualTo(2L);
        assertThat(dispatcher.getHitsCount(TEST_CACHE_MANAGER_1, TEST_CACHE_2)).isEqualTo(2L);
        assertThat(dispatcher.getEstimatedCacheSize(TEST_CACHE_MANAGER_1, TEST_CACHE_2))
                .isEqualTo(2L);

        cacheManager2.getCache(TEST_CACHE_2).put("key3", "value");
        cacheManager2.getCache(TEST_CACHE_2).get("key3");
        cacheManager2.getCache(TEST_CACHE_2).get("notCache2");

        assertThat(dispatcher.getMissesCount(TEST_CACHE_MANAGER_2, TEST_CACHE_2))
                .isEqualTo(1L);
        assertThat(dispatcher.getHitsCount(TEST_CACHE_MANAGER_2, TEST_CACHE_2)).isEqualTo(1L);
        assertThat(dispatcher.getEstimatedCacheSize(TEST_CACHE_MANAGER_2, TEST_CACHE_2))
                .isEqualTo(1L);
    }

    @Test
    void shouldReturnNull_ForNonExistentManager() {
        String cacheManagerName = "nonExistentManager";
        String cacheName = TEST_CACHE_1;

        assertThat(dispatcher.getMissesCount(cacheManagerName, cacheName)).isNull();
        assertThat(dispatcher.getHitsCount(cacheManagerName, cacheName)).isNull();
        assertThat(dispatcher.getEstimatedCacheSize(cacheManagerName, cacheName))
                .isNull();
    }

    @Test
    void enableCacheManager_shouldThrowExceptionForNonExistentManager() {
        assertThatThrownBy(() -> dispatcher.enableCacheManager("nonExistentManager"))
                .isInstanceOf(CacheManagerAdapterNotFoundException.class)
                .hasMessageContaining("nonExistentManager");
    }

    @Test
    void disableCacheManager_shouldThrowExceptionForNonExistentManager() {
        assertThatThrownBy(() -> dispatcher.disableCacheManager("nonExistentManager"))
                .isInstanceOf(CacheManagerAdapterNotFoundException.class)
                .hasMessageContaining("nonExistentManager");
    }

    @Test
    void enableCache_shouldThrowExceptionForNonExistentManager() {
        assertThatThrownBy(() -> dispatcher.enableCache("nonExistentManager", TEST_CACHE_1))
                .isInstanceOf(CacheManagerAdapterNotFoundException.class)
                .hasMessageContaining("nonExistentManager");
    }

    @Test
    void disableCache_shouldThrowExceptionForNonExistentManager() {
        assertThatThrownBy(() -> dispatcher.disableCache("nonExistentManager", TEST_CACHE_1))
                .isInstanceOf(CacheManagerAdapterNotFoundException.class)
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
