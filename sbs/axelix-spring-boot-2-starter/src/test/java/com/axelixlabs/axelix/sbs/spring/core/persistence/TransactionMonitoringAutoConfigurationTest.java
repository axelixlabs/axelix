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
package com.axelixlabs.axelix.sbs.spring.core.persistence;

import javax.persistence.EntityManagerFactory;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.log4j2.Log4J2LoggingSystem;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.autoconfiguration.AxelixConfigurationsPropertiesEndpointAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringAutoConfiguration.Log4j2InMemoryPaginationAppenderConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringAutoConfiguration.LogbackInMemoryPaginationAppenderConfiguration;

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
            .withPropertyValues("management.endpoints.web.exposure.include=axelix-transactions-monitoring")
            .withConfiguration(AutoConfigurations.of(TransactionMonitoringAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TransactionMonitoringAutoConfiguration.class);
            assertThat(context).hasSingleBean(TransactionStatsCollector.class);
            assertThat(context).hasSingleBean(TransactionMonitoringService.class);
            assertThat(context).hasSingleBean(TransactionMonitoringEndpoint.class);
            assertThat(context).hasSingleBean(TransactionMonitoringBeanPostProcessor.class);
            assertThat(context).hasSingleBean(ProxyingDataSourceBeanPostProcessor.class);
            assertThat(context).doesNotHaveBean(LogbackInMemoryPaginationAppenderConfiguration.class);
        });
    }

    @Test // GH-1254
    void shouldCreatePaginationAppender_whenEntityManagerFactoryIsPresent() {
        EntityManagerFactory mockFactory = Mockito.mock(EntityManagerFactory.class);

        contextRunner
                .withBean(EntityManagerFactory.class, () -> mockFactory)
                .run(context ->
                        assertThat(context).hasSingleBean(LogbackInMemoryPaginationAppenderConfiguration.class));
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

    @Test
    void shouldNotActivateAutoConfiguration_whenEndpointDisabled() {
        contextRunner // Overriding the property value to test the disabled state
                .withPropertyValues("management.endpoints.web.exposure.exclude=axelix-transactions-monitoring")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TransactionMonitoringAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(TransactionStatsCollector.class);
                    assertThat(context).doesNotHaveBean(TransactionMonitoringService.class);
                    assertThat(context).doesNotHaveBean(TransactionMonitoringEndpoint.class);
                    assertThat(context).doesNotHaveBean(TransactionMonitoringBeanPostProcessor.class);
                    assertThat(context).doesNotHaveBean(ProxyingDataSourceBeanPostProcessor.class);
                    assertThat(context).doesNotHaveBean(LogbackInMemoryPaginationAppenderConfiguration.class);
                });
    }

    @Test
    void shouldNotActivateAutoConfiguration_withoutRequiredProperty() {
        ApplicationContextRunner runnerWithoutRequiredProperty = new ApplicationContextRunner()
                .withConfiguration(
                        AutoConfigurations.of(AxelixConfigurationsPropertiesEndpointAutoConfiguration.class));

        runnerWithoutRequiredProperty.run(context -> {
            assertThat(context).doesNotHaveBean(TransactionMonitoringAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(TransactionStatsCollector.class);
            assertThat(context).doesNotHaveBean(TransactionMonitoringService.class);
            assertThat(context).doesNotHaveBean(TransactionMonitoringEndpoint.class);
            assertThat(context).doesNotHaveBean(TransactionMonitoringBeanPostProcessor.class);
            assertThat(context).doesNotHaveBean(ProxyingDataSourceBeanPostProcessor.class);
            assertThat(context).doesNotHaveBean(LogbackInMemoryPaginationAppenderConfiguration.class);
        });
    }

    @Test
    void shouldHandleMultipleCustomBeans() {
        contextRunner
                .withUserConfiguration(CustomTransactionConfiguration.class)
                .run(context -> {
                    assertThat(context.getBean(TransactionStatsCollector.class))
                            .isExactlyInstanceOf(CustomTransactionStatsCollector.class);
                    assertThat(context.getBean(TransactionMonitoringService.class))
                            .isExactlyInstanceOf(CustomTransactionMonitoringService.class);
                    assertThat(context.getBean(TransactionMonitoringEndpoint.class))
                            .isExactlyInstanceOf(CustomTransactionMonitoringEndpoint.class);
                });
    }

    @TestConfiguration
    static class CustomTransactionConfiguration {

        @Bean
        public TransactionStatsCollector transactionStatsCollector() {
            return new CustomTransactionStatsCollector();
        }

        @Bean
        public TransactionMonitoringService transactionMonitoringService(
                TransactionStatsCollector transactionStatsCollector) {
            return new CustomTransactionMonitoringService(transactionStatsCollector);
        }

        @Bean
        public TransactionMonitoringEndpoint transactionMonitoringEndpoint(
                TransactionMonitoringService transactionMonitoringService) {
            return new CustomTransactionMonitoringEndpoint(transactionMonitoringService);
        }
    }

    static class CustomTransactionStatsCollector extends DefaultTransactionStatsCollector {

        public CustomTransactionStatsCollector() {
            super(1000);
        }
    }

    static class CustomTransactionMonitoringService extends DefaultTransactionMonitoringService {

        public CustomTransactionMonitoringService(TransactionStatsCollector transactionStatsCollector) {
            super(transactionStatsCollector);
        }
    }

    static class CustomTransactionMonitoringEndpoint extends TransactionMonitoringEndpoint {

        public CustomTransactionMonitoringEndpoint(TransactionMonitoringService transactionMonitoringService) {
            super(transactionMonitoringService);
        }
    }
}
