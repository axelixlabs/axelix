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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.core.cache.AxelixCachesEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.cache.CacheManagerBeanPostProcessor;
import com.axelixlabs.axelix.sbs.spring.core.cache.CacheOperationsDispatcher;
import com.axelixlabs.axelix.sbs.spring.core.cache.CacheSizeProvider;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link AxelixCachesEndpointAutoConfiguration}
 *
 * @since 09.02.2026
 * @author Nikita Kirillov
 */
class AxelixCachesEndpointAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-caches")
            .withConfiguration(AutoConfigurations.of(AxelixCachesEndpointAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(CacheSizeProvider.class);
            assertThat(context).hasSingleBean(CacheOperationsDispatcher.class);
            assertThat(context).hasSingleBean(AxelixCachesEndpoint.class);
            assertThat(context).hasSingleBean(CacheManagerBeanPostProcessor.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfigurationWhenEndpointDisabled() {
        contextRunner // Overriding the property value to test the disabled state
                .withPropertyValues("management.endpoints.web.exposure.exclude=axelix-caches")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixCachesEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(CacheSizeProvider.class);
                    assertThat(context).doesNotHaveBean(CacheOperationsDispatcher.class);
                    assertThat(context).doesNotHaveBean(AxelixCachesEndpoint.class);
                    assertThat(context).doesNotHaveBean(CacheManagerBeanPostProcessor.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutRequiredProperty() {
        ApplicationContextRunner runnerWithoutCacheConfig = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AxelixCachesEndpointAutoConfiguration.class));

        runnerWithoutCacheConfig.run(context -> {
            assertThat(context).doesNotHaveBean(AxelixCachesEndpointAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(CacheSizeProvider.class);
            assertThat(context).doesNotHaveBean(CacheOperationsDispatcher.class);
            assertThat(context).doesNotHaveBean(AxelixCachesEndpoint.class);
            assertThat(context).doesNotHaveBean(CacheManagerBeanPostProcessor.class);
        });
    }

    @Test
    void shouldHandleMultipleCustomBeans() {
        contextRunner
                .withUserConfiguration(
                        CustomCacheManagerBeanPostProcessorConfig.class,
                        CustomAxelixCachesEndpointConfig.class,
                        CustomCacheSizeProviderConfig.class)
                .run(context -> {
                    assertThat(context.getBean(CacheManagerBeanPostProcessor.class))
                            .isExactlyInstanceOf(CustomCacheManagerBeanPostProcessor.class);
                    assertThat(context.getBean(AxelixCachesEndpoint.class))
                            .isExactlyInstanceOf(CustomAxelixCachesEndpoint.class);
                    assertThat(context.getBean(CacheSizeProvider.class))
                            .isExactlyInstanceOf(CustomCacheSizeProvider.class);
                });
    }

    @TestConfiguration
    static class CustomCacheSizeProviderConfig {
        @Bean
        public CacheSizeProvider cacheSizeProvider() {
            return new CustomCacheSizeProvider();
        }
    }

    @TestConfiguration
    static class CustomAxelixCachesEndpointConfig {
        @Bean
        public AxelixCachesEndpoint axelixCachesEndpoint(CacheOperationsDispatcher cacheOperationsDispatcher) {
            return new CustomAxelixCachesEndpoint(cacheOperationsDispatcher);
        }
    }

    @TestConfiguration
    static class CustomCacheManagerBeanPostProcessorConfig {
        @Bean
        public CacheManagerBeanPostProcessor cacheManagerBeanPostProcessor() {
            return new CustomCacheManagerBeanPostProcessor();
        }
    }

    static class CustomCacheSizeProvider implements CacheSizeProvider {

        @Override
        public Long getEstimatedCacheSize(Object nativeCache) {
            return 0L;
        }
    }

    static class CustomAxelixCachesEndpoint extends AxelixCachesEndpoint {
        public CustomAxelixCachesEndpoint(CacheOperationsDispatcher dispatcher) {
            super(dispatcher);
        }
    }

    static class CustomCacheManagerBeanPostProcessor extends CacheManagerBeanPostProcessor {}
}
