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

import java.util.List;

import feign.Feign;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.openfeign.FeignClientFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.util.ReflectionTestUtils;

import com.axelixlabs.axelix.sbs.spring.core.integrations.feign.AxelixFeignEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.integrations.feign.FeignClientIntegrationDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.integrations.feign.NoOpDiscoveryClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixFeignEndpointAutoConfiguration}.
 *
 * @author Sergey Cherkasov
 */
class AxelixFeignEndpointAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-feign")
            .withBean(FeignClientFactoryBean.class, FeignClientFactoryBean::new, bean -> bean.setLazyInit(true))
            .withConfiguration(AutoConfigurations.of(AxelixFeignEndpointAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AxelixFeignEndpoint.class);
            assertThat(context).hasSingleBean(FeignClientIntegrationDiscoverer.class);
        });
    }

    @Test
    void shouldUseNoOpDiscoveryClientFallbackWhenDiscoveryClientMissing() {
        contextRunner.run(context -> {
            FeignClientIntegrationDiscoverer discoverer = context.getBean(FeignClientIntegrationDiscoverer.class);
            DiscoveryClient discoveryClient =
                    (DiscoveryClient) ReflectionTestUtils.getField(discoverer, "discoveryClient");

            assertThat(discoveryClient).isNotNull();
            assertThat(discoveryClient.getClass().getName()).isEqualTo(NoOpDiscoveryClient.class.getName());
            assertThat(context).doesNotHaveBean(DiscoveryClient.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfigurationWhenEndpointDisabled() {
        contextRunner // Overriding the property value to test the disabled state
                .withPropertyValues("management.endpoints.web.exposure.exclude=axelix-feign")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixFeignEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(AxelixFeignEndpoint.class);
                    assertThat(context).doesNotHaveBean(FeignClientIntegrationDiscoverer.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutRequiredProperty() {
        ApplicationContextRunner runnerWithoutCacheConfig = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AxelixFeignEndpointAutoConfiguration.class));

        runnerWithoutCacheConfig.run(context -> {
            assertThat(context).doesNotHaveBean(AxelixFeignEndpointAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(AxelixFeignEndpoint.class);
            assertThat(context).doesNotHaveBean(FeignClientIntegrationDiscoverer.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfigurationWhenFeignClassesMissing() {
        contextRunner.withClassLoader(new FilteredClassLoader(Feign.class)).run(context -> {
            // then.
            assertThat(context).doesNotHaveBean(AxelixFeignEndpointAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(AxelixFeignEndpoint.class);
            assertThat(context).doesNotHaveBean(FeignClientIntegrationDiscoverer.class);
        });
    }

    @Test
    void shouldHandleMultipleCustomBeans() {
        contextRunner
                .withUserConfiguration(
                        CustomFeignClientIntegrationDiscovererConfig.class, CustomAxelixFeignEndpointConfig.class)
                .run(context -> {
                    assertThat(context.getBean(FeignClientIntegrationDiscoverer.class))
                            .isExactlyInstanceOf(
                                    AxelixFeignEndpointAutoConfigurationTest.CustomFeignClientIntegrationDiscoverer
                                            .class);
                    assertThat(context.getBean(AxelixFeignEndpoint.class))
                            .isExactlyInstanceOf(
                                    AxelixFeignEndpointAutoConfigurationTest.CustomAxelixFeignEndpoint.class);
                });
    }

    @TestConfiguration
    static class CustomFeignClientIntegrationDiscovererConfig {
        @Bean
        public FeignClientIntegrationDiscoverer customFeignClientIntegrationDiscoverer(ApplicationContext context) {
            return new CustomFeignClientIntegrationDiscoverer(context, new NoOpTestDiscoveryClient());
        }
    }

    @TestConfiguration
    static class CustomAxelixFeignEndpointConfig {
        @Bean
        public AxelixFeignEndpoint customAxelixFeignEndpoint(FeignClientIntegrationDiscoverer discoverer) {
            return new CustomAxelixFeignEndpoint(discoverer);
        }
    }

    static final class CustomAxelixFeignEndpoint extends AxelixFeignEndpoint {
        CustomAxelixFeignEndpoint(FeignClientIntegrationDiscoverer discoverer) {
            super(discoverer);
        }
    }

    static final class CustomFeignClientIntegrationDiscoverer extends FeignClientIntegrationDiscoverer {
        CustomFeignClientIntegrationDiscoverer(ApplicationContext context, DiscoveryClient discoveryClient) {
            super(context, discoveryClient);
        }
    }

    static final class NoOpTestDiscoveryClient implements DiscoveryClient {
        @Override
        public String description() {
            return "test";
        }

        @Override
        public List<ServiceInstance> getInstances(String serviceId) {
            return List.of();
        }

        @Override
        public List<String> getServices() {
            return List.of();
        }
    }
}
