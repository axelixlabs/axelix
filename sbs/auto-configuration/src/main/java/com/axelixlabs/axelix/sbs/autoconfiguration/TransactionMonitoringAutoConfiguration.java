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
package com.axelixlabs.axelix.sbs.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

import com.axelixlabs.axelix.sbs.spring.config.TransactionMonitoringConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.transactions.DefaultTransactionMonitoringService;
import com.axelixlabs.axelix.sbs.spring.transactions.DefaultTransactionStatsCollector;
import com.axelixlabs.axelix.sbs.spring.transactions.TransactionMonitoringBeanPostProcessor;
import com.axelixlabs.axelix.sbs.spring.transactions.TransactionMonitoringEndpoint;
import com.axelixlabs.axelix.sbs.spring.transactions.TransactionMonitoringService;
import com.axelixlabs.axelix.sbs.spring.transactions.TransactionStatsCollector;

/**
 * Auto-configuration for Transaction Monitoring infrastructure.
 *
 * @since 21.01.2026
 * @author Nikita Kirillov
 */
@AutoConfiguration
@EnableConfigurationProperties(TransactionMonitoringConfigurationProperties.class)
public class TransactionMonitoringAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TransactionStatsCollector transactionStatsCollector(
            TransactionMonitoringConfigurationProperties properties) {
        Assert.isTrue(properties.maxTransactionsPerMethod() > 0, "maxTransactionsPerMethod must be positive");
        Assert.isTrue(properties.cleanupInterval().toSeconds() > 0L, "cleanupInterval must be non-negative");
        return new DefaultTransactionStatsCollector(
                properties.maxTransactionsPerMethod(), properties.cleanupInterval());
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
            TransactionStatsCollector transactionStatsCollector) {
        return new TransactionMonitoringBeanPostProcessor(transactionStatsCollector);
    }
}
