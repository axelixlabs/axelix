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

import com.axelixlabs.axelix.sbs.spring.core.heapdump.AxelixHeapDumpEndpoint;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixHeapDumpEndpointAutoConfiguration}
 *
 * @since 09.02.2026
 * @author Nikita Kirillov
 */
class AxelixHeapDumpEndpointAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-heap-dump")
            .withConfiguration(AutoConfigurations.of(AxelixHeapDumpEndpointAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(AxelixHeapDumpEndpoint.class));
    }

    @Test
    void shouldNotActivateAutoConfigurationWhenEndpointDisabled() {
        contextRunner // Overriding the property value to test the disabled state
                .withPropertyValues("management.endpoint.axelix-heap-dump.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixHeapDumpEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(AxelixHeapDumpEndpoint.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutRequiredProperty() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AxelixHeapDumpEndpointAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixHeapDumpEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(AxelixHeapDumpEndpoint.class);
                });
    }

    @Test
    void shouldNotCreateDefaultAxelixHeapDumpEndpoint_whenCustomBeanProvided() {
        contextRunner
                .withUserConfiguration(CustomAxelixHeapDumpEndpointConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(AxelixHeapDumpEndpoint.class);
                    var beans = context.getBeansOfType(AxelixHeapDumpEndpoint.class);
                    assertThat(beans).hasSize(1);
                    assertThat(beans.values().iterator().next())
                            .isExactlyInstanceOf(CustomAxelixHeapDumpEndpoint.class);
                });
    }

    @TestConfiguration
    static class CustomAxelixHeapDumpEndpointConfig {
        @Bean
        public AxelixHeapDumpEndpoint customAxelixHeapDumpEndpoint() {
            return new CustomAxelixHeapDumpEndpoint();
        }
    }

    static class CustomAxelixHeapDumpEndpoint extends AxelixHeapDumpEndpoint {
        public CustomAxelixHeapDumpEndpoint() {
            super();
        }
    }
}
