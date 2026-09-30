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
import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link CacheManagerBeanPostProcessor}.
 *
 * @author Nikita Kirillov
 */
class CacheManagerBeanPostProcessorTest {

    private final CacheManagerBeanPostProcessor subject = new CacheManagerBeanPostProcessor(noopProvider());

    @Test
    void shouldWrapCacheManagerWithEnhancedCacheManagerProxy() {
        CacheManager cacheManager = new ConcurrentMapCacheManager();

        Object result = subject.postProcessAfterInitialization(cacheManager, "cacheManager");

        assertThat(AopUtils.isCglibProxy(result)).isTrue();
        assertThat(result).isInstanceOf(EnhancedCacheManager.class);
    }

    @Test
    void shouldNotWrapNonCacheManagerBean() {
        Object nonCacheManagerBean = new Object();

        Object result = subject.postProcessAfterInitialization(nonCacheManagerBean, "someBean");

        assertThat(result).isSameAs(nonCacheManagerBean);
    }

    @Test
    void shouldNotWrapAlreadyEnhancedCacheManager() {
        EnhancedCacheManager alreadyEnhanced =
                new DefaultEnhancedCacheManager("cacheManager", new ConcurrentMapCacheManager(), null);

        Object result = subject.postProcessAfterInitialization(alreadyEnhanced, "cacheManager");

        assertThat(result).isSameAs(alreadyEnhanced);
    }

    @Test
    void shouldNotWrapCacheManagerWithFinalMethod() {
        CacheManager finalMethodCacheManager = new FinalMethodCacheManager();

        Object result = subject.postProcessAfterInitialization(finalMethodCacheManager, "finalMethodCacheManager");

        assertThat(result).isSameAs(finalMethodCacheManager);
        assertThat(AopUtils.isAopProxy(result)).isFalse();
    }

    private static ObjectProvider<AxelixMetricsPublisher> noopProvider() {
        return new ObjectProvider<>() {
            @Override
            public AxelixMetricsPublisher getObject() {
                return null;
            }

            @Override
            public AxelixMetricsPublisher getObject(Object... args) {
                return null;
            }

            @Override
            public AxelixMetricsPublisher getIfAvailable() {
                return null;
            }

            @Override
            public AxelixMetricsPublisher getIfUnique() {
                return null;
            }
        };
    }

    static class FinalMethodCacheManager implements CacheManager {
        @Override
        public final Cache getCache(String name) {
            return null;
        }

        @Override
        public Collection<String> getCacheNames() {
            return Collections.emptyList();
        }
    }
}
