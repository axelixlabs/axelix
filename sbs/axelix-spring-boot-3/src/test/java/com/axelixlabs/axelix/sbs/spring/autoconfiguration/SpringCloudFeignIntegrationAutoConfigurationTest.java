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

import java.util.Set;

import feign.Feign;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.core.integrations.IntegrationComponentDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.integrations.http.FeignClientIntegrationDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.integrations.http.HttpIntegration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link SpringCloudFeignIntegrationAutoConfiguration}
 *
 * @since 10.02.2026
 * @author Nikita Kirillov
 */
class SpringCloudFeignIntegrationAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SpringCloudFeignIntegrationAutoConfiguration.class));

    @Test
    void shouldCreateBeanWhenFeignClassesArePresent() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SpringCloudFeignIntegrationAutoConfiguration.class);
            assertThat(context).hasSingleBean(IntegrationComponentDiscoverer.class);
            assertThat(context)
                    .getBean(IntegrationComponentDiscoverer.class)
                    .isInstanceOf(FeignClientIntegrationDiscoverer.class);
        });
    }

    @ParameterizedTest
    @ValueSource(classes = {Feign.class, FeignClient.class})
    void shouldNotActivateAutoConfiguration_whenFeignClassMissing(Class<?> toBeExcluded) {
        contextRunner.withClassLoader(new FilteredClassLoader(toBeExcluded)).run(context -> {
            assertThat(context).doesNotHaveBean(SpringCloudFeignIntegrationAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(IntegrationComponentDiscoverer.class);
        });
    }

    @Test
    void shouldNotCreateDefaultDiscoverer_whenCustomBeanProvided() {
        contextRunner.withUserConfiguration(CustomDiscovererConfig.class).run(context -> {
            assertThat(context).hasSingleBean(IntegrationComponentDiscoverer.class);
            assertThat(context)
                    .getBean(IntegrationComponentDiscoverer.class)
                    .isExactlyInstanceOf(CustomFeignClientIntegrationDiscoverer.class)
                    .isNotInstanceOf(FeignClientIntegrationDiscoverer.class);
        });
    }

    @TestConfiguration
    static class CustomDiscovererConfig {
        @Bean
        public IntegrationComponentDiscoverer<HttpIntegration> feignClientIntegrationDiscoverer(
                ApplicationContext context) {
            return new CustomFeignClientIntegrationDiscoverer(context);
        }
    }

    static class CustomFeignClientIntegrationDiscoverer implements IntegrationComponentDiscoverer<HttpIntegration> {
        private final ApplicationContext context;

        public CustomFeignClientIntegrationDiscoverer(ApplicationContext context) {
            this.context = context;
        }

        public ApplicationContext getContext() {
            return context;
        }

        @Override
        public Set<HttpIntegration> discoverIntegrations() {
            return Set.of();
        }
    }
}
