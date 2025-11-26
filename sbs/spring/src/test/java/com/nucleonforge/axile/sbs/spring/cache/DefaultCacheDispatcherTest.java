package com.nucleonforge.axile.sbs.spring.cache;

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
 */
class DefaultCacheDispatcherTest {

    private CacheManager cacheManager1;
    private CacheManager cacheManager2;
    private CacheDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        cacheManager1 = new EnhancedCacheManager(new ConcurrentMapCacheManager());
        cacheManager2 = new EnhancedCacheManager(new ConcurrentMapCacheManager());
        Map<String, CacheManager> managers = new HashMap<>();
        managers.put("cacheManager1", cacheManager1);
        managers.put("cacheManager2", cacheManager2);
        dispatcher = new DefaultCacheDispatcher(managers);
    }

    @Test
    void clear_shouldRemoveAllEntriesInCache() {
        String key = "key";
        String cacheName = "cache";
        String cacheManagerName = "cacheManager1";
        Cache cache = cacheManager1.getCache(cacheName);
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
        String cacheManagerName = "cacheManager1";
        Cache cache = cacheManager1.getCache(cacheName);
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
        String cacheManagerName = "cacheManager1";
        Cache cache1 = cacheManager1.getCache("cache1");
        Cache cache2 = cacheManager1.getCache("cache2");
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

    @Test
    void disableCacheManager_shouldDisableSpecificManager() {
        Cache cache1 = cacheManager1.getCache("cache1");
        assert cache1 != null;

        cache1.put("key1", "value1");
        assertThat(cache1.get("key1")).isNotNull();

        dispatcher.disableCacheManager("cacheManager1");

        cache1.put("key2", "value2");
        assertThat(cache1.get("key2")).isNull();
    }

    @Test
    void enableCacheManager_shouldEnableSpecificManager() {
        Cache cache = cacheManager1.getCache("cache1");
        assert cache != null;

        dispatcher.disableCacheManager("cacheManager1");

        cache.put("key", "value");
        assertThat(cache.get("key")).isNull();

        dispatcher.enableCacheManager("cacheManager1");

        cache.put("key2", "value2");
        assertThat(cache.get("key2")).isNotNull();
    }

    @Test
    void disableCache_shouldDisableSpecificCache() {
        Cache cache1 = cacheManager1.getCache("cache1");
        assert cache1 != null;

        cache1.put("key1", "value1");
        assertThat(cache1.get("key1")).isNotNull();

        dispatcher.disableCache("cacheManager1", "cache1");

        cache1.put("key2", "value2");
        assertThat(cache1.get("key2")).isNull();
    }

    @Test
    void enableCache_shouldEnableSpecificCache() {
        Cache cache1 = cacheManager1.getCache("cache1");
        assert cache1 != null;
        cache1.put("key", "value");
        dispatcher.disableCache("cacheManager1", "cache1");

        cache1.put("key", "value");
        assertThat(cache1.get("key")).isNull();

        dispatcher.enableCache("cacheManager1", "cache1");

        cache1.put("key2", "value2");
        assertThat(cache1.get("key2")).isNotNull();
    }

    @Test
    void enableAllCache_shouldEnableAllManagers() {
        Cache cache1 = cacheManager1.getCache("cache1");
        Cache cache2 = cacheManager2.getCache("cache2");
        assert cache1 != null;
        assert cache2 != null;

        dispatcher.disableCacheManager("cacheManager1");
        dispatcher.disableCacheManager("cacheManager2");

        cache1.put("key1", "value1");
        cache2.put("key2", "value2");
        assertThat(cache1.get("key1")).isNull();
        assertThat(cache2.get("key2")).isNull();

        dispatcher.enableAllCache();

        cache1.put("key3", "value3");
        cache2.put("key4", "value4");

        assertThat(cache1.get("key3")).isNotNull();
        assertThat(cache2.get("key4")).isNotNull();
    }

    @Test
    void disableCache_shouldNotAffectOtherCachesInSameManager() {
        Cache cache1 = cacheManager1.getCache("cache1");
        Cache cache2 = cacheManager1.getCache("cache2");
        assert cache1 != null;
        assert cache2 != null;

        cache1.put("key1", "value1");
        cache2.put("key2", "value2");
        assertThat(cache1.get("key1")).isNotNull();
        assertThat(cache2.get("key2")).isNotNull();

        dispatcher.disableCache("cacheManager1", "cache1");

        cache1.put("key3", "value3");
        cache2.put("key4", "value4");

        assertThat(cache1.get("key3")).isNull();
        assertThat(cache2.get("key4")).isNotNull();
    }

    @Test
    void disableCacheManager_shouldNotAffectOtherManagers() {
        Cache cache1 = cacheManager1.getCache("cache1");
        Cache cache2 = cacheManager2.getCache("cache2");
        assert cache1 != null;
        assert cache2 != null;

        cache1.put("key1", "value1");
        cache2.put("key2", "value2");
        assertThat(cache1.get("key1")).isNotNull();
        assertThat(cache2.get("key2")).isNotNull();

        dispatcher.disableCacheManager("cacheManager1");

        cache1.put("key3", "value3");
        cache2.put("key4", "value4");

        assertThat(cache1.get("key3")).isNull();
        assertThat(cache2.get("key4")).isNotNull();
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
        assertThatThrownBy(() -> dispatcher.enableCache("nonExistentManager", "cache1"))
                .isInstanceOf(CacheManagerAdapterNotFoundException.class)
                .hasMessageContaining("nonExistentManager");
    }

    @Test
    void disableCache_shouldThrowExceptionForNonExistentManager() {
        assertThatThrownBy(() -> dispatcher.disableCache("nonExistentManager", "cache1"))
                .isInstanceOf(CacheManagerAdapterNotFoundException.class)
                .hasMessageContaining("nonExistentManager");
    }

    @Test
    void enableCache_shouldWorkWhenCacheDoesNotExist() {
        assertThatNoException().isThrownBy(() -> dispatcher.enableCache("cacheManager1", "nonExistentCache"));
    }

    @Test
    void disableCache_shouldWorkWhenCacheDoesNotExist() {
        assertThatNoException().isThrownBy(() -> dispatcher.disableCache("cacheManager1", "nonExistentCache"));
    }
}
