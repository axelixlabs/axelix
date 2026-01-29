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
package com.axelixlabs.axelix.sbs.spring.transactions;

import java.time.Duration;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.common.api.TransactionMonitoringFeed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for {@link DefaultTransactionMonitoringService}
 *
 * TODO: Revisit design of this test.
 *
 * @since 22.01.2026
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest
@Import(TransactionMonitoringServiceTest.TransactionMonitoringIntegrationTestConfiguration.class)
public class TransactionMonitoringServiceTest {
    private final String PATH_PROPAGATION_TEST_HELPER = PropagationTestHelper.class.getName();
    private final String PATH_PROPAGATION_TEST_SERVICE = PropagationTestService.class.getName();

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PropagationTestHelper propagationTestHelper;

    @Autowired
    private PropagationTestService propagationTestService;

    @Autowired
    private TransactionMonitoringService transactionMonitoringService;

    @BeforeEach
    void cleanUp() {
        transactionMonitoringService.clearAllStats();
    }

    @Test
    void requiredPropagationIsMonitored() {
        for (int i = 0; i < 3; i++) {
            propagationTestService.testRequired("Rodriquez");
        }

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        var testRequiredMethod = monitoringFeed.entrypoints().stream()
                .filter(method -> PATH_PROPAGATION_TEST_SERVICE.equals(method.className())
                        && "testRequired".equals(method.methodName()))
                .findFirst();

        assertThat(testRequiredMethod).isPresent();
        assertThat(testRequiredMethod.get().executionStats()).isNotNull();
        assertThat(testRequiredMethod.get().executions().size()).isEqualTo(3);

        var nestedRequiresNewMethod = monitoringFeed.entrypoints().stream()
                .filter(method -> PATH_PROPAGATION_TEST_HELPER.equals(method.className())
                        && "testNestedRequiresNew".equals(method.methodName()))
                .findFirst();

        assertThat(nestedRequiresNewMethod).isPresent();
        assertThat(testRequiredMethod.get().executionStats()).isNotNull();
        assertThat(testRequiredMethod.get().executions().size()).isEqualTo(3);

        boolean hasRepositoryMethod = monitoringFeed.entrypoints().stream()
                .anyMatch(method -> method.methodName().contains("findByLastName"));
        assertThat(hasRepositoryMethod).isFalse();
    }

    @Test
    void requiresNewFromNonTransactionalIsMonitored() {
        propagationTestService.testFromNonTransactional("Rodriquez");

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.entrypoints()).hasSize(1);

        var requiresNewMethod = monitoringFeed.entrypoints().get(0);
        assertThat(requiresNewMethod.className()).isEqualTo(PATH_PROPAGATION_TEST_HELPER);
        assertThat(requiresNewMethod.methodName()).isEqualTo("testRequiresNew");

        boolean hasRepositoryMethod = monitoringFeed.entrypoints().stream()
                .anyMatch(method -> method.methodName().contains("findByLastName"));
        assertThat(hasRepositoryMethod).isFalse();
    }

    @Test
    void rollbackScenarioIsMonitored() {
        assertThatThrownBy(() -> propagationTestService.testRollbackScenario("Rodriquez"))
                .isInstanceOf(RuntimeException.class);

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        var rollbackMethod = monitoringFeed.entrypoints().stream()
                .filter(method -> PATH_PROPAGATION_TEST_SERVICE.equals(method.className())
                        && "testRollbackScenario".equals(method.methodName()))
                .findFirst();

        assertThat(rollbackMethod).isPresent();
        assertThat(rollbackMethod.get().executionStats()).isNotNull();
        assertThat(rollbackMethod.get().executions().size()).isEqualTo(1);

        var nestedMethod = monitoringFeed.entrypoints().stream()
                .filter(method -> PATH_PROPAGATION_TEST_HELPER.equals(method.className())
                        && "testNestedRequiresNew".equals(method.methodName()))
                .findFirst();

        assertThat(nestedMethod).isPresent();
        assertThat(nestedMethod.get().executionStats()).isNotNull();
        assertThat(nestedMethod.get().executions().size()).isEqualTo(1);
    }

    @Test
    void notSupportedPropagationIsNotMonitored() {
        propagationTestHelper.testNotSupported("Rodriquez");

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        boolean hasTestNotSupported = monitoringFeed.entrypoints().stream()
                .anyMatch(method -> "testNotSupported".equals(method.methodName()));
        assertThat(hasTestNotSupported)
                .as("'testNotSupported()' should not be tracked")
                .isFalse();

        boolean hasFindByLastName = monitoringFeed.entrypoints().stream()
                .anyMatch(method -> method.methodName().contains("findByLastName"));
        assertThat(hasFindByLastName).as("'findByLastName()' should be tracked").isTrue();

        assertThat(monitoringFeed.entrypoints()).hasSize(1);
    }

    // testSupports() should NOT be tracked (reuses test's transaction)
    @Test
    @Transactional
    void supportsPropagationWithExistingTransaction() {
        propagationTestService.testSupports("Rodriquez");

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.entrypoints()).isEmpty();
    }

    @Test
    void testSupportsWithoutTransaction() {
        propagationTestService.testSupportsWithoutTransaction();

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.entrypoints()).hasSize(0);
    }

    // testMandatory() should NOT be tracked (reuses test's transaction)
    @Test
    @Transactional
    void mandatoryPropagationIsMonitored() {
        propagationTestHelper.testMandatory("Rodriquez");

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.entrypoints()).isEmpty();
    }

    @Test
    void mandatoryWithoutTransaction_shouldThrowException() {
        assertThatThrownBy(() -> propagationTestHelper.testMandatory("Rodriquez"))
                .isInstanceOf(IllegalTransactionStateException.class)
                .hasMessageContaining("No existing transaction");

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.entrypoints()).isEmpty();
    }

    // internalMethod() should NOT be tracked (bypasses proxy)
    @Test
    void selfInvocationProblemIsDetectedIsNotMonitored() {
        propagationTestHelper.testSelfInvocation();

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.entrypoints()).hasSize(1);

        boolean hasTestSelfInvocation = monitoringFeed.entrypoints().stream()
                .anyMatch(method -> PATH_PROPAGATION_TEST_HELPER.equals(method.className())
                        && "testSelfInvocation".equals(method.methodName()));
        assertThat(hasTestSelfInvocation).isTrue();
    }

    @Test
    void correctSelfInvocationViaProxyIsMonitored() {
        propagationTestHelper.testCorrectSelfInvocation();

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.entrypoints()).hasSize(2);

        boolean hasTestCorrectSelfInvocation = monitoringFeed.entrypoints().stream()
                .anyMatch(method -> PATH_PROPAGATION_TEST_HELPER.equals(method.className())
                        && "testCorrectSelfInvocation".equals(method.methodName()));
        boolean hasRequiresNewViaProxy = monitoringFeed.entrypoints().stream()
                .anyMatch(method -> PATH_PROPAGATION_TEST_HELPER.equals(method.className())
                        && "requiresNewViaProxy".equals(method.methodName()));

        assertThat(hasTestCorrectSelfInvocation)
                .as("'testCorrectSelfInvocation()' should be tracked (creates new transaction)")
                .isTrue();
        assertThat(hasRequiresNewViaProxy)
                .as("'requiresNewViaProxy()' should be tracked (REQUIRES_NEW)")
                .isTrue();
    }

    @Test
    void nestedPropagationIsMonitored() {
        propagationTestHelper.testNested();

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.entrypoints()).hasSize(1);

        boolean hasTestNested = monitoringFeed.entrypoints().stream()
                .anyMatch(method -> PATH_PROPAGATION_TEST_HELPER.equals(method.className())
                        && "testNested".equals(method.methodName()));

        assertThat(hasTestNested).isTrue();
    }

    // Overrides default propagation with SUPPORTS
    @Test
    void shouldNotCreateTransactionWhenPropagationIsOverridden() {
        ownerRepository.findAll();

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.entrypoints()).isEmpty();
    }

    @TestConfiguration
    @EnableJpaRepositories(
            basePackageClasses = TransactionMonitoringServiceTest.OwnerRepository.class,
            considerNestedRepositories = true)
    @EntityScan(basePackageClasses = TransactionMonitoringServiceTest.Owner.class)
    static class TransactionMonitoringIntegrationTestConfiguration {

        @Bean
        public TransactionMonitoringService transactionMonitoringService(
                TransactionStatsCollector transactionStatsCollector) {
            return new DefaultTransactionMonitoringService(transactionStatsCollector);
        }

        @Bean
        public TransactionStatsCollector transactionStatsCollector() {
            return new DefaultTransactionStatsCollector(30, Duration.ofSeconds(10000));
        }

        @Bean
        public TransactionMonitoringBeanPostProcessor transactionMonitoringBeanPostProcessor(
                TransactionStatsCollector transactionStatsCollector) {
            return new TransactionMonitoringBeanPostProcessor(transactionStatsCollector);
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

    /**
     * Simple {@link Entity} for test.
     */
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

    /**
     * Simple {@link JpaRepository} for test.
     */
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

    /**
     * Test helper for demonstrating transaction propagation scenarios and monitoring.
     * Used in integration tests to verify transaction tracking behavior.
     */
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

        @Transactional(propagation = Propagation.NESTED)
        public void testNested() {
            ownerRepository.findByLastName("Schroeder");
        }

        @Transactional(propagation = Propagation.MANDATORY)
        public void testMandatory(String lastName) {
            ownerRepository.findByLastName(lastName);
        }

        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        public void testNotSupported(String lastName) {
            ownerRepository.findByLastName(lastName);
        }

        @Transactional
        public void testSelfInvocation() {
            internalMethod();
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void internalMethod() {
            ownerRepository.findByLastName("Black");
        }

        @Transactional
        public void testCorrectSelfInvocation() {
            self.requiresNewViaProxy();
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void requiresNewViaProxy() {
            ownerRepository.findByLastName("White");
        }
    }

    /**
     * Test service for demonstrating transaction propagation scenarios and monitoring.
     * Used in integration tests to verify transaction tracking behavior.
     */
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

        public void testFromNonTransactional(String lastName) {
            helperService.testRequiresNew(lastName);
        }

        @Transactional
        protected void testRollbackScenario(String lastName) {
            ownerRepository.findByLastName(lastName);
            helperService.testNestedRequiresNew();
            throw new RuntimeException("Test rollback");
        }

        @Transactional(propagation = Propagation.SUPPORTS)
        public void testSupports(String lastName) {
            ownerRepository.findByLastName(lastName);
        }

        @Transactional(propagation = Propagation.SUPPORTS)
        public void testSupportsWithoutTransaction() {}
    }
}
