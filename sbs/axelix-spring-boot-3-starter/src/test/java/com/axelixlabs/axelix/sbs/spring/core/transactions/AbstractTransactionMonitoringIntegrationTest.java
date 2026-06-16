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

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;
import com.axelixlabs.axelix.sbs.spring.core.metrics.DefaultAxelixMetricsPublisher;

/**
 * Base class for the transaction-monitoring integration tests that exercise the
 * {@link TransactionMonitoringBeanPostProcessor} / {@link ProxyingDataSourceBeanPostProcessor}
 * machinery. It owns the {@link SpringBootTest} declaration and the shared {@link TestConfiguration},
 * so that all subclasses resolve to an identical merged context configuration and therefore share a
 * single cached {@link org.springframework.context.ApplicationContext}.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 * @author Artemiy Degtyarev
 */
@SpringBootTest
@Import(AbstractTransactionMonitoringIntegrationTest.SharedTransactionTestConfiguration.class)
abstract class AbstractTransactionMonitoringIntegrationTest {

    @TestConfiguration
    @EnableJpaRepositories(basePackageClasses = OwnerRepository.class, considerNestedRepositories = true)
    @EntityScan(basePackageClasses = Owner.class)
    static class SharedTransactionTestConfiguration {

        @Bean
        public TransactionStatsCollector transactionStatsCollector() {
            return new DefaultTransactionStatsCollector(30);
        }

        @Bean
        public TransactionMonitoringBeanPostProcessor transactionMonitoringBeanPostProcessor(
                TransactionStatsCollector transactionStatsCollector,
                QueriesRecorder queriesCollector,
                ObjectProvider<AxelixMetricsPublisher> axelixMetricsPublisherObjectProvider) {
            return new TransactionMonitoringBeanPostProcessor(
                    transactionStatsCollector, queriesCollector, axelixMetricsPublisherObjectProvider);
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
        public PropagationTestHelper propagationTestHelper(
                OwnerRepository ownerRepository, @Lazy PropagationTestHelper self) {
            return new PropagationTestHelper(ownerRepository, self);
        }

        @Bean
        public PropagationTestService propagationTestService(
                OwnerRepository ownerRepository, PropagationTestHelper helper) {
            return new PropagationTestService(ownerRepository, helper);
        }

        @Bean
        public AxelixMetricsPublisher axelixMetricsPublisher(MeterRegistry meterRegistry) {
            return new DefaultAxelixMetricsPublisher(meterRegistry);
        }
    }

    @Entity
    static class Owner {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String lastName;

        public Long getId() {
            return id;
        }

        public String getLastName() {
            return lastName;
        }
    }

    interface OwnerRepository extends JpaRepository<Owner, Long> {

        @Transactional
        default Owner findByLastName(String lastName) {
            return new Owner();
        }

        @Transactional(propagation = Propagation.SUPPORTS)
        default List<Owner> findAll() {
            return List.of(new Owner());
        }
    }

    static class PropagationTestHelper {

        private final OwnerRepository ownerRepository;
        private final PropagationTestHelper self;

        public PropagationTestHelper(OwnerRepository ownerRepository, @Lazy PropagationTestHelper self) {
            this.ownerRepository = ownerRepository;
            this.self = self;
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void testRequiresNew(String lastName) {
            ownerRepository.findByLastName(lastName);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void testNestedRequiresNew() {
            ownerRepository.findByLastName("Franklin");
        }

        @Transactional(propagation = Propagation.MANDATORY)
        public void testMandatory(String lastName) {
            ownerRepository.findByLastName(lastName);
        }
    }

    static class PropagationTestService {

        private final OwnerRepository ownerRepository;
        private final PropagationTestHelper helperService;

        public PropagationTestService(OwnerRepository ownerRepository, PropagationTestHelper helperService) {
            this.ownerRepository = ownerRepository;
            this.helperService = helperService;
        }

        @Transactional(propagation = Propagation.REQUIRED)
        void testRequired(String lastName) {
            ownerRepository.findByLastName(lastName);
            helperService.testNestedRequiresNew();
        }
    }
}
