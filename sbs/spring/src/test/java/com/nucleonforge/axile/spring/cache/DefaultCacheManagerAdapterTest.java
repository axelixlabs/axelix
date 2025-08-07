package com.nucleonforge.axile.spring.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link DefaultCacheManagerAdapter} verifying cache clearance functionality.
 * <p>Uses Spring's {@link ConcurrentMapCacheManager} for realistic cache behavior to test actual interactions.</p>
 *
 * @since 23.06.2025
 * @author Nikita Kirillov
 */
class DefaultCacheManagerAdapterTest {

    private CacheManager cacheManager;
    private CacheManagerAdapter cacheManagerAdapter;

    @BeforeEach
    void setUp() {
        cacheManager = new ConcurrentMapCacheManager();
        cacheManagerAdapter = new DefaultCacheManagerAdapter(cacheManager);
    }

    @Test
    void clear_shouldCallClearOnCache() {
        String key = "key";
        String cacheName = "cache";
        Cache cache = cacheManager.getCache(cacheName);
        cache.put(key, "value");
        assertNotNull(cache.get(key));

        cacheManagerAdapter.clear(cacheName);

        assertNull(cache.get(key));
    }

    @Test
    void clear_shouldDoNothing() {
        String cacheName = "nonExistentCache";

        Assertions.assertDoesNotThrow(() -> cacheManagerAdapter.clear(cacheName));
    }

    @Test
    void clearAll_shouldClearAllCaches() {
        String key1 = "key1", key2 = "key2";
        Cache cache1 = cacheManager.getCache("cache1");
        Cache cache2 = cacheManager.getCache("cache2");
        cache1.put(key1, "value1");
        cache2.put(key2, "value2");
        assertNotNull(cache1.get(key1));
        assertNotNull(cache2.get(key2));

        cacheManagerAdapter.clearAll();

        assertNull(cache1.get(key1));
        assertNull(cache2.get(key2));
    }

    @Test
    void clearAll_shouldDoNothing() {
        assertEquals(0, cacheManager.getCacheNames().size());

        Assertions.assertDoesNotThrow(() -> cacheManagerAdapter.clearAll());
    }

    @Test
    void clearWithKey_shouldEvictOnlySpecifiedKey() {
        String cacheName = "cache";
        String keyToRemove = "keyToRemove", keyToKeep = "keyToKeep";
        Cache cache = cacheManager.getCache(cacheName);
        cache.put(keyToRemove, "value1");
        cache.put(keyToKeep, "value2");
        assertNotNull(cache.get(keyToRemove));
        assertNotNull(cache.get(keyToKeep));

        cacheManagerAdapter.clear(cacheName, keyToRemove);

        assertNull(cache.get(keyToRemove));
        assertNotNull(cache.get(keyToKeep));
        assertEquals("value2", cache.get(keyToKeep).get());
    }

    @Test
    void clearWithKey_shouldDoNothing() {
        String cacheName = "nonExistentCache";

        Assertions.assertDoesNotThrow(() -> cacheManagerAdapter.clear(cacheName, "key"));
    }
}
