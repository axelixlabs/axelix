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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link TransactionMonitoringBeanPostProcessor}.
 *
 * TODO: Revisit design of this test.
 *
 * @since 22.01.2026
 * @author Nikita Kirillov
 */
@SpringBootTest
@Disabled
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
    }
}
