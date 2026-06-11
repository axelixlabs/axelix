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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;

/**
 * Base class for the transactional-related integration tests.
 *
 * <p>Every beans test extends this class and declares no context-affecting annotations of its own
 * (no {@code @SpringBootTest}, {@code @TestPropertySource}, {@code @Import} or nested
 * {@code @TestConfiguration} classes). As a result, all the transactional tests produce an identical
 * merged context configuration and therefore share a single cached Spring application context,
 * which is only started once for the whole test run.
 *
 * <p>{@link TransactionMonitoringEndpointTest} is intentionally not part of this hierarchy.
 *
 * <p>The annotations below are the union of the configuration previously declared by the
 * individual transactional tests.
 */
@SpringBootTest
@Import({AbstractTransactionalIntegrationTest.TransactionalConfiguration.class})
public abstract class AbstractTransactionalIntegrationTest {
    @TestConfiguration
    @EnableJpaRepositories(basePackageClasses = OwnerRepository.class, considerNestedRepositories = true)
    @EntityScan(basePackageClasses = Owner.class)
    static class TransactionalConfiguration {
        @Bean
        public QueriesRecorder queriesRecorder() {
            return new DefaultQueriesRecorder();
        }

        @Bean
        public ProxyingDataSourceBeanPostProcessor proxyingDataSourceBeanPostProcessor(
            QueriesRecorder queriesRecorder) {
            return new ProxyingDataSourceBeanPostProcessor(queriesRecorder);
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
        public PropagationTestHelper propagationTestHelper(
            OwnerRepository ownerRepository, @Lazy PropagationTestHelper self) {
            return new PropagationTestHelper(ownerRepository, self);
        }

        @Bean
        public PropagationTestService propagationTestService(
            OwnerRepository ownerRepository, PropagationTestHelper helper) {
            return new PropagationTestService(ownerRepository, helper);
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
