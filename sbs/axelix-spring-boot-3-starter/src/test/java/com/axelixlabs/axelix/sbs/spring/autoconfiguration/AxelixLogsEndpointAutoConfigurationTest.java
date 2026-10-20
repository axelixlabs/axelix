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
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.axelixlabs.axelix.sbs.spring.core.SlidingWindow;
import com.axelixlabs.axelix.sbs.spring.core.contract.logs.LogEntry;
import com.axelixlabs.axelix.sbs.spring.core.logs.AxelixLogsEndpoint;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixLogsEndpointAutoConfiguration}
 *
 * @author Mikhail Polivakha
 * @author Vyacheslav Yanin
 */
class AxelixLogsEndpointAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ConfigurationPropertiesAutoConfiguration.class, AxelixLogsEndpointAutoConfiguration.class));

    @Test
    void shouldCaptureLogEntriesInDefaultScenario() {
        contextRunner
                .withPropertyValues("management.endpoints.web.exposure.include=axelix-logs")
                .run(context -> {
                    assertThat(context).hasSingleBean(AxelixLogsEndpoint.class);

                    LoggerFactory.getLogger("axelix.logs.autoconfiguration.test")
                            .info("captured by the appender");

                    assertThat(context.getBean(AxelixLogsEndpoint.class)
                                    .tail(1000)
                                    .getEntries())
                            .extracting(LogEntry::getMessage)
                            .contains("captured by the appender");
                });
    }

    @Test
    void shouldNotActivateAutoConfigurationWhenEndpointDisabled() {
        contextRunner
                .withPropertyValues(
                        "management.endpoints.web.exposure.include=axelix-logs",
                        "management.endpoint.axelix-logs.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(AxelixLogsEndpoint.class));
    }

    @Test
    void shouldNotActivateAutoConfigurationWithoutRequiredProperty() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(AxelixLogsEndpoint.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldKeepTheDefaultAmountOfLogEntries() {
        contextRunner
                .withPropertyValues("management.endpoints.web.exposure.include=axelix-logs")
                .run(context -> {
                    // given.
                    SlidingWindow<LogEntry> logEntries = context.getBean("logEntries", SlidingWindow.class);
                    fill(logEntries, 1001);

                    // when.
                    int buffered = context.getBean(AxelixLogsEndpoint.class).tail(null).getEntries().size();

                    // then.
                    assertThat(buffered).isEqualTo(1000);
                });
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldKeepTheConfiguredAmountOfLogEntries() {
        contextRunner
                .withPropertyValues(
                        "management.endpoints.web.exposure.include=axelix-logs", "axelix.sbs.logs.max-entries=2")
                .run(context -> {
                    // given.
                    SlidingWindow<LogEntry> logEntries = context.getBean("logEntries", SlidingWindow.class);
                    fill(logEntries, 5);

                    // when.
                    int buffered = context.getBean(AxelixLogsEndpoint.class).tail(null).getEntries().size();

                    // then.
                    assertThat(buffered).isEqualTo(2);
                });
    }

    @Test
    void shouldNotActivateAutoConfigurationWhenTheAmountOfLogEntriesIsNotPositive() {
        contextRunner
                .withPropertyValues(
                        "management.endpoints.web.exposure.include=axelix-logs", "axelix.sbs.logs.max-entries=0")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(IllegalArgumentException.class);
                });
    }

    private static void fill(SlidingWindow<LogEntry> logEntries, int amount) {
        for (int i = 0; i < amount; i++) {
            logEntries.put(new LogEntry().message("entry " + i));
        }
    }
}
