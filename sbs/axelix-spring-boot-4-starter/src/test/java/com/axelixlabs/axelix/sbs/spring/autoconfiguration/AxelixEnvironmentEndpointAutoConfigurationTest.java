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
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.axelixlabs.axelix.sbs.spring.core.auth.RequiredAuthorityCheckService;
import com.axelixlabs.axelix.sbs.spring.core.configprops.SmartSanitizingFunction;
import com.axelixlabs.axelix.sbs.spring.core.env.AxelixEnvironmentEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.env.EnvPropertyEnricher;
import com.axelixlabs.axelix.sbs.spring.core.env.EnvironmentService;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyMetadataExtractor;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.env.ValueInjectionTrackerBeanPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixEnvironmentEndpointAutoConfiguration}
 *
 * @since 09.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
class AxelixEnvironmentEndpointAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-env")
            .withConfiguration(AutoConfigurations.of(
                    AxelixEnvironmentEndpointAutoConfiguration.class,
                    EndpointPropertiesSupportAutoConfiguration.class,
                    SecurityContextExecutorAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(PropertyNameNormalizer.class);
            assertThat(context).hasSingleBean(PropertyMetadataExtractor.class);
            assertThat(context).hasSingleBean(SmartSanitizingFunction.class);
            assertThat(context).hasSingleBean(EnvPropertyEnricher.class);
            assertThat(context).hasSingleBean(AxelixEnvironmentEndpoint.class);
            assertThat(context).hasSingleBean(ValueInjectionTrackerBeanPostProcessor.class);
            assertThat(context).hasSingleBean(EnvironmentService.class);
            assertThat(context).hasSingleBean(RequiredAuthorityCheckService.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutRequiredProperty() {
        new ApplicationContextRunner()
                .withPropertyValues("management.endpoints.web.exposure.exclude=axelix-env")
                .withConfiguration(AutoConfigurations.of(
                        AxelixEnvironmentEndpointAutoConfiguration.class,
                        EndpointPropertiesSupportAutoConfiguration.class,
                        SecurityContextExecutorAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixEnvironmentEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(PropertyMetadataExtractor.class);
                    assertThat(context).doesNotHaveBean(EnvPropertyEnricher.class);
                    assertThat(context).doesNotHaveBean(AxelixEnvironmentEndpoint.class);
                    assertThat(context).doesNotHaveBean(ValueInjectionTrackerBeanPostProcessor.class);
                    assertThat(context).doesNotHaveBean(EnvironmentService.class);
                });
    }
}
