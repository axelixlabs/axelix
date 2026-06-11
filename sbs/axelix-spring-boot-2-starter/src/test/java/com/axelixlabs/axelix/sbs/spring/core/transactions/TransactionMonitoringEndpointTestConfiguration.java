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
package com.axelixlabs.axelix.sbs.spring.core.transactions;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringEndpointTest.Owner;
import com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringEndpointTest.OwnerRepository;
import com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringEndpointTest.Pet;
import com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringEndpointTest.PropagationTestHelper;

/**
 * Test configuration for {@link TransactionMonitoringEndpointTest}, part of the shared endpoint
 * test context.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@TestConfiguration
@EnableJpaRepositories(basePackageClasses = OwnerRepository.class, considerNestedRepositories = true)
@EntityScan(basePackageClasses = {Owner.class, Pet.class})
public class TransactionMonitoringEndpointTestConfiguration {

    @Bean
    public TransactionMonitoringEndpoint transactionMonitoringEndpoint(
            TransactionMonitoringService transactionMonitoringService) {
        return new TransactionMonitoringEndpoint(transactionMonitoringService);
    }

    @Bean
    public TransactionMonitoringService transactionMonitoringService(
            TransactionStatsCollector transactionStatsCollector) {
        return new DefaultTransactionMonitoringService(transactionStatsCollector);
    }

    @Bean
    public TransactionStatsCollector transactionStatsCollector() {
        return new DefaultTransactionStatsCollector(30);
    }

    @Bean
    public TransactionMonitoringBeanPostProcessor transactionMonitoringBeanPostProcessor(
            TransactionStatsCollector transactionStatsCollector, QueriesRecorder queriesCollector) {
        return new TransactionMonitoringBeanPostProcessor(transactionStatsCollector, queriesCollector);
    }

    @Bean
    public QueriesRecorder queriesStatsCollector() {
        return new DefaultQueriesRecorder();
    }

    @Bean
    public ProxyingDataSourceBeanPostProcessor transactionMonitoringDataSourceBeanPostProcessor(
            QueriesRecorder queriesCollector) {
        return new ProxyingDataSourceBeanPostProcessor(queriesCollector);
    }

    @Bean
    public PropagationTestHelper propagationTestHelper(OwnerRepository ownerRepository) {
        return new PropagationTestHelper(ownerRepository);
    }
}
