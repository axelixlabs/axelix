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
package com.axelixlabs.axelix.sbs.spring.core.persistence;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.NoOpTransactionStatsCollector;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAttributesRegistry;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionDefinitionAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Integration test for {@link TransactionMonitoringBeanPostProcessor}.
 *
 * @since 22.01.2026
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Artemiy Degtyarev
 */
class TransactionMonitoringBeanPostProcessorTest extends AbstractTransactionMonitoringSharedContextTest {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PropagationTestHelper propagationTestHelper;

    @Autowired
    private PropagationTestService propagationTestService;

    @Autowired
    private TransactionMonitoringBeanPostProcessor transactionMonitoringBeanPostProcessor;

    @Autowired
    private TransactionAttributesRegistry transactionAttributesRegistry;

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
    void testSpringProxyingMechanismIsNotBroken() {
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

    @Test
    void testAttributesRegistryCapturesDeclaredTransactionAttributes() throws NoSuchMethodException {
        Method testRequired = PropagationTestService.class.getDeclaredMethod("testRequired", String.class);
        MethodClassKey key = new MethodClassKey(testRequired, PropagationTestService.class);

        TransactionDefinitionAttributes attributes = transactionAttributesRegistry.get(key);

        assertThat(attributes).isNotNull();
        assertThat(attributes.getPropagation()).isEqualTo("REQUIRED");
        assertThat(attributes.getIsolation()).isEqualTo("DEFAULT");
        assertThat(attributes.isReadOnly()).isFalse();
    }

    @Test
    void testFinalClassBeanIsSkippedInsteadOfThrowing() {
        TransactionMonitoringBeanPostProcessor processor = newStandaloneProcessor();
        FinalGreeterImpl bean = new FinalGreeterImpl();

        assertThatCode(() -> {
                    Object result = processor.postProcessAfterInitialization(bean, "finalGreeter");
                    assertThat(result).isSameAs(bean);
                })
                .doesNotThrowAnyException();
    }

    @Test
    void testFinalClassAlreadyWrappedInJdkProxyIsStillMonitored() {
        TransactionMonitoringBeanPostProcessor processor = newStandaloneProcessor();

        Object jdkProxiedBean = new ProxyFactory(new FinalGreeterImpl()).getProxy();
        assertThat(AopUtils.isJdkDynamicProxy(jdkProxiedBean)).isTrue();

        Object result = processor.postProcessAfterInitialization(jdkProxiedBean, "jdkProxiedFinalGreeter");

        assertThat(result).isSameAs(jdkProxiedBean);
        assertThat(AopUtils.isAopProxy(result)).isTrue();
    }

    @Test
    void testBeanWithFinalMethodIsSkippedFromMonitoring() {
        TransactionMonitoringBeanPostProcessor processor = newStandaloneProcessor();
        FinalMethodGreeterImpl bean = new FinalMethodGreeterImpl();

        Object result = processor.postProcessAfterInitialization(bean, "finalMethodGreeter");

        assertThat(result).isSameAs(bean);
    }

    private TransactionMonitoringBeanPostProcessor newStandaloneProcessor() {
        ObjectProvider<AxelixMetricsPublisher> noopProvider = new ObjectProvider<>() {
            @Override
            public AxelixMetricsPublisher getObject() {
                return null;
            }

            @Override
            public AxelixMetricsPublisher getObject(Object... args) {
                return null;
            }

            @Override
            public AxelixMetricsPublisher getIfAvailable() {
                return null;
            }

            @Override
            public AxelixMetricsPublisher getIfUnique() {
                return null;
            }
        };

        return new TransactionMonitoringBeanPostProcessor(
                new NoOpTransactionStatsCollector(),
                noopProvider,
                new TransactionAccessor(),
                new TransactionAttributesRegistry());
    }

    interface Greeter {
        String greet();
    }

    static final class FinalGreeterImpl implements Greeter {
        @Override
        @Transactional
        public String greet() {
            return "hi";
        }
    }

    static class FinalMethodGreeterImpl implements Greeter {
        @Override
        @Transactional
        public final String greet() {
            return "hi";
        }
    }
}
