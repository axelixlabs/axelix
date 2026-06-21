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

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import com.axelixlabs.axelix.sbs.spring.core.config.TransactionMonitoringConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.transactions.DefaultQueriesRecorder;
import com.axelixlabs.axelix.sbs.spring.core.transactions.DefaultTransactionMonitoringService;
import com.axelixlabs.axelix.sbs.spring.core.transactions.DefaultTransactionStatsCollector;
import com.axelixlabs.axelix.sbs.spring.core.transactions.LogbackInMemoryPaginationAppenderRegistrar;
import com.axelixlabs.axelix.sbs.spring.core.transactions.ProxyingDataSourceBeanPostProcessor;
import com.axelixlabs.axelix.sbs.spring.core.transactions.QueriesRecorder;
import com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringBeanPostProcessor;
import com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringService;
import com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionStatsCollector;
import com.axelixlabs.axelix.sbs.spring.core.validate.ValidationListener;

/**
 * Auto-configuration for Transaction Monitoring infrastructure.
 *
 * @since 21.01.2026
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Ilya Naumov
 */
@AutoConfiguration
@ConditionalOnAvailableEndpoint(endpoint = TransactionMonitoringEndpoint.class)
public class TransactionMonitoringAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ValidationListener validationListener() {
        return new ValidationListener();
    }

    @Bean
    @ConfigurationProperties(prefix = "axelix.sbs.transaction.monitoring")
    public TransactionMonitoringConfigurationProperties transactionMonitoringConfigurationProperties() {
        return new TransactionMonitoringConfigurationProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionStatsCollector transactionStatsCollector(
            TransactionMonitoringConfigurationProperties properties) {

        return new DefaultTransactionStatsCollector(properties.getMaxTransactionsPerMethod());
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionMonitoringService transactionMonitoringService(
            TransactionStatsCollector transactionStatsCollector) {
        return new DefaultTransactionMonitoringService(transactionStatsCollector);
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionMonitoringEndpoint transactionMonitoringEndpoint(
            TransactionMonitoringService transactionMonitoringService) {
        return new TransactionMonitoringEndpoint(transactionMonitoringService);
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionMonitoringBeanPostProcessor transactionMonitoringBeanPostProcessor(
            TransactionStatsCollector transactionStatsCollector, QueriesRecorder queriesCollector) {
        return new TransactionMonitoringBeanPostProcessor(transactionStatsCollector, queriesCollector);
    }

    @Bean
    @ConditionalOnMissingBean
    public QueriesRecorder queriesStatsCollector() {
        return new DefaultQueriesRecorder();
    }

    @Bean
    @ConditionalOnMissingBean
    public ProxyingDataSourceBeanPostProcessor transactionMonitoringDataSourceBeanPostProcessor(
            QueriesRecorder queriesCollector) {
        return new ProxyingDataSourceBeanPostProcessor(queriesCollector);
    }

    @Configuration
    @ConditionalOnClass(name = {"org.hibernate.Session", "ch.qos.logback.classic.LoggerContext"})
    @ConditionalOnProperty(
            prefix = "axelix.sbs.transaction.monitoring.in-memory-pagination-detection",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    static class LogbackInMemoryPaginationAppenderConfiguration {

        @EventListener(ApplicationReadyEvent.class)
        public void registerAppender() {
            new LogbackInMemoryPaginationAppenderRegistrar().register();
        }
    }
}
