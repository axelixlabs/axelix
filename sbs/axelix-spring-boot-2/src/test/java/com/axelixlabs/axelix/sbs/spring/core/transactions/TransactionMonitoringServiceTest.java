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

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.common.api.TransactionMonitoringFeed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for {@link DefaultTransactionMonitoringService}
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

        var testRequiredMethod = monitoringFeed.getEntrypoints().stream()
                .filter(method -> PATH_PROPAGATION_TEST_SERVICE.equals(method.getClassName())
                        && "testRequired".equals(method.getMethodName()))
                .findFirst();

        assertThat(testRequiredMethod).isPresent();
        assertThat(testRequiredMethod.get().getExecutionStats()).isNotNull();
        assertThat(testRequiredMethod.get().getExecutions().size()).isEqualTo(3);

        var nestedRequiresNewMethod = monitoringFeed.getEntrypoints().stream()
                .filter(method -> PATH_PROPAGATION_TEST_HELPER.equals(method.getClassName())
                        && "testNestedRequiresNew".equals(method.getMethodName()))
                .findFirst();

        assertThat(nestedRequiresNewMethod).isPresent();
        assertThat(testRequiredMethod.get().getExecutionStats()).isNotNull();
        assertThat(testRequiredMethod.get().getExecutions().size()).isEqualTo(3);

        boolean hasRepositoryMethod = monitoringFeed.getEntrypoints().stream()
                .anyMatch(method -> method.getMethodName().contains("findByLastName"));
        assertThat(hasRepositoryMethod).isFalse();
    }

    @Test
    void requiresNewFromNonTransactionalIsMonitored() {
        propagationTestService.testFromNonTransactional("Rodriquez");

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.getEntrypoints()).hasSize(1);

        var requiresNewMethod = monitoringFeed.getEntrypoints().get(0);
        assertThat(requiresNewMethod.getClassName()).isEqualTo(PATH_PROPAGATION_TEST_HELPER);
        assertThat(requiresNewMethod.getMethodName()).isEqualTo("testRequiresNew");

        boolean hasRepositoryMethod = monitoringFeed.getEntrypoints().stream()
                .anyMatch(method -> method.getMethodName().contains("findByLastName"));
        assertThat(hasRepositoryMethod).isFalse();
    }

    @Test
    void rollbackScenarioIsMonitored() {
        assertThatThrownBy(() -> propagationTestService.testRollbackScenario("Rodriquez"))
                .isInstanceOf(RuntimeException.class);

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        var rollbackMethod = monitoringFeed.getEntrypoints().stream()
                .filter(method -> PATH_PROPAGATION_TEST_SERVICE.equals(method.getClassName())
                        && "testRollbackScenario".equals(method.getMethodName()))
                .findFirst();

        assertThat(rollbackMethod).isPresent();
        assertThat(rollbackMethod.get().getExecutionStats()).isNotNull();
        assertThat(rollbackMethod.get().getExecutions().size()).isEqualTo(1);

        var nestedMethod = monitoringFeed.getEntrypoints().stream()
                .filter(method -> PATH_PROPAGATION_TEST_HELPER.equals(method.getClassName())
                        && "testNestedRequiresNew".equals(method.getMethodName()))
                .findFirst();

        assertThat(nestedMethod).isPresent();
        assertThat(nestedMethod.get().getExecutionStats()).isNotNull();
        assertThat(nestedMethod.get().getExecutions().size()).isEqualTo(1);
    }

    @Test
    void notSupportedPropagationIsNotMonitored() {
        propagationTestHelper.testNotSupported("Rodriquez");

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        boolean hasTestNotSupported = monitoringFeed.getEntrypoints().stream()
                .anyMatch(method -> "testNotSupported".equals(method.getMethodName()));
        assertThat(hasTestNotSupported)
                .as("'testNotSupported()' should not be tracked")
                .isFalse();

        boolean hasFindByLastName = monitoringFeed.getEntrypoints().stream()
                .anyMatch(method -> method.getMethodName().contains("findByLastName"));
        assertThat(hasFindByLastName).as("'findByLastName()' should be tracked").isTrue();

        assertThat(monitoringFeed.getEntrypoints()).hasSize(1);
    }

    // testSupports() should NOT be tracked (reuses test's transaction)
    @Test
    @Transactional
    void supportsPropagationWithExistingTransaction() {
        propagationTestService.testSupports("Rodriquez");

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.getEntrypoints()).isEmpty();
    }

    @Test
    void testSupportsWithoutTransaction() {
        propagationTestService.testSupportsWithoutTransaction();

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.getEntrypoints()).hasSize(0);
    }

    // testMandatory() should NOT be tracked (reuses test's transaction)
    @Test
    @Transactional
    void mandatoryPropagationIsMonitored() {
        propagationTestHelper.testMandatory("Rodriquez");

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.getEntrypoints()).isEmpty();
    }

    @Test
    void mandatoryWithoutTransaction_shouldThrowException() {
        assertThatThrownBy(() -> propagationTestHelper.testMandatory("Rodriquez"))
                .isInstanceOf(IllegalTransactionStateException.class)
                .hasMessageContaining("No existing transaction");

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.getEntrypoints()).isEmpty();
    }

    // internalMethod() should NOT be tracked (bypasses proxy)
    @Test
    void selfInvocationProblemIsDetectedIsNotMonitored() {
        propagationTestHelper.testSelfInvocation();

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.getEntrypoints()).hasSize(1);

        boolean hasTestSelfInvocation = monitoringFeed.getEntrypoints().stream()
                .anyMatch(method -> PATH_PROPAGATION_TEST_HELPER.equals(method.getClassName())
                        && "testSelfInvocation".equals(method.getMethodName()));
        assertThat(hasTestSelfInvocation).isTrue();
    }

    @Test
    void correctSelfInvocationViaProxyIsMonitored() {
        propagationTestHelper.testCorrectSelfInvocation();

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.getEntrypoints()).hasSize(2);

        boolean hasTestCorrectSelfInvocation = monitoringFeed.getEntrypoints().stream()
                .anyMatch(method -> PATH_PROPAGATION_TEST_HELPER.equals(method.getClassName())
                        && "testCorrectSelfInvocation".equals(method.getMethodName()));
        boolean hasRequiresNewViaProxy = monitoringFeed.getEntrypoints().stream()
                .anyMatch(method -> PATH_PROPAGATION_TEST_HELPER.equals(method.getClassName())
                        && "requiresNewViaProxy".equals(method.getMethodName()));

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

        assertThat(monitoringFeed.getEntrypoints()).hasSize(1);

        boolean hasTestNested = monitoringFeed.getEntrypoints().stream()
                .anyMatch(method -> PATH_PROPAGATION_TEST_HELPER.equals(method.getClassName())
                        && "testNested".equals(method.getMethodName()));

        assertThat(hasTestNested).isTrue();
    }

    // Overrides default propagation with SUPPORTS
    @Test
    void shouldNotCreateTransactionWhenPropagationIsOverridden() {
        ownerRepository.findAll();

        TransactionMonitoringFeed monitoringFeed = transactionMonitoringService.getMonitoringFeed();

        assertThat(monitoringFeed.getEntrypoints()).isEmpty();
    }

    @TestConfiguration
    @EnableJpaRepositories(basePackageClasses = OwnerRepository.class, considerNestedRepositories = true)
    @EntityScan(basePackageClasses = Owner.class)
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
}
