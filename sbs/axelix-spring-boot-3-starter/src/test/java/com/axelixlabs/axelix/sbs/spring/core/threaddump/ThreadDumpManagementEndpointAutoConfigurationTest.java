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
package com.axelixlabs.axelix.sbs.spring.core.threaddump;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ThreadDumpManagementEndpointAutoConfiguration}
 *
 * @since 10.02.2026
 * @author Nikita Kirillov
 */
class ThreadDumpManagementEndpointAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-thread-dump")
            .withConfiguration(AutoConfigurations.of(ThreadDumpManagementEndpointAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ThreadDumpManagementEndpointAutoConfiguration.class);
            assertThat(context).hasSingleBean(ThreadDumpContentionMonitoringManagement.class);
            assertThat(context).hasSingleBean(ThreadDumpManagementEndpoint.class);
            assertThat(context.getBean(ThreadDumpContentionMonitoringManagement.class))
                    .isExactlyInstanceOf(DefaultThreadDumpContentionMonitoringManagement.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfiguration_whenEndpointDisabled() {
        contextRunner
                .withPropertyValues("management.endpoints.web.exposure.exclude=axelix-thread-dump")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ThreadDumpManagementEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(ThreadDumpContentionMonitoringManagement.class);
                    assertThat(context).doesNotHaveBean(ThreadDumpManagementEndpoint.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutRequiredProperty() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ThreadDumpManagementEndpointAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ThreadDumpManagementEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(ThreadDumpContentionMonitoringManagement.class);
                    assertThat(context).doesNotHaveBean(ThreadDumpManagementEndpoint.class);
                });
    }
}
