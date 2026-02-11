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

import com.axelixlabs.axelix.sbs.spring.core.threaddump.DefaultThreadDumpContentionMonitoringManagement;
import com.axelixlabs.axelix.sbs.spring.core.threaddump.ThreadDumpContentionMonitoringManagement;
import com.axelixlabs.axelix.sbs.spring.core.threaddump.ThreadDumpManagementEndpoint;

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

    @Test
    void shouldNotCreateDefaultThreadDumpContentionMonitoringManagement_whenCustomBeanProvided() {
        contextRunner
                .withUserConfiguration(CustomThreadDumpManagementConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(ThreadDumpContentionMonitoringManagement.class);
                    assertThat(context.getBean(ThreadDumpContentionMonitoringManagement.class))
                            .isExactlyInstanceOf(CustomThreadDumpContentionMonitoringManagement.class);

                    // Endpoint should still be created with custom management bean
                    assertThat(context).hasSingleBean(ThreadDumpManagementEndpoint.class);
                });
    }

    @Test
    void shouldNotCreateDefaultThreadDumpManagementEndpoint_whenCustomBeanProvided() {
        contextRunner
                .withUserConfiguration(CustomThreadDumpEndpointConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(ThreadDumpManagementEndpoint.class);
                    assertThat(context.getBean(ThreadDumpManagementEndpoint.class))
                            .isExactlyInstanceOf(CustomThreadDumpManagementEndpoint.class);
                });
    }

    @TestConfiguration
    static class CustomThreadDumpManagementConfig {
        @Bean
        public ThreadDumpContentionMonitoringManagement threadDumpContentionMonitoringManagement() {
            return new CustomThreadDumpContentionMonitoringManagement();
        }
    }

    @TestConfiguration
    static class CustomThreadDumpEndpointConfig {
        @Bean
        public ThreadDumpManagementEndpoint threadDumpManagementEndpoint(
                ThreadDumpContentionMonitoringManagement threadDumpContentionMonitoringManagement) {
            return new CustomThreadDumpManagementEndpoint(threadDumpContentionMonitoringManagement);
        }
    }

    static class CustomThreadDumpContentionMonitoringManagement implements ThreadDumpContentionMonitoringManagement {
        @Override
        public void enable() {}

        @Override
        public void disable() {}
    }

    static class CustomThreadDumpManagementEndpoint extends ThreadDumpManagementEndpoint {
        public CustomThreadDumpManagementEndpoint(
                ThreadDumpContentionMonitoringManagement threadDumpContentionMonitoringManagement) {
            super(threadDumpContentionMonitoringManagement);
        }
    }
}
