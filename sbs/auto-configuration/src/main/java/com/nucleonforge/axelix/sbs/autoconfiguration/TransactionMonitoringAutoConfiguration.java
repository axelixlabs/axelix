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
package com.nucleonforge.axelix.sbs.autoconfiguration;

import com.nucleonforge.axelix.sbs.spring.transactions.TransactionMonitoringBeanPostProcessor;
import com.nucleonforge.axelix.sbs.spring.transactions.TransactionMonitoringEndpoint;
import com.nucleonforge.axelix.sbs.spring.transactions.TransactionMonitoringService;
import com.nucleonforge.axelix.sbs.spring.transactions.TransactionStatsCollector;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 *
 *
 * @since 21.01.2026
 * @author Nikita Kirillov
 */
@AutoConfiguration
public class TransactionMonitoringAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TransactionStatsCollector transactionStatsCollector(
        final @Value("${transaction.monitoring.max-transactions-per-method:30}") Integer maxTransactionsPerMethod) {
        return new TransactionStatsCollector(maxTransactionsPerMethod);
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionMonitoringService transactionMonitoringService(
            TransactionStatsCollector transactionStatsCollector) {
        return new TransactionMonitoringService(transactionStatsCollector);
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
