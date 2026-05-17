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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;

import com.axelixlabs.axelix.sbs.spring.core.shared.AbstractEndpointTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link TransactionMonitoringBeanPostProcessor}.
 *
 * @since 22.01.2026
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
class TransactionMonitoringBeanPostProcessorTest extends AbstractEndpointTest {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PropagationTestHelper propagationTestHelper;

    @Autowired
    private TransactionMonitoringBeanPostProcessor transactionMonitoringBeanPostProcessor;

    private Map<MethodClassKey, Propagation> propagationCache;

    private List<Object> transactionalBeans;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        propagationCache = (Map<MethodClassKey, Propagation>)
                ReflectionTestUtils.getField(transactionMonitoringBeanPostProcessor, "propagationCache");

        transactionalBeans = List.of(propagationTestHelper, ownerRepository);
    }

    @Test
    void testServicesAreProxied() {
        // PropagationTestHelper is a concrete class, so the proxy must be CGLIB rather than a JDK proxy.
        assertThat(AopUtils.isCglibProxy(propagationTestHelper)).isTrue();
        assertThat(AopUtils.isJdkDynamicProxy(propagationTestHelper)).isFalse();
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

        // A @Transactional method on a concrete class.
        Method saveRequiresNew = PropagationTestHelper.class.getDeclaredMethod("saveRequiresNew", String.class);
        MethodClassKey key = new MethodClassKey(saveRequiresNew, PropagationTestHelper.class);
        assertThat(propagationCache).containsKey(key);

        // A @Transactional default method on a Spring Data repository interface.
        Method findByLastName = OwnerRepository.class.getDeclaredMethod("findByLastName", String.class);
        key = new MethodClassKey(findByLastName, OwnerRepository.class);
        assertThat(propagationCache).containsKey(key);
    }
}
