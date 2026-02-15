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

import com.axelixlabs.axelix.sbs.spring.core.gclog.AxelixGcEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.gclog.DefaultGcLogService;
import com.axelixlabs.axelix.sbs.spring.core.gclog.GcLogService;
import com.axelixlabs.axelix.sbs.spring.core.gclog.JcmdExecutor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixGcEndpointAutoConfiguration}
 *
 * @since 09.02.2026
 * @author Nikita Kirillov
 */
class AxelixGcEndpointAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-gc")
            .withConfiguration(AutoConfigurations.of(AxelixGcEndpointAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(JcmdExecutor.class);
            assertThat(context).hasSingleBean(GcLogService.class);
            assertThat(context).hasSingleBean(AxelixGcEndpoint.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfigurationWhenEndpointDisabled() {
        contextRunner // Overriding the property value to test the disabled state
                .withPropertyValues("management.endpoint.axelix-gc.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixGcEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(JcmdExecutor.class);
                    assertThat(context).doesNotHaveBean(GcLogService.class);
                    assertThat(context).doesNotHaveBean(AxelixGcEndpoint.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutRequiredProperty() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AxelixGcEndpointAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixGcEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(AxelixGcEndpoint.class);
                    assertThat(context).doesNotHaveBean(JcmdExecutor.class);
                    assertThat(context).doesNotHaveBean(GcLogService.class);
                });
    }

    @Test
    void shouldHandleMultipleCustomBeans() {
        contextRunner
                .withUserConfiguration(
                        CustomJcmdExecutorConfig.class,
                        CustomGcLogServiceConfig.class,
                        CustomAxelixGcEndpointConfig.class)
                .run(context -> {
                    assertThat(context.getBean(JcmdExecutor.class)).isExactlyInstanceOf(CustomJcmdExecutor.class);
                    assertThat(context.getBean(GcLogService.class)).isExactlyInstanceOf(CustomGcLogService.class);
                    assertThat(context.getBean(AxelixGcEndpoint.class))
                            .isExactlyInstanceOf(CustomAxelixGcEndpoint.class);
                });
    }

    @TestConfiguration
    static class CustomJcmdExecutorConfig {
        @Bean
        public JcmdExecutor jcmdExecutor() {
            return new CustomJcmdExecutor();
        }
    }

    @TestConfiguration
    static class CustomGcLogServiceConfig {
        @Bean
        public GcLogService gcLogService(JcmdExecutor jcmdExecutor) {
            return new CustomGcLogService(jcmdExecutor);
        }
    }

    @TestConfiguration
    static class CustomAxelixGcEndpointConfig {
        @Bean
        public AxelixGcEndpoint axelixGcEndpoint(GcLogService gcLogService) {
            return new CustomAxelixGcEndpoint(gcLogService);
        }
    }

    static class CustomJcmdExecutor extends JcmdExecutor {}

    static class CustomGcLogService extends DefaultGcLogService {
        public CustomGcLogService(JcmdExecutor jcmdExecutor) {
            super(jcmdExecutor);
        }
    }

    static class CustomAxelixGcEndpoint extends AxelixGcEndpoint {
        public CustomAxelixGcEndpoint(GcLogService gcLogService) {
            super(gcLogService);
        }
    }
}
