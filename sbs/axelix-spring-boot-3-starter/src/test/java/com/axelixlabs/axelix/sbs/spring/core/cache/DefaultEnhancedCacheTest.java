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

import java.util.concurrent.Callable;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.axelixlabs.axelix.sbs.spring.core.SlidingWindow;
import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;
import com.axelixlabs.axelix.sbs.spring.core.metrics.DefaultAxelixMetricsPublisher;

import static com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricNames.CACHE_REQUESTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultEnhancedCache}.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@ExtendWith(MockitoExtension.class)
class DefaultEnhancedCacheTest {

    private static final String KEY = "testKey";
    private static final String VALUE = "testValue";
    private static final String CACHE_NAME = "testCacheName";

    private MeterRegistry meterRegistry;
    private AxelixMetricsPublisher axelixMetricsPublisher;

    @Mock
    private Cache delegate;

    @Mock
    private EnhancedCacheManager cacheManager;

    private EnhancedCache enhancedCache;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        axelixMetricsPublisher = new DefaultAxelixMetricsPublisher(meterRegistry);

        Mockito.lenient().when(delegate.getName()).thenReturn(CACHE_NAME);
        Mockito.lenient().when(cacheManager.isEnabled(CACHE_NAME)).thenReturn(true);
        SlidingWindow<CacheLookup> lookupHistory = new SlidingWindow<>(200);
        Mockito.lenient().when(cacheManager.getLookupHistory(CACHE_NAME)).thenReturn(lookupHistory);
        enhancedCache = new DefaultEnhancedCache(delegate, axelixMetricsPublisher, cacheManager);
    }

    @Test
    void getByKey_shouldUseCurrentHistoryAfterHistoryReplacement() {
        // given.
        SlidingWindow<CacheLookup> originalHistory = new SlidingWindow<>(200);
        SlidingWindow<CacheLookup> replacementHistory = new SlidingWindow<>(200);
        when(cacheManager.getLookupHistory(CACHE_NAME)).thenReturn(originalHistory, replacementHistory);
        EnhancedCache cache = new DefaultEnhancedCache(delegate, null, cacheManager);
        cache.get(KEY);

        // when.
        cache.get(KEY);

        // then.
        assertThat(originalHistory.get()).hasSize(1);
        assertThat(replacementHistory.get()).hasSize(1);
        assertThat(cache.getCacheLookups()).containsExactlyElementsOf(replacementHistory.get());
    }

    @Nested
    class EnableDisable {

        @BeforeEach
        void setUp() {
            EnhancedCacheManager enhancedCacheManager = new DefaultEnhancedCacheManager(
                    "cacheManager", new ConcurrentMapCacheManager(CACHE_NAME), axelixMetricsPublisher);
            enhancedCache = enhancedCacheManager.getCache(CACHE_NAME);
        }

        @Test
        void shouldBeEnabledByDefault() {
            // given.
            // cache is created in setUp

            // when.
            boolean isEnabled = enhancedCache.isEnabled();

            // then.
            assertThat(isEnabled).isTrue();
        }
    }

    @Nested
    class SimpleGet {

        @Test
        void shouldReturnValueWhenCacheIsEnabled() {
            // given.
            when(delegate.get(KEY)).thenReturn(() -> VALUE);

            // when.
            Cache.ValueWrapper result = enhancedCache.get(KEY);

            // then.
            assertThat(result).isNotNull();
            assertThat(result.get()).isEqualTo(VALUE);
            verify(delegate).get(KEY);
        }

        @Test
        void shouldTrackHitWhenValueIsFound() {
            // given.
            when(delegate.get(KEY)).thenReturn(() -> VALUE);

            // when.
            enhancedCache.get(KEY);

            // then.
            assertThat(enhancedCache.getCacheLookups()).hasSize(1);
            assertThat(enhancedCache.getCacheLookups().get(0).outcome()).isEqualTo(CacheLookup.Outcome.HIT);
        }

        @Test
        void shouldTrackMissWhenValueIsNotFound() {
            // given.
            when(delegate.get(KEY)).thenReturn(null);

            // when.
            enhancedCache.get(KEY);

            // then.
            assertThat(enhancedCache.getCacheLookups()).hasSize(1);
            assertThat(enhancedCache.getCacheLookups().get(0).outcome()).isEqualTo(CacheLookup.Outcome.MISS);
        }

        @Test
        void getByKey_shouldReturnNullWhenCacheIsDisabled() {
            // given.
            when(cacheManager.isEnabled(CACHE_NAME)).thenReturn(false);

            // when.
            Cache.ValueWrapper result = enhancedCache.get(KEY);

            // then.
            assertThat(result).isNull();
            verify(delegate, never()).get(any());
        }
    }

    @Nested
    class GetWithType {

        @Test
        void shouldReturnValueWhenCacheIsEnabled() {
            // given.
            when(delegate.get(KEY, String.class)).thenReturn(VALUE);

            // when.
            String result = enhancedCache.get(KEY, String.class);

            // then.
            assertThat(result).isEqualTo(VALUE);
            verify(delegate).get(KEY, String.class);
        }

        @Test
        void getWithType_shouldReturnNullWhenCacheIsDisabled() {
            // given.
            when(cacheManager.isEnabled(CACHE_NAME)).thenReturn(false);

            // when.
            String result = enhancedCache.get(KEY, String.class);

            // then.
            assertThat(result).isNull();
            verify(delegate, never()).get(any(), any(Class.class));
        }
    }

    @Nested
    class GetWithValueLoader {

        @Test
        void getWithValueLoader_shouldReturnValueWhenCacheIsEnabled() {
            // given.
            when(delegate.get(eq(KEY), any(Callable.class))).thenAnswer(invocation -> {
                Callable<String> loader = invocation.getArgument(1);
                return loader.call();
            });

            // when.
            String result = enhancedCache.get(KEY, () -> VALUE);

            // then.
            assertThat(result).isEqualTo(VALUE);
            verify(delegate).get(eq(KEY), any(Callable.class));
        }

        @Test
        void shouldTrackHitWhenValueExistsInCache() {
            // given.
            when(delegate.get(eq(KEY), any(Callable.class))).thenReturn(VALUE);

            // when.
            enhancedCache.get(KEY, () -> VALUE);

            // then.
            assertThat(enhancedCache.getCacheLookups()).hasSize(1);
            assertThat(enhancedCache.getCacheLookups().get(0).outcome()).isEqualTo(CacheLookup.Outcome.HIT);
        }

        @Test
        void shouldTrackMissWhenValueLoaderIsCalled() {
            // given.
            when(delegate.get(eq(KEY), any(Callable.class))).thenAnswer(invocation -> {
                Callable<String> loader = invocation.getArgument(1);
                return loader.call();
            });

            // when.
            enhancedCache.get(KEY, () -> VALUE);

            // then.
            assertThat(enhancedCache.getCacheLookups()).hasSize(1);
            assertThat(enhancedCache.getCacheLookups().get(0).outcome()).isEqualTo(CacheLookup.Outcome.MISS);
        }

        @Test
        void getWithValueLoader_shouldReturnNullWhenCacheIsDisabled() {
            // given.
            when(cacheManager.isEnabled(CACHE_NAME)).thenReturn(false);

            // when.
            String result = enhancedCache.get(KEY, () -> VALUE);

            // then.
            assertThat(result).isNull();
            verify(delegate, never()).get(any(), any(Callable.class));
        }
    }

    @Nested
    class Put {

        @Test
        void shouldDelegateToUnderlyingCacheWhenEnabled() {
            // given.
            // cache is enabled by default

            // when.
            enhancedCache.put(KEY, VALUE);

            // then.
            verify(delegate).put(KEY, VALUE);
        }

        @Test
        void shouldHandleNullValue() {
            // given.
            // cache is enabled by default

            // when.
            enhancedCache.put(KEY, null);

            // then.
            verify(delegate).put(KEY, null);
        }

        @Test
        void put_shouldNotDelegateWhenCacheIsDisabled() {
            // given.
            when(cacheManager.isEnabled(CACHE_NAME)).thenReturn(false);

            // when.
            enhancedCache.put(KEY, VALUE);

            // then.
            verify(delegate, never()).put(any(), any());
        }
    }

    @Nested
    class Evict {

        @Test
        void shouldDelegateToUnderlyingCacheWhenEnabled() {
            // given.
            // cache is enabled by default

            // when.
            enhancedCache.evict(KEY);

            // then.
            verify(delegate).evict(KEY);
        }

        @Test
        void evict_shouldNotDelegateWhenCacheIsDisabled() {
            // given.
            when(cacheManager.isEnabled(CACHE_NAME)).thenReturn(false);

            // when.
            enhancedCache.evict(KEY);

            // then.
            verify(delegate, never()).evict(any());
        }
    }

    @Nested
    class Clear {

        @Test
        void shouldDelegateToUnderlyingCacheWhenEnabled() {
            // given.
            // cache is enabled by default

            // when.
            enhancedCache.clear();

            // then.
            verify(delegate).clear();
        }

        @Test
        void clear_shouldNotDelegateWhenCacheIsDisabled() {
            // given.
            when(cacheManager.isEnabled(CACHE_NAME)).thenReturn(false);

            // when.
            enhancedCache.clear();

            // then.
            verify(delegate, never()).clear();
        }
    }

    @Nested
    class Invalidate {

        @Test
        void shouldDelegateToUnderlyingCacheWhenEnabled() {
            // given.
            when(delegate.invalidate()).thenReturn(true);

            // when.
            boolean result = enhancedCache.invalidate();

            // then.
            assertThat(result).isTrue();
            verify(delegate).invalidate();
        }

        @Test
        void invalidate_shouldReturnFalseWhenCacheIsDisabled() {
            // given.
            when(cacheManager.isEnabled(CACHE_NAME)).thenReturn(false);

            // when.
            boolean result = enhancedCache.invalidate();

            // then.
            assertThat(result).isFalse();
            verify(delegate, never()).invalidate();
        }
    }

    @Nested
    class EvictIfPresent {

        @Test
        void shouldDelegateToUnderlyingCacheWhenEnabled() {
            // given.
            when(delegate.evictIfPresent(KEY)).thenReturn(true);

            // when.
            boolean result = enhancedCache.evictIfPresent(KEY);

            // then.
            assertThat(result).isTrue();
            verify(delegate).evictIfPresent(KEY);
        }

        @Test
        void shouldReturnFalseWhenDelegateReturnsFalse() {
            // given.
            when(delegate.evictIfPresent(KEY)).thenReturn(false);

            // when.
            boolean result = enhancedCache.evictIfPresent(KEY);

            // then.
            assertThat(result).isFalse();
            verify(delegate).evictIfPresent(KEY);
        }

        @Test
        void evictIfPresent_shouldReturnFalseWhenCacheIsDisabled() {
            // given.
            when(cacheManager.isEnabled(CACHE_NAME)).thenReturn(false);

            // when.
            boolean result = enhancedCache.evictIfPresent(KEY);

            // then.
            assertThat(result).isFalse();
            verify(delegate, never()).evictIfPresent(any());
        }
    }

    @Nested
    class HitsMisses {

        @Test
        void shouldReturnOnlyHits() {
            // given.
            Cache.ValueWrapper hitWrapper = () -> VALUE;
            when(delegate.get("key1")).thenReturn(hitWrapper);
            when(delegate.get("key2")).thenReturn(hitWrapper);
            when(delegate.get("key3")).thenReturn(hitWrapper);

            // when.
            enhancedCache.get("key1");
            enhancedCache.get("key2");
            enhancedCache.get("key3");

            // then.
            assertThat(enhancedCache.getCacheLookups()).hasSize(3);
            assertThat(enhancedCache.getCacheLookups())
                    .extracting(CacheLookup::outcome)
                    .containsOnly(CacheLookup.Outcome.HIT);
            // and then.
            checkMeterRegistryCacheLookup(3d, 0d);
        }

        @Test
        void getMisses_shouldReturnOnlyMisses() {
            // given.
            when(delegate.get("key1")).thenReturn(null);
            when(delegate.get("key2")).thenReturn(null);
            when(delegate.get("key3")).thenReturn(null);

            // when.
            enhancedCache.get("key1");
            enhancedCache.get("key2");
            enhancedCache.get("key3");

            // then.
            assertThat(enhancedCache.getCacheLookups()).hasSize(3);
            assertThat(enhancedCache.getCacheLookups())
                    .extracting(CacheLookup::outcome)
                    .containsOnly(CacheLookup.Outcome.MISS);
            // and then.
            checkMeterRegistryCacheLookup(0d, 3d);
        }

        @Test
        void multipleOperations_shouldTrackHitsAndMissesCorrectly() {
            // given.
            Cache.ValueWrapper hitWrapper = () -> VALUE;
            when(delegate.get("hit1")).thenReturn(hitWrapper);
            when(delegate.get("miss1")).thenReturn(null);
            when(delegate.get("hit2")).thenReturn(hitWrapper);
            when(delegate.get("miss2")).thenReturn(null);
            when(delegate.get("hit3")).thenReturn(hitWrapper);

            // when.
            enhancedCache.get("hit1");
            enhancedCache.get("miss1");
            enhancedCache.get("hit2");
            enhancedCache.get("miss2");
            enhancedCache.get("hit3");

            // then.
            assertThat(enhancedCache.getCacheLookups()).hasSize(5);
            assertThat(enhancedCache.getCacheLookups())
                    .extracting(CacheLookup::outcome)
                    .containsExactly(
                            CacheLookup.Outcome.HIT,
                            CacheLookup.Outcome.MISS,
                            CacheLookup.Outcome.HIT,
                            CacheLookup.Outcome.MISS,
                            CacheLookup.Outcome.HIT);
            // and then.
            checkMeterRegistryCacheLookup(3d, 2d);
        }

        @Test
        void getByKey_shouldNotTrackWhenCacheIsDisabled() {
            // given.
            when(cacheManager.isEnabled(CACHE_NAME)).thenReturn(false);

            // when.
            enhancedCache.get(KEY);

            // then.
            assertThat(enhancedCache.getCacheLookups()).isEmpty();
        }
    }

    private void checkMeterRegistryCacheLookup(double expectedHits, double expectedMisses) {
        // 1. Verify Cache Hits
        Counter cacheHitsCounter = meterRegistry
                .find(CACHE_REQUESTS)
                .tag("cache", CACHE_NAME)
                .tag("result", CacheLookup.Outcome.HIT.getValue())
                .counter();

        if (expectedHits != 0.0) {
            assertThat(cacheHitsCounter).isNotNull();
            assertThat(cacheHitsCounter.count()).isEqualTo(expectedHits);
        }

        // 2. Verify Cache Misses
        Counter cacheMissesCounter = meterRegistry
                .find(CACHE_REQUESTS)
                .tag("cache", CACHE_NAME)
                .tag("result", CacheLookup.Outcome.MISS.getValue())
                .counter();

        if (expectedMisses != 0.0) {
            assertThat(cacheMissesCounter).isNotNull();
            assertThat(cacheMissesCounter.count()).isEqualTo(expectedMisses);
        }
    }
}
