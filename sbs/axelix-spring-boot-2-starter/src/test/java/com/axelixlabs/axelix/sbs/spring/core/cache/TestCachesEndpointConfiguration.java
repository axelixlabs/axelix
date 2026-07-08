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

import java.util.Map;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;

/**
 * Test-only configuration that wires the {@link AxelixCachesEndpoint} without importing production auto-configuration.
 *
 * @author Sergey Cherkasov
 */
@TestConfiguration
public class TestCachesEndpointConfiguration {

    @Bean
    CacheSizeProvider cacheSizeProvider() {
        return new DefaultCacheSizeProvider();
    }

    @Bean
    CacheOperationsDispatcher cacheOperationsDispatcher(
            Map<String, CacheManager> managerMap, CacheSizeProvider cacheSizeProvider) {
        return new DefaultCacheOperationsDispatcher(managerMap, cacheSizeProvider);
    }

    @Bean
    AxelixCachesEndpoint axelixCachesEndpoint(CacheOperationsDispatcher dispatcher) {
        return new AxelixCachesEndpoint(dispatcher);
    }

    @Bean
    CacheManagerBeanPostProcessor cacheManagerBeanPostProcessor() {
        return new CacheManagerBeanPostProcessor();
    }
}
