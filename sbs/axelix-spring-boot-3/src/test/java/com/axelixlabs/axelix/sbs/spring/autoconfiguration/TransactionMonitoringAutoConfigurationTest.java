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

import java.time.Duration;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.core.transactions.DefaultTransactionMonitoringService;
import com.axelixlabs.axelix.sbs.spring.core.transactions.DefaultTransactionStatsCollector;
import com.axelixlabs.axelix.sbs.spring.core.transactions.QueriesStatsCollector;
import com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringBeanPostProcessor;
import com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringService;
import com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionStatsCollector;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link TransactionMonitoringAutoConfiguration}
 *
 * @since 10.02.2026
 * @author Nikita Kirillov
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
        });
    }

    @Test
    void shouldFailure_handleInvalidMaxTransactionsPerMethod() {
        new ApplicationContextRunner()
                .withPropertyValues(
                        "management.endpoints.web.exposure.include=axelix-transactions-monitoring",
                        "axelix.sbs.transaction.monitoring.max-transactions-per-method=0",
                        "axelix.sbs.transaction.monitoring.cleanup-interval=PT5S")
                .withConfiguration(AutoConfigurations.of(TransactionMonitoringAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).isNotNull();
                    assertThat(context.getStartupFailure().getMessage())
                            .contains("maxTransactionsPerMethod must be positive");
                });
    }

    @Test
    void shouldFailure_HandleInvalidCleanupInterval() {
        new ApplicationContextRunner()
                .withPropertyValues(
                        "management.endpoints.web.exposure.include=axelix-transactions-monitoring",
                        "axelix.sbs.transaction.monitoring.max-transactions-per-method=10",
                        "axelix.sbs.transaction.monitoring.cleanup-interval=PT0S")
                .withConfiguration(AutoConfigurations.of(TransactionMonitoringAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).isNotNull();
                    assertThat(context.getStartupFailure().getMessage()).contains("cleanupInterval must be positive");
                });
    }

    @Test
    void shouldHandleMultipleCustomBeans() {
        contextRunner
                .withUserConfiguration(
                        CustomTransactionStatsCollectorConfig.class,
                        CustomTransactionMonitoringServiceConfig.class,
                        CustomTransactionMonitoringEndpointConfig.class,
                        CustomTransactionMonitoringBeanPostProcessorConfig.class)
                .run(context -> {
                    assertThat(context.getBean(TransactionStatsCollector.class))
                            .isExactlyInstanceOf(CustomTransactionStatsCollector.class);
                    assertThat(context.getBean(TransactionMonitoringService.class))
                            .isExactlyInstanceOf(CustomTransactionMonitoringService.class);
                    assertThat(context.getBean(TransactionMonitoringEndpoint.class))
                            .isExactlyInstanceOf(CustomTransactionMonitoringEndpoint.class);
                    assertThat(context.getBean(TransactionMonitoringBeanPostProcessor.class))
                            .isExactlyInstanceOf(CustomTransactionMonitoringBeanPostProcessor.class);
                });
    }

    @TestConfiguration
    static class CustomTransactionStatsCollectorConfig {

        @Bean
        public TransactionStatsCollector transactionStatsCollector() {
            return new CustomTransactionStatsCollector();
        }
    }

    @TestConfiguration
    static class CustomTransactionMonitoringServiceConfig {

        @Bean
        public TransactionMonitoringService transactionMonitoringService(
                TransactionStatsCollector transactionStatsCollector) {
            return new CustomTransactionMonitoringService(transactionStatsCollector);
        }
    }

    @TestConfiguration
    static class CustomTransactionMonitoringEndpointConfig {

        @Bean
        public TransactionMonitoringEndpoint transactionMonitoringEndpoint(
                TransactionMonitoringService transactionMonitoringService) {
            return new CustomTransactionMonitoringEndpoint(transactionMonitoringService);
        }
    }

    @TestConfiguration
    static class CustomTransactionMonitoringBeanPostProcessorConfig {

        @Bean
        public TransactionMonitoringBeanPostProcessor transactionMonitoringBeanPostProcessor(
                TransactionStatsCollector transactionStatsCollector, QueriesStatsCollector queriesStatsCollector) {
            return new CustomTransactionMonitoringBeanPostProcessor(transactionStatsCollector, queriesStatsCollector);
        }
    }

    static class CustomTransactionStatsCollector extends DefaultTransactionStatsCollector {
        public CustomTransactionStatsCollector() {
            super(1000, Duration.ofSeconds(5));
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

    static class CustomTransactionMonitoringBeanPostProcessor extends TransactionMonitoringBeanPostProcessor {
        public CustomTransactionMonitoringBeanPostProcessor(
                TransactionStatsCollector transactionStatsCollector, QueriesStatsCollector queriesStatsCollector) {
            super(transactionStatsCollector, queriesStatsCollector);
        }
    }
}
