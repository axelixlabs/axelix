package com.nucleonforge.axile.sbs.spring.cache;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultCacheDispatcher} with a real {@link ConcurrentMapCacheManager}.
 *
 * <p>Verifies cache clearing behavior for individual entries, entire caches,
 * and all caches managed by a {@code CacheManager}.
 *
 * @since 24.06.2025
 * @author Nikita Kirillov
 */
class DefaultCacheDispatcherTest {

    private CacheManager cacheManager;
    private CacheDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        cacheManager = new ConcurrentMapCacheManager();
        Map<String, CacheManager> managers = new HashMap<>();
        managers.put("cacheManager", cacheManager);
        dispatcher = new DefaultCacheDispatcher(managers);
    }

    @Test
    void clear_shouldRemoveAllEntriesInCache() {
        String key = "key";
        String cacheName = "cache";
        String cacheManagerName = "cacheManager";
        Cache cache = cacheManager.getCache(cacheName);
        assertThat(cache).isNotNull();

        cache.put(key, "value");
        assertThat(cache.get(key)).isNotNull();

        assertThat(dispatcher.clear(cacheManagerName, cacheName)).isTrue();
        assertThat(cache.get(key)).isNull();
    }

    @Test
    void clear_shouldReturnFalse() {
        assertThat(dispatcher.clear("nonExistentCacheManager", "cache")).isFalse();
    }

    @Test
    void clearKey_shouldEvictSingleEntry() {
        String cacheName = "cache";
        String keyToRemove = "keyToRemove", keyToKeep = "keyToKeep";
        String cacheManagerName = "cacheManager";
        Cache cache = cacheManager.getCache(cacheName);
        assertThat(cache).isNotNull();

        cache.put(keyToRemove, "value1");
        cache.put(keyToKeep, "value2");
        assertThat(cache.get(keyToRemove)).isNotNull();
        assertThat(cache.get(keyToKeep)).isNotNull();

        assertThat(dispatcher.clear(cacheManagerName, cacheName, keyToRemove)).isTrue();

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
        String cacheManagerName = "cacheManager";
        Cache cache1 = cacheManager.getCache("cache1");
        Cache cache2 = cacheManager.getCache("cache2");
        assertThat(cache1).isNotNull();
        assertThat(cache2).isNotNull();

        cache1.put(key1, "value1");
        cache2.put(key2, "value2");
        assertThat(cache1.get(key1)).isNotNull();
        assertThat(cache2.get(key2)).isNotNull();

        assertThat(dispatcher.clearAll(cacheManagerName)).isTrue();

        assertThat(cache1.get(key1)).isNull();
        assertThat(cache2.get(key2)).isNull();
    }

    @Test
    void clearAll_shouldReturnFalse() {
        assertThat(dispatcher.clearAll("nonExistentCacheManager")).isFalse();
    }
}
