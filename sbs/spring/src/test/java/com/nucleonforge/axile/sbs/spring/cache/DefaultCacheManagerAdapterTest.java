package com.nucleonforge.axile.sbs.spring.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

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
        assertThat(cache).isNotNull();

        cache.put(key, "value");
        assertThat(cache.get(key)).isNotNull();

        assertThat(cacheManagerAdapter.clear(cacheName)).isTrue();
        assertThat(cache.get(key)).isNull();
    }

    @Test
    void clearAll_shouldClearAllCaches() {
        String key1 = "key1", key2 = "key2";
        Cache cache1 = cacheManager.getCache("cache1");
        Cache cache2 = cacheManager.getCache("cache2");
        assertThat(cache1).isNotNull();
        assertThat(cache2).isNotNull();

        cache1.put(key1, "value1");
        cache2.put(key2, "value2");
        assertThat(cache1.get(key1)).isNotNull();
        assertThat(cache2.get(key2)).isNotNull();

        assertThat(cacheManagerAdapter.clearAll()).isTrue();
        assertThat(cache1.get(key1)).isNull();
        assertThat(cache2.get(key2)).isNull();
    }

    @Test
    void clearWithKey_shouldEvictOnlySpecifiedKey() {
        String cacheName = "cache";
        String keyToRemove = "keyToRemove", keyToKeep = "keyToKeep";
        Cache cache = cacheManager.getCache(cacheName);
        assertThat(cache).isNotNull();

        cache.put(keyToRemove, "value1");
        cache.put(keyToKeep, "value2");
        assertThat(cache.get(keyToRemove)).isNotNull();
        assertThat(cache.get(keyToKeep)).isNotNull();

        assertThat(cacheManagerAdapter.clear(cacheName, keyToRemove)).isTrue();

        assertThat(cache.get(keyToRemove)).isNull();
        assertThat(cache.get(keyToKeep)).isNotNull();
        assertThat(cache.get(keyToKeep)).isNotNull().satisfies(cacheValue -> assertThat(cacheValue.get())
                .isEqualTo("value2"));
    }

    @Test
    void clearAll_shouldDoNothing() {
        assertThat(cacheManager.getCacheNames()).isEmpty();
        assertThatNoException().isThrownBy(() -> cacheManagerAdapter.clearAll());
    }

    @Test
    void clear_shouldDoNothing() {
        String cacheName = "nonExistentCache";
        assertThatNoException().isThrownBy(() -> cacheManagerAdapter.clear(cacheName));
    }

    @Test
    void clearWithKey_shouldDoNothing() {
        String cacheName = "nonExistentCache";
        assertThatNoException().isThrownBy(() -> cacheManagerAdapter.clear(cacheName, "key"));
    }
}
