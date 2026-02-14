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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.core.conditions.AxelixConditionsEndpoint;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link AxelixConditionsEndpointAutoConfiguration}
 *
 * @since 09.02.2026
 * @author Nikita Kirillov
 */
class AxelixConditionsEndpointAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-conditions")
            .withConfiguration(AutoConfigurations.of(AxelixConditionsEndpointAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AxelixConditionsEndpoint.class);

            AxelixConditionsEndpoint endpoint = context.getBean(AxelixConditionsEndpoint.class);
            assertThat(endpoint).isNotNull();
        });
    }

    @Test
    void shouldNotActivateAutoConfigurationWhenEndpointDisabled() {
        contextRunner // Overriding the property value to test the disabled state
                .withPropertyValues("management.endpoints.web.exposure.exclude=axelix-conditions")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixConditionsEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(AxelixConditionsEndpoint.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutRequiredProperty() {
        ApplicationContextRunner runnerWithoutRequiredConfig = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AxelixConditionsEndpointAutoConfiguration.class));

        runnerWithoutRequiredConfig.run(context -> {
            assertThat(context).doesNotHaveBean(AxelixConditionsEndpointAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(AxelixConditionsEndpoint.class);
        });
    }

    @Test
    void shouldNotCreateDefaultEndpointWhenCustomBeanProvided() {
        contextRunner
                .withUserConfiguration(CustomAxelixConditionsEndpointConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(AxelixConditionsEndpoint.class);
                    assertThat(context.getBean(AxelixConditionsEndpoint.class))
                            .isExactlyInstanceOf(CustomAxelixConditionsEndpoint.class);
                });
    }

    @TestConfiguration
    static class CustomAxelixConditionsEndpointConfig {
        @Bean
        public AxelixConditionsEndpoint axelixConditionsEndpoint(
                ConfigurableApplicationContext configurableApplicationContext) {
            return new CustomAxelixConditionsEndpoint(configurableApplicationContext);
        }
    }

    static class CustomAxelixConditionsEndpoint extends AxelixConditionsEndpoint {
        public CustomAxelixConditionsEndpoint(ConfigurableApplicationContext configurableApplicationContext) {
            super(configurableApplicationContext);
        }
    }
}
