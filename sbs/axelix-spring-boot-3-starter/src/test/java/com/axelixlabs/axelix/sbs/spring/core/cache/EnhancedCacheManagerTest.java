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
package com.axelixlabs.axelix.sbs.spring.core.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricNames;
import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;
import com.axelixlabs.axelix.sbs.spring.core.metrics.DefaultAxelixMetricsPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        AxelixMetricsPublisher axelixMetricsPublisher = new DefaultAxelixMetricsPublisher(new SimpleMeterRegistry());
        subject = new DefaultEnhancedCacheManager("testCacheManagerBeanName", cacheManager, axelixMetricsPublisher);
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
        assertThat(cache.get(keyToKeep))
                .isNotNull()
                .satisfies(cacheValue -> assertThat(cacheValue.get()).isEqualTo("value2"));
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
        EnhancedCache cache = subject.getCache(cacheName);

        // then.
        assertThat(cache).isNotNull();
        assertThat(cache.isEnabled()).isTrue();
    }

    @Test
    void isCacheEnabled_shouldReturnFalseForDisabledCacheInEnhancedCacheManager() {
        // given.
        String cacheName = "cache";
        EnhancedCache cache = subject.getCache(cacheName);

        // when.
        subject.disable(cacheName);

        // then.
        assertThat(cache).isNotNull();
        assertThat(cache.isEnabled()).isFalse();
    }

    @Test
    void isCacheEnabled_shouldReturnTrueAfterEnableDisableCacheInEnhancedCacheManager() {
        // given.
        String cacheName = "cache";

        // when.
        subject.disable(cacheName);
        EnhancedCache cache = subject.getCache(cacheName);

        // then.
        assertThat(cache.isEnabled()).isFalse();

        // and then.
        subject.enable(cacheName);
        assertThat(cache.isEnabled()).isTrue();
    }

    @Test
    void isCacheEnabled_shouldWorkWithMultipleCachesInEnhancedCacheManager() {
        // given.
        EnhancedCache cache1 = subject.getCache("cache1");
        EnhancedCache cache2 = subject.getCache("cache2");
        EnhancedCache cache3 = subject.getCache("cache3");

        // when.
        subject.disable("cache2");

        // then.
        assertThat(cache1.isEnabled()).isTrue();
        assertThat(cache2.isEnabled()).isFalse();
        assertThat(cache3.isEnabled()).isTrue();
    }

    @Test
    void getCache_shouldResolveCurrentDelegateWithoutChangingEarlierWrappers() {
        // given.
        SwitchingCacheManager underlyingCacheManager = new SwitchingCacheManager();
        EnhancedCacheManager enhancedCacheManager =
                new DefaultEnhancedCacheManager("test", underlyingCacheManager, null);
        EnhancedCache originalCacheWrapper = enhancedCacheManager.getCache("cache");
        Cache originalCache = underlyingCacheManager.currentCache;
        underlyingCacheManager.currentCache = new ConcurrentMapCache("cache");

        // when.
        EnhancedCache replacementCacheWrapper = enhancedCacheManager.getCache("cache");
        replacementCacheWrapper.put("key", "current");
        originalCacheWrapper.put("key", "original");

        // then.
        assertThat(replacementCacheWrapper.getNativeCache())
                .isSameAs(underlyingCacheManager.currentCache.getNativeCache());
        assertThat(originalCacheWrapper.getNativeCache()).isSameAs(originalCache.getNativeCache());
        assertThat(underlyingCacheManager.currentCache.get("key", String.class)).isEqualTo("current");
        assertThat(originalCache.get("key", String.class)).isEqualTo("original");
    }

    @Test
    void getCache_shouldShareManagementStateAcrossDelegates() {
        // given.
        SwitchingCacheManager underlyingCacheManager = new SwitchingCacheManager();
        EnhancedCacheManager enhancedCacheManager =
                new DefaultEnhancedCacheManager("test", underlyingCacheManager, null);
        EnhancedCache originalCacheWrapper = enhancedCacheManager.getCache("cache");
        originalCacheWrapper.get("missing");
        enhancedCacheManager.disable("cache");
        underlyingCacheManager.currentCache = new ConcurrentMapCache("cache");

        // when.
        EnhancedCache replacementCacheWrapper = enhancedCacheManager.getCache("cache");
        replacementCacheWrapper.put("key", "value");

        // then.
        assertThat(replacementCacheWrapper.isEnabled()).isFalse();
        assertThat(replacementCacheWrapper.getCacheLookups())
                .containsExactlyElementsOf(originalCacheWrapper.getCacheLookups())
                .hasSize(1);
        assertThat(underlyingCacheManager.currentCache.get("key")).isNull();
    }

    @Test
    void clearAll_shouldClearCurrentDelegate() {
        // given.
        SwitchingCacheManager underlyingCacheManager = new SwitchingCacheManager();
        EnhancedCacheManager enhancedCacheManager =
                new DefaultEnhancedCacheManager("test", underlyingCacheManager, null);
        enhancedCacheManager.getCache("cache").put("key", "original");
        Cache originalCache = underlyingCacheManager.currentCache;
        underlyingCacheManager.currentCache = new ConcurrentMapCache("cache");
        underlyingCacheManager.currentCache.put("key", "current");

        // when.
        enhancedCacheManager.clearAll();

        // then.
        assertThat(underlyingCacheManager.currentCache.get("key")).isNull();
        assertThat(originalCache.get("key", String.class)).isEqualTo("original");
    }

    @Test
    void getAll_shouldResolveCurrentDelegate() {
        // given.
        SwitchingCacheManager underlyingCacheManager = new SwitchingCacheManager();
        EnhancedCacheManager enhancedCacheManager =
                new DefaultEnhancedCacheManager("test", underlyingCacheManager, null);
        enhancedCacheManager.getCache("cache");
        underlyingCacheManager.currentCache = new ConcurrentMapCache("cache");

        // when.
        Collection<EnhancedCache> resolvedCacheWrappers = enhancedCacheManager.getAll();

        // then.
        assertThat(resolvedCacheWrappers)
                .singleElement()
                .satisfies(cacheWrapper -> assertThat(cacheWrapper.getNativeCache())
                        .isSameAs(underlyingCacheManager.currentCache.getNativeCache()));
    }

    @Test
    void getAll_shouldNotRecreateRemovedCache() {
        // given.
        Map<String, Cache> underlyingCaches = new HashMap<>();
        CacheManager underlyingCacheManager = mock(CacheManager.class);
        when(underlyingCacheManager.getCacheNames()).thenAnswer(invocation -> underlyingCaches.keySet());
        when(underlyingCacheManager.getCache("cache"))
                .thenAnswer(invocation -> underlyingCaches.computeIfAbsent("cache", ConcurrentMapCache::new));
        EnhancedCacheManager enhancedCacheManager =
                new DefaultEnhancedCacheManager("test", underlyingCacheManager, null);
        enhancedCacheManager.getCache("cache");
        underlyingCaches.remove("cache");

        // when.
        Collection<EnhancedCache> resolvedCacheWrappers = enhancedCacheManager.getAll();

        // then.
        assertThat(resolvedCacheWrappers).isEmpty();
        assertThat(underlyingCacheManager.getCacheNames()).isEmpty();
    }

    @Test
    void getCache_shouldPublishLookupsForCachesPresentAtStartup() {
        // given.
        ConcurrentMapCacheManager underlyingCacheManager = new ConcurrentMapCacheManager();
        underlyingCacheManager.getCache("startupCache").put("key", "value");
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        EnhancedCacheManager enhancedCacheManager = new DefaultEnhancedCacheManager(
                "test", underlyingCacheManager, new DefaultAxelixMetricsPublisher(meterRegistry));

        // when.
        EnhancedCache startupCache = enhancedCacheManager.getCache("startupCache");
        startupCache.get("key");
        startupCache.get("missing");

        // then.
        assertThat(meterRegistry
                        .get(AxelixMetricNames.CACHE_REQUESTS)
                        .tags("cache", "startupCache", "result", "hit")
                        .counter()
                        .count())
                .isEqualTo(1.0);
        assertThat(meterRegistry
                        .get(AxelixMetricNames.CACHE_REQUESTS)
                        .tags("cache", "startupCache", "result", "miss")
                        .counter()
                        .count())
                .isEqualTo(1.0);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void cacheStatusGauge_shouldTrackCachesPresentAtStartup(boolean enabled) {
        // given.
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        EnhancedCacheManager enhancedCacheManager = new DefaultEnhancedCacheManager(
                "test",
                new ConcurrentMapCacheManager("startupCache"),
                new DefaultAxelixMetricsPublisher(meterRegistry));

        // when.
        if (!enabled) {
            enhancedCacheManager.disable("startupCache");
        }

        // then.
        assertThat(meterRegistry
                        .get(AxelixMetricNames.CACHE_ENABLED)
                        .tag("cache", "startupCache")
                        .gauge()
                        .value())
                .isEqualTo(enabled ? 1.0 : 0.0);
    }

    @Test
    void getCache_shouldKeepHistoryWithoutPublishingMetricsForCachesCreatedAfterStartup() {
        // given.
        ConcurrentMapCacheManager underlyingCacheManager = new ConcurrentMapCacheManager();
        underlyingCacheManager.getCache("startupCache");
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        EnhancedCacheManager enhancedCacheManager = new DefaultEnhancedCacheManager(
                "test", underlyingCacheManager, new DefaultAxelixMetricsPublisher(meterRegistry));

        // when.
        EnhancedCache dynamicCache = enhancedCacheManager.getCache("dynamicCache");
        dynamicCache.put("key", "value");
        dynamicCache.get("key");
        dynamicCache.get("missing");
        enhancedCacheManager.disable("dynamicCache");
        enhancedCacheManager.enable("dynamicCache");

        // then.
        assertThat(dynamicCache.getCacheLookups()).hasSize(2);
        assertThat(dynamicCache.isEnabled()).isTrue();
        assertThat(meterRegistry
                        .find(AxelixMetricNames.CACHE_REQUESTS)
                        .tag("cache", "dynamicCache")
                        .meters())
                .isEmpty();
        assertThat(meterRegistry
                        .find(AxelixMetricNames.CACHE_ENABLED)
                        .tag("cache", "dynamicCache")
                        .gauge())
                .isNull();
        assertThat(meterRegistry
                        .find(AxelixMetricNames.CACHE_ENABLED)
                        .tag("cache", "startupCache")
                        .gauge())
                .isNotNull();
    }

    private static class SwitchingCacheManager implements CacheManager {
        private Cache currentCache = new ConcurrentMapCache("cache");

        @Override
        public Cache getCache(String name) {
            return currentCache;
        }

        @Override
        public Collection<String> getCacheNames() {
            return List.of("cache");
        }
    }
}
