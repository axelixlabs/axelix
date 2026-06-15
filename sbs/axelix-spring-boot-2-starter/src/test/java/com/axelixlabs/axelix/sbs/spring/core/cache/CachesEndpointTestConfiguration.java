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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration for {@link AxelixCachesEndpointTest}, part of the shared endpoint test context.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
@TestConfiguration
@Import({AxelixCachesEndpoint.class, DefaultCacheOperationsDispatcher.class})
public class CachesEndpointTestConfiguration {

    // Cache names under test
    public static final String TEST_CACHE_1 = "cache1";
    public static final String TEST_CACHE_2 = "cache2";

    public static final String MAIN_CACHE_MANAGER = "mainCacheManager";
    public static final String CLEAR_CACHE_MANAGER = "clearCacheManager";
    public static final String ENABLE_CACHE_MANAGER = "enableCacheManager";
    public static final String DISABLE_CACHE_MANAGER = "disableCacheManager";

    @Bean
    @ConditionalOnMissingBean
    public CacheSizeProvider cacheSizeProvider() {
        return new DefaultCacheSizeProvider();
    }

    @Bean
    public static CacheManagerBeanPostProcessor cacheManagerBeanPostProcessor() {
        return new CacheManagerBeanPostProcessor();
    }

    @Bean(name = MAIN_CACHE_MANAGER)
    @Primary
    public CacheManager testSubjectCacheManager() {
        return new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2);
    }

    @Bean(name = CLEAR_CACHE_MANAGER)
    public CacheManager clearSubjectCacheManager() {
        return new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2);
    }

    @Bean(name = ENABLE_CACHE_MANAGER)
    public CacheManager enableSubjectCacheManager() {
        return new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2);
    }

    @Bean(name = DISABLE_CACHE_MANAGER)
    public CacheManager disableSubjectCacheManager() {
        return new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2);
    }
}
