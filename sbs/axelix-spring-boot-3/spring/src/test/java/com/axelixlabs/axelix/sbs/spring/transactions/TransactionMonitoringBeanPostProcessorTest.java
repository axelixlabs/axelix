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

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link TransactionMonitoringBeanPostProcessor}.
 *
 * TODO: Revisit design of this test.
 *
 * @since 22.01.2026
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest
@Import(TransactionMonitoringBeanPostProcessorTest.TransactionMonitoringBeanPostProcessorTestConfiguration.class)
class TransactionMonitoringBeanPostProcessorTest {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PropagationTestHelper propagationTestHelper;

    @Autowired
    private PropagationTestService propagationTestService;

    @Autowired
    private TransactionMonitoringBeanPostProcessor transactionMonitoringBeanPostProcessor;

    private Map<MethodClassKey, Propagation> propagationCache;

    private List<Object> transactionalBeans;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        propagationCache = (Map<MethodClassKey, Propagation>)
                ReflectionTestUtils.getField(transactionMonitoringBeanPostProcessor, "propagationCache");

        transactionalBeans = List.of(propagationTestService, propagationTestHelper, ownerRepository);
    }

    @Test
    void testServicesAreProxied() {
        assertThat(AopUtils.isCglibProxy(propagationTestHelper)).isTrue();
        assertThat(AopUtils.isCglibProxy(propagationTestService)).isTrue();

        assertThat(AopUtils.isJdkDynamicProxy(propagationTestHelper)).isFalse();
        assertThat(AopUtils.isJdkDynamicProxy(propagationTestService)).isFalse();
    }

    @Test
    void testAllTransactionalBeansHaveMonitoringAdvisor() {
        for (Object bean : transactionalBeans) {
            List<Advisor> advisors = Arrays.asList(((Advised) bean).getAdvisors());

            boolean hasMonitoringInterceptor = advisors.stream()
                    .anyMatch(advisor -> advisor.getAdvice() instanceof TransactionMonitoringInterceptor);

            assertThat(hasMonitoringInterceptor).isTrue();
        }
    }

    @Test
    void testCachesAreFilled() throws NoSuchMethodException {
        assertThat(propagationCache).isNotEmpty();

        Method testRequired = PropagationTestService.class.getDeclaredMethod("testRequired", String.class);
        MethodClassKey key = new MethodClassKey(testRequired, PropagationTestService.class);

        assertThat(propagationCache).containsKey(key);

        Method testFromNonTransactional = PropagationTestHelper.class.getMethod("testMandatory", String.class);
        key = new MethodClassKey(testFromNonTransactional, PropagationTestHelper.class);

        assertThat(propagationCache).containsKey(key);
    }

    @TestConfiguration
    @EnableJpaRepositories(
            basePackageClasses = TransactionMonitoringBeanPostProcessorTest.OwnerRepository.class,
            considerNestedRepositories = true)
    @EntityScan(basePackageClasses = TransactionMonitoringBeanPostProcessorTest.Owner.class)
    static class TransactionMonitoringBeanPostProcessorTestConfiguration {

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
