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
import io.micrometer.core.instrument.Gauge;
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

import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;
import com.axelixlabs.axelix.sbs.spring.core.metrics.DefaultAxelixMetricsPublisher;

import static com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricNames.CACHE_ENABLED;
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

    private EnhancedCache enhancedCache;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        axelixMetricsPublisher = new DefaultAxelixMetricsPublisher(meterRegistry);

        Mockito.lenient().when(delegate.getName()).thenReturn(CACHE_NAME);
        enhancedCache = new DefaultEnhancedCache(delegate, axelixMetricsPublisher);
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
            checkMeterRegistryCacheStatusGauge(isEnabled);
        }

        @Test
        void shouldDisableCache() {
            // given.
            assertThat(enhancedCache.isEnabled()).isTrue();
            checkMeterRegistryCacheStatusGauge(true);

            // when.
            enhancedCache.disable();

            // then.
            assertThat(enhancedCache.isEnabled()).isFalse();
            checkMeterRegistryCacheStatusGauge(false);
        }

        @Test
        void shouldReEnableCache() {
            // given.
            enhancedCache.disable();
            assertThat(enhancedCache.isEnabled()).isFalse();
            checkMeterRegistryCacheStatusGauge(false);

            // when.
            enhancedCache.enable();

            // then.
            assertThat(enhancedCache.isEnabled()).isTrue();
            checkMeterRegistryCacheStatusGauge(true);
        }

        @Test
        void shouldBeNoOpWhenCacheIsAlreadyEnabled() {
            // given.
            assertThat(enhancedCache.isEnabled()).isTrue();
            checkMeterRegistryCacheStatusGauge(true);

            // when.
            enhancedCache.enable();

            // then.
            assertThat(enhancedCache.isEnabled()).isTrue();
            checkMeterRegistryCacheStatusGauge(true);
        }

        @Test
        void shouldBeNoOpWhenCacheIsAlreadyDisabled() {
            // given.
            enhancedCache.disable();
            assertThat(enhancedCache.isEnabled()).isFalse();
            checkMeterRegistryCacheStatusGauge(false);

            // when.
            enhancedCache.disable();

            // then.
            assertThat(enhancedCache.isEnabled()).isFalse();
            checkMeterRegistryCacheStatusGauge(false);
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
        void shouldReturnNullWhenCacheIsDisabled() {
            // given.
            enhancedCache.disable();

            // when.
            Cache.ValueWrapper result = enhancedCache.get(KEY);

            // then.
            assertThat(result).isNull();
            verify(delegate, never()).get(any());
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
        void shouldReturnNullWhenCacheIsDisabled() {
            // given.
            enhancedCache.disable();

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
        void shouldReturnNullWhenCacheIsDisabled() {
            // given.
            enhancedCache.disable();

            // when.
            String result = enhancedCache.get(KEY, () -> VALUE);

            // then.
            assertThat(result).isNull();
            verify(delegate, never()).get(any(), any(Callable.class));
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
        void shouldNotDelegateWhenCacheIsDisabled() {
            // given.
            enhancedCache.disable();

            // when.
            enhancedCache.put(KEY, VALUE);

            // then.
            verify(delegate, never()).put(any(), any());
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
        void shouldNotDelegateWhenCacheIsDisabled() {
            // given.
            enhancedCache.disable();

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
        void shouldNotDelegateWhenCacheIsDisabled() {
            // given.
            enhancedCache.disable();

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
        void shouldReturnFalseWhenCacheIsDisabled() {
            // given.
            enhancedCache.disable();

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
        void shouldReturnFalseWhenCacheIsDisabled() {
            // given.
            enhancedCache.disable();

            // when.
            boolean result = enhancedCache.evictIfPresent(KEY);

            // then.
            assertThat(result).isFalse();
            verify(delegate, never()).evictIfPresent(any());
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
        void shouldNotTrackWhenCacheIsDisabled() {
            // given.
            enhancedCache.disable();

            // when.
            enhancedCache.get(KEY);

            // then.
            assertThat(enhancedCache.getCacheLookups()).isEmpty();
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
    }

    private void checkMeterRegistryCacheStatusGauge(boolean cacheEnabled) {
        Gauge gauge = meterRegistry.find(CACHE_ENABLED).tag("cache", CACHE_NAME).gauge();

        assertThat(gauge).isNotNull();
        // The value is returned as a double (1.0 for enabled, 0.0 for disabled)
        assertThat(gauge.value()).isEqualTo(cacheEnabled ? 1.0 : 0.0);
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
