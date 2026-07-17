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

import jakarta.persistence.EntityManagerFactory;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.log4j2.Log4J2LoggingSystem;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.axelixlabs.axelix.sbs.spring.autoconfiguration.TransactionMonitoringAutoConfiguration.Log4j2InMemoryPaginationAppenderConfiguration;
import com.axelixlabs.axelix.sbs.spring.autoconfiguration.TransactionMonitoringAutoConfiguration.LogbackInMemoryPaginationAppenderConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.persistence.ProxyingDataSourceBeanPostProcessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringBeanPostProcessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.http.ExternalCallRestTemplateCustomizer;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link TransactionMonitoringAutoConfiguration}
 *
 * @since 10.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Ilya Naumov
 * @author Vyacheslav Yanin
 */
class TransactionMonitoringAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TransactionMonitoringAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TransactionMonitoringAutoConfiguration.class);
            assertThat(context).hasSingleBean(TransactionStatsCollector.class);
            assertThat(context).hasSingleBean(TransactionMonitoringBeanPostProcessor.class);
            assertThat(context).hasSingleBean(ProxyingDataSourceBeanPostProcessor.class);
            assertThat(context).hasSingleBean(ExternalCallRestTemplateCustomizer.class);
            assertThat(context).doesNotHaveBean(LogbackInMemoryPaginationAppenderConfiguration.class);
        });
    }

    @Test // GH-1254
    void shouldCreateLogbackPaginationAppender_whenDefaultSpringBootSetupIsUsed() {

        // by default, in spring-boot logback has precedence over log4j2
        contextRunner
                .withBean(EntityManagerFactory.class, () -> Mockito.mock(EntityManagerFactory.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(LogbackInMemoryPaginationAppenderConfiguration.class);
                    assertThat(context).doesNotHaveBean(Log4j2InMemoryPaginationAppenderConfiguration.class);
                });
    }

    @Test // GH-1251
    @Disabled(
            "TODO: We need to figure out how to run tests with log4j2, maybe we can create a new gradle test task or smth")
    void shouldActivateLog4j2Configuration_whenLog4j2IsTheDetectedLoggingSystemBySpringBoot() {

        contextRunner
                .withBean(EntityManagerFactory.class, () -> Mockito.mock(EntityManagerFactory.class))
                .withBean(
                        LoggingSystem.class,
                        () -> new Log4J2LoggingSystem(getClass().getClassLoader()))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(LogbackInMemoryPaginationAppenderConfiguration.class);
                    assertThat(context).hasSingleBean(Log4j2InMemoryPaginationAppenderConfiguration.class);
                });
    }

    @Test // GH-1251
    void shouldNotCreateAnyAppender_whenEntityManagerFactoryIsMissing() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(LogbackInMemoryPaginationAppenderConfiguration.class);
            assertThat(context).doesNotHaveBean(Log4j2InMemoryPaginationAppenderConfiguration.class);
        });
    }

    @Test // GH-1250
    void shouldNotRegisterInMemoryPaginationAppenderConfiguration_whenDetectionDisabled() {
        contextRunner
                .withPropertyValues("axelix.sbs.transaction.monitoring.in-memory-pagination-detection.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(TransactionMonitoringAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(LogbackInMemoryPaginationAppenderConfiguration.class);
                    assertThat(context).doesNotHaveBean(Log4j2InMemoryPaginationAppenderConfiguration.class);
                });
    }
}
