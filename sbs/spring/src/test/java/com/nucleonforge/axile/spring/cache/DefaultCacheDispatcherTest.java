package com.nucleonforge.axile.spring.cache;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        cache.put(key, "value");
        assertNotNull(cache.get(key));

        boolean result = dispatcher.clear(cacheManagerName, cacheName);

        assertTrue(result);
        assertNull(cache.get(key));
    }

    @Test
    void clear_shouldReturnFalse() {
        boolean result = dispatcher.clear("nonExistentCacheManager", "cache");

        assertFalse(result);
    }

    @Test
    void clearKey_shouldEvictSingleEntry() {
        String cacheName = "cache";
        String keyToRemove = "keyToRemove", keyToKeep = "keyToKeep";
        String cacheManagerName = "cacheManager";
        Cache cache = cacheManager.getCache(cacheName);
        cache.put(keyToRemove, "value1");
        cache.put(keyToKeep, "value2");
        assertNotNull(cache.get(keyToRemove));
        assertNotNull(cache.get(keyToKeep));

        boolean result = dispatcher.clear(cacheManagerName, cacheName, keyToRemove);

        assertTrue(result);
        assertNull(cache.get(keyToRemove));
        assertNotNull(cache.get(keyToKeep));
        assertEquals("value2", cache.get(keyToKeep).get());
    }

    @Test
    void clearKey_shouldReturnFalse() {
        boolean result = dispatcher.clear("nonExistentCacheManager", "cache", "key");

        assertFalse(result);
    }

    @Test
    void clearAll_shouldClearAllCaches() {
        String key1 = "key1", key2 = "key2";
        String cacheManagerName = "cacheManager";
        Cache cache1 = cacheManager.getCache("cache1");
        Cache cache2 = cacheManager.getCache("cache2");
        cache1.put(key1, "value1");
        cache2.put(key2, "value2");
        assertNotNull(cache1.get(key1));
        assertNotNull(cache2.get(key2));

        boolean result = dispatcher.clearAll(cacheManagerName);

        assertTrue(result);
        assertNull(cache1.get(key1));
        assertNull(cache2.get(key2));
    }

    @Test
    void clearAll_shouldReturnFalse() {
        boolean result = dispatcher.clearAll("nonExistentCacheManager");

        assertFalse(result);
    }
}
