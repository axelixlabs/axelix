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

import com.axelixlabs.axelix.sbs.spring.core.sbom.AxelixDependenciesEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.sbom.ClasspathDependencySbom;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link AxelixDependenciesEndpointAutoConfiguration}.
 *
 * @author Mikhail Polivakha
 */
class AxelixDependenciesEndpointAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-dependencies")
            .withConfiguration(AutoConfigurations.of(AxelixDependenciesEndpointAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ClasspathDependencySbom.class);
            assertThat(context).hasSingleBean(AxelixDependenciesEndpoint.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfigurationWhenEndpointDisabled() {
        contextRunner // Overriding the property value to test the disabled state
                .withPropertyValues("management.endpoints.web.exposure.exclude=axelix-dependencies")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixDependenciesEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(ClasspathDependencySbom.class);
                    assertThat(context).doesNotHaveBean(AxelixDependenciesEndpoint.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutRequiredProperty() {
        ApplicationContextRunner runnerWithoutExposure = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AxelixDependenciesEndpointAutoConfiguration.class));

        runnerWithoutExposure.run(context -> {
            assertThat(context).doesNotHaveBean(AxelixDependenciesEndpointAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(ClasspathDependencySbom.class);
            assertThat(context).doesNotHaveBean(AxelixDependenciesEndpoint.class);
        });
    }
}
