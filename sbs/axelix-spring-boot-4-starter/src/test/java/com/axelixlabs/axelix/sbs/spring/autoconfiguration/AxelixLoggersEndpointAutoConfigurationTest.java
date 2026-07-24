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
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.axelixlabs.axelix.sbs.spring.core.loggers.AxelixLoggersEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.loggers.LoggersService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixLoggersEndpointAutoConfiguration}.
 *
 * @author Sergey Cherkasov
 */
class AxelixLoggersEndpointAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-loggers")
            .withBean(LoggingSystem.class, () -> LoggingSystem.get(getClass().getClassLoader()))
            .withConfiguration(AutoConfigurations.of(AxelixLoggersEndpointAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AxelixLoggersEndpointAutoConfiguration.class);
            assertThat(context).hasSingleBean(AxelixLoggersEndpoint.class);
            assertThat(context).hasSingleBean(LoggersService.class);
        });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutRequiredProperty() {
        new ApplicationContextRunner()
                .withBean(
                        LoggingSystem.class, () -> LoggingSystem.get(getClass().getClassLoader()))
                .withConfiguration(AutoConfigurations.of(AxelixLoggersEndpointAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixLoggersEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(AxelixLoggersEndpoint.class);
                    assertThat(context).doesNotHaveBean(LoggersService.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutLoggingSystem() {
        new ApplicationContextRunner()
                .withPropertyValues("management.endpoints.web.exposure.include=axelix-loggers")
                .withConfiguration(AutoConfigurations.of(AxelixLoggersEndpointAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AxelixLoggersEndpointAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(AxelixLoggersEndpoint.class);
                    assertThat(context).doesNotHaveBean(LoggersService.class);
                });
    }
}
