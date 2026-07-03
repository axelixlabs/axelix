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

import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.sbs.spring.core.auth.RequiredAuthorityCheckService;
import com.axelixlabs.axelix.sbs.spring.core.configprops.AxelixConfigurationPropertiesEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesConverter;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesFlattener;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesService;
import com.axelixlabs.axelix.sbs.spring.core.configprops.SmartSanitizingFunction;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link AxelixConfigurationsPropertiesEndpointAutoConfiguration}
 *
 * @since 09.02.2026
 * @author Nikita Kirillov
 */
class AxelixConfigurationsPropertiesEndpointAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    AxelixConfigurationsPropertiesEndpointAutoConfiguration.class,
                    EndpointPropertiesSupportAutoConfiguration.class,
                    SecurityContextExecutorAutoConfiguration.class,
                    EndpointsConfigurationPropertiesAutoConfiguration.class))
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-configprops");

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ConfigurationPropertiesFlattener.class);
            assertThat(context).hasSingleBean(ConfigurationPropertiesConverter.class);
            assertThat(context).hasSingleBean(PropertyNameNormalizer.class);
            assertThat(context).hasSingleBean(SmartSanitizingFunction.class);
            assertThat(context).hasSingleBean(ConfigurationPropertiesService.class);
            assertThat(context).hasSingleBean(AxelixConfigurationPropertiesEndpoint.class);
            assertThat(context).hasSingleBean(RequiredAuthorityCheckService.class);
            assertThat(context).hasSingleBean(SecurityContextExecutor.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfigurationWhenEndpointDisabled() {
        contextRunner // Overriding the property value to test the disabled state
                .withPropertyValues("management.endpoints.web.exposure.exclude=axelix-configprops")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixConfigurationsPropertiesEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(AxelixConfigurationPropertiesEndpoint.class);
                    assertThat(context).doesNotHaveBean(ConfigurationPropertiesService.class);
                    assertThat(context).doesNotHaveBean(ConfigurationPropertiesConverter.class);
                    assertThat(context).doesNotHaveBean(ConfigurationPropertiesFlattener.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutRequiredProperty() {
        ApplicationContextRunner runnerWithoutRequiredConfig = new ApplicationContextRunner()
                .withConfiguration(
                        AutoConfigurations.of(AxelixConfigurationsPropertiesEndpointAutoConfiguration.class));

        runnerWithoutRequiredConfig.run(context -> {
            assertThat(context).doesNotHaveBean(AxelixConfigurationsPropertiesEndpointAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(AxelixConfigurationPropertiesEndpoint.class);
            assertThat(context).doesNotHaveBean(ConfigurationPropertiesService.class);
            assertThat(context).doesNotHaveBean(SmartSanitizingFunction.class);
            assertThat(context).doesNotHaveBean(PropertyNameNormalizer.class);
            assertThat(context).doesNotHaveBean(ConfigurationPropertiesConverter.class);
            assertThat(context).doesNotHaveBean(ConfigurationPropertiesFlattener.class);
            assertThat(context).doesNotHaveBean(SecurityContextExecutor.class);
            assertThat(context).doesNotHaveBean(RequiredAuthorityCheckService.class);
        });
    }
}
