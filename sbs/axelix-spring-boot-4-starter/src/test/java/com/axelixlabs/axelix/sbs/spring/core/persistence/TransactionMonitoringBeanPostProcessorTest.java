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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.DynamicMethodMatcherPointcut;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.util.ReflectionUtils;

import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.DefaultTransactionStatsCollector;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.NoOpTransactionStatsCollector;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAttributesRegistry;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionDefinitionAttributes;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;

import static org.assertj.core.api.Assertions.assertThat;

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
    private PetRepository petRepository;

    @Autowired
    private PropagationTestHelper propagationTestHelper;

    @Autowired
    private PropagationTestService propagationTestService;

    @Autowired
    private ScopedService scopedService;

    @Autowired
    private SupportsOnlyService supportsOnlyService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TransactionAttributesRegistry transactionAttributesRegistry;

    private List<Object> transactionalBeans;

    @BeforeEach
    void setup() {
        transactionalBeans = List.of(propagationTestService, propagationTestHelper, ownerRepository, petRepository);
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
            assertThat(hasMonitoringInterceptor(bean)).isTrue();
        }
    }

    @Test
    void testMonitoringAdvisorIsAddedRightBeforeTransactionAdvisor() {
        assertMonitoringAdvisorIsRightBeforeTransactionAdvisor((Advised) propagationTestService);
        assertMonitoringAdvisorIsRightBeforeTransactionAdvisor((Advised) petRepository);
    }

    /**
     * {@code OwnerRepository} has a {@code @Cacheable} method, so Spring wraps the repository proxy - the one
     * holding the transaction interceptor - into one more, caching, proxy.
     */
    @Test
    void testMonitoringAdvisorIsAddedToInnerProxyHoldingTransactionAdvisor() {
        Advised cachingProxy = (Advised) ownerRepository;
        Advised repositoryProxy = (Advised) AopProxyUtils.getSingletonTarget(cachingProxy);

        assertThat(adviceTypes(cachingProxy)).doesNotContain(TransactionMonitoringInterceptor.class);
        assertMonitoringAdvisorIsRightBeforeTransactionAdvisor(repositoryProxy);
    }

    /**
     * A scoped bean is two beans: the scoped proxy, which merely delegates, and its target. Only the latter
     * may be monitored, otherwise every transaction would be recorded twice.
     */
    @Test
    void testScopedProxyIsNotMonitoredButItsTargetIs() {
        assertThat(hasMonitoringInterceptor(scopedService)).isFalse();
        assertThat(hasMonitoringInterceptor(applicationContext.getBean("scopedTarget.scopedService")))
                .isTrue();
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
    void testBeanThatIsNotSpringProxyIsLeftUntouched() {
        TransactionMonitoringBeanPostProcessor processor = newStandaloneProcessor();
        FinalGreeterImpl bean = new FinalGreeterImpl();

        Object result = processor.postProcessAfterInitialization(bean, "finalGreeter");
        assertThat(result).isSameAs(bean);
    }

    @Test
    void testFinalClassAlreadyWrappedInJdkProxyIsStillMonitored() {
        TransactionMonitoringBeanPostProcessor processor = newStandaloneProcessor();

        ProxyFactory proxyFactory = new ProxyFactory(new FinalGreeterImpl());
        proxyFactory.addAdvice(new TransactionInterceptor(
                (TransactionManager) new JdbcTransactionManager(), new AnnotationTransactionAttributeSource()));
        Object jdkProxiedBean = proxyFactory.getProxy();
        assertThat(AopUtils.isJdkDynamicProxy(jdkProxiedBean)).isTrue();

        Object result = processor.postProcessAfterInitialization(jdkProxiedBean, "jdkProxiedFinalGreeter");

        // Already Advised, so the monitoring advisor is added directly to it rather than wrapping it again.
        assertThat(result).isSameAs(jdkProxiedBean);
        assertThat(hasMonitoringInterceptor(result)).isTrue();
    }

    /**
     * An opaque Spring proxy does not expose its advisors, so it cannot even be inspected for transactions.
     */
    @Test
    void testOpaqueSpringProxyIsLeftUntouched() {
        TransactionMonitoringBeanPostProcessor processor = newStandaloneProcessor();

        ProxyFactory proxyFactory = new ProxyFactory(new FinalGreeterImpl());
        proxyFactory.setOpaque(true);
        proxyFactory.addAdvice(new TransactionInterceptor(
                (TransactionManager) new JdbcTransactionManager(), new AnnotationTransactionAttributeSource()));
        Object opaqueProxy = proxyFactory.getProxy();
        assertThat(opaqueProxy).isNotInstanceOf(Advised.class);

        Object result = processor.postProcessAfterInitialization(opaqueProxy, "opaqueGreeter");

        assertThat(result).isSameAs(opaqueProxy);
    }

    /**
     * Whoever froze the proxy asked for its advisor chain to stay as it is.
     */
    @Test
    void testFrozenProxyIsLeftUntouched() {
        TransactionMonitoringBeanPostProcessor processor = newStandaloneProcessor();

        ProxyFactory proxyFactory = new ProxyFactory(new PlainGreeterImpl());
        proxyFactory.addAdvice(new TransactionInterceptor(
                (TransactionManager) new NoOpTransactionManager(), new AnnotationTransactionAttributeSource()));
        proxyFactory.setFrozen(true);
        Object frozenProxy = proxyFactory.getProxy();

        Object result = processor.postProcessAfterInitialization(frozenProxy, "frozenGreeter");

        assertThat(result).isSameAs(frozenProxy);
        assertThat(adviceTypes((Advised) result)).containsExactly(TransactionInterceptor.class);
    }

    /**
     * The proxy holding the transaction interceptor is frozen, the one around it is not. Monitoring could only
     * go to the outer one, away from the transaction interceptor - so the bean is not monitored at all.
     */
    @Test
    void testFrozenInnerProxyIsLeftUntouchedEvenWhenOuterProxyIsNotFrozen() {
        TransactionStatsCollector collector = new DefaultTransactionStatsCollector();
        TransactionMonitoringBeanPostProcessor processor =
                newStandaloneProcessor(collector, new StaticApplicationContext());

        ProxyFactory innerFactory = new ProxyFactory(new PlainGreeterImpl());
        innerFactory.setProxyTargetClass(true);
        innerFactory.addAdvice(new TransactionInterceptor(
                (TransactionManager) new NoOpTransactionManager(), new AnnotationTransactionAttributeSource()));
        innerFactory.setFrozen(true);

        ProxyFactory outerFactory = new ProxyFactory();
        outerFactory.setTarget(innerFactory.getProxy());
        outerFactory.setInterfaces(Greeter.class);
        Object outerProxy = outerFactory.getProxy();

        Greeter result = (Greeter) processor.postProcessAfterInitialization(outerProxy, "greeter");
        result.greet();

        assertThat(result).isSameAs(outerProxy);
        assertThat(hasMonitoringInterceptor(result)).isFalse();
        assertThat(collector.getCopyOfStats()).isEmpty();
    }

    /**
     * The other way round: the outer proxy is frozen, the one holding the transaction interceptor is not. The
     * advisor only ever goes to the latter, so the bean is monitored as usual.
     */
    @Test
    void testBeanIsMonitoredWhenOnlyOuterProxyIsFrozen() {
        TransactionStatsCollector collector = new DefaultTransactionStatsCollector();
        TransactionMonitoringBeanPostProcessor processor =
                newStandaloneProcessor(collector, new StaticApplicationContext());

        ProxyFactory innerFactory = new ProxyFactory(new PlainGreeterImpl());
        innerFactory.addAdvice(new TransactionInterceptor(
                (TransactionManager) new NoOpTransactionManager(), new AnnotationTransactionAttributeSource()));
        Object transactionalProxy = innerFactory.getProxy();

        ProxyFactory outerFactory = new ProxyFactory();
        outerFactory.setTarget(transactionalProxy);
        outerFactory.setInterfaces(Greeter.class);
        outerFactory.setFrozen(true);
        Object frozenOuterProxy = outerFactory.getProxy();

        Greeter result = (Greeter) processor.postProcessAfterInitialization(frozenOuterProxy, "greeter");
        result.greet();

        assertThat(result).isSameAs(frozenOuterProxy);
        assertMonitoringAdvisorIsRightBeforeTransactionAdvisor((Advised) transactionalProxy);
        assertThat(collector.getCopyOfStats().keySet())
                .singleElement()
                .satisfies(key -> assertThat(key.getIdentityClass()).isEqualTo(PlainGreeterImpl.class));
    }

    /**
     * A transaction interceptor paired by hand with a pointcut of its own, the way transactions were configured
     * before {@code @Transactional}: the attribute source calls every method transactional, yet the interceptor
     * is only ever reached by the methods the pointcut lets through.
     */
    @Test
    void testOnlyMethodsMatchedByPointcutOfTransactionAdvisorAreMonitored() {
        TransactionStatsCollector collector = new DefaultTransactionStatsCollector();
        TransactionMonitoringBeanPostProcessor processor =
                newStandaloneProcessor(collector, new StaticApplicationContext());

        NameMatchMethodPointcut saveMethodsOnly = new NameMatchMethodPointcut();
        saveMethodsOnly.setMappedName("save*");
        ProxyFactory proxyFactory = new ProxyFactory(new StorageImpl());
        proxyFactory.addAdvisor(new DefaultPointcutAdvisor(
                saveMethodsOnly,
                new TransactionInterceptor(
                        (TransactionManager) new NoOpTransactionManager(),
                        new MatchAlwaysTransactionAttributeSource())));

        Storage storage = (Storage) processor.postProcessAfterInitialization(proxyFactory.getProxy(), "storage");

        storage.read();
        assertThat(collector.getCopyOfStats()).isEmpty();

        storage.save();
        assertThat(collector.getCopyOfStats().keySet())
                .singleElement()
                .satisfies(key -> assertThat(key.getMethod().getName()).isEqualTo("save"));
    }

    /**
     * The same, with the pointcut written as an AspectJ expression - what {@code <aop:advisor pointcut="...">}
     * of an XML configuration turns into.
     */
    @Test
    void testOnlyMethodsMatchedByAspectJExpressionOfTransactionAdvisorAreMonitored() {
        TransactionStatsCollector collector = new DefaultTransactionStatsCollector();
        TransactionMonitoringBeanPostProcessor processor =
                newStandaloneProcessor(collector, new StaticApplicationContext());

        AspectJExpressionPointcut saveMethodsOnly = new AspectJExpressionPointcut();
        saveMethodsOnly.setExpression("execution(* save*(..))");
        ProxyFactory proxyFactory = new ProxyFactory(new StorageImpl());
        proxyFactory.addAdvisor(new DefaultPointcutAdvisor(
                saveMethodsOnly,
                new TransactionInterceptor(
                        (TransactionManager) new NoOpTransactionManager(),
                        new MatchAlwaysTransactionAttributeSource())));

        Storage storage = (Storage) processor.postProcessAfterInitialization(proxyFactory.getProxy(), "storage");

        storage.read();
        assertThat(collector.getCopyOfStats()).isEmpty();

        storage.save();
        assertThat(collector.getCopyOfStats().keySet())
                .singleElement()
                .satisfies(key -> assertThat(key.getMethod().getName()).isEqualTo("save"));
    }

    /**
     * Such a pointcut decides by the arguments of each call, so there is no telling at startup which methods
     * will reach the transaction interceptor.
     */
    @Test
    void testBeanWhoseTransactionAdvisorDecidesAtRuntimeIsLeftUntouched() {
        TransactionMonitoringBeanPostProcessor processor = newStandaloneProcessor();

        Pointcut decidingByArguments = new DynamicMethodMatcherPointcut() {
            @Override
            public boolean matches(Method method, Class<?> targetClass, Object... args) {
                return true;
            }
        };
        ProxyFactory proxyFactory = new ProxyFactory(new StorageImpl());
        proxyFactory.addAdvisor(new DefaultPointcutAdvisor(
                decidingByArguments,
                new TransactionInterceptor(
                        (TransactionManager) new NoOpTransactionManager(),
                        new MatchAlwaysTransactionAttributeSource())));
        Object proxy = proxyFactory.getProxy();

        Object result = processor.postProcessAfterInitialization(proxy, "storage");

        assertThat(result).isSameAs(proxy);
        assertThat(hasMonitoringInterceptor(result)).isFalse();
    }

    @Test
    void testUnexpectedFailureLeavesBeanUntouchedInsteadOfBreakingStartup() {
        TransactionMonitoringBeanPostProcessor processor = newStandaloneProcessor(new StaticApplicationContext() {
            @Override
            public Class<?> getType(String name) {
                throw new IllegalStateException("boom");
            }
        });

        ProxyFactory proxyFactory = new ProxyFactory(new FinalGreeterImpl());
        proxyFactory.addAdvice(new TransactionInterceptor(
                (TransactionManager) new JdbcTransactionManager(), new AnnotationTransactionAttributeSource()));
        Object transactionalProxy = proxyFactory.getProxy();

        Object result = processor.postProcessAfterInitialization(transactionalProxy, "failingGreeter");

        assertThat(result).isSameAs(transactionalProxy);
        assertThat(hasMonitoringInterceptor(result)).isFalse();
    }

    /**
     * Spring makes the bean transactional, yet none of its methods ever starts a transaction.
     */
    @Test
    void testBeanWithoutMethodsStartingTransactionIsLeftUntouched() {
        assertThat(adviceTypes((Advised) supportsOnlyService)).containsExactly(TransactionInterceptor.class);
    }

    /**
     * The same transactional beans, arriving at the post-processor as each kind of proxy Spring may build
     * for them. The CGLIB proxies of a regular Spring Boot application are covered by the shared context.
     */
    @Nested
    class ProxyKinds {

        @Test
        void shouldLeaveBeansUntouchedWithoutSpringTransactionManagement() {
            runner(MonitoringConfiguration.class).run(context -> {
                for (Placement placement : ALL_PLACEMENTS) {
                    assertThat(context.getBean(placement.beanType())).isNotInstanceOf(Advised.class);
                }
                assertNothingRecorded(context, ALL_PLACEMENTS);
            });
        }

        @Test
        void shouldMonitorSpringCglibProxies() {
            runner(MonitoringConfiguration.class, CglibProxyConfiguration.class).run(context -> {
                for (Placement placement : ALL_PLACEMENTS) {
                    Object bean = context.getBean(placement.lookupType());
                    assertThat(AopUtils.isCglibProxy(bean)).isTrue();
                    assertMonitoringAdvisorIsRightBeforeTransactionAdvisor((Advised) bean);
                }
                assertRecorded(context, ALL_PLACEMENTS);
            });
        }

        @Test
        void shouldMonitorSpringJdkProxies() {
            runner(MonitoringConfiguration.class, JdkProxyConfiguration.class).run(context -> {
                for (Placement placement : INTERFACE_BASED) {
                    Object bean = context.getBean(placement.lookupType());
                    assertThat(AopUtils.isJdkDynamicProxy(bean)).isTrue();
                    assertMonitoringAdvisorIsRightBeforeTransactionAdvisor((Advised) bean);
                }
                // A bean without interfaces gets a CGLIB proxy even in this mode.
                assertRecorded(context, ALL_PLACEMENTS);
            });
        }

        /**
         * Nothing can be added to a frozen proxy, so such beans are not monitored.
         */
        @Test
        void shouldLeaveFrozenSpringProxiesUntouched() {
            runner(MonitoringConfiguration.class, FrozenProxyConfiguration.class)
                    .run(context -> {
                        for (Placement placement : ALL_PLACEMENTS) {
                            Advised bean = (Advised) context.getBean(placement.lookupType());
                            assertThat(bean.isFrozen()).isTrue();
                            assertThat(adviceTypes(bean)).containsExactly(TransactionInterceptor.class);
                        }
                        assertNothingRecorded(context, ALL_PLACEMENTS);
                    });
        }

        /**
         * The decorator is a bean of its own, whose proxy has another transactional bean as its target. That
         * one is monitored as itself, and must not get monitored once more on behalf of the decorator. The
         * annotation is on the implementation only, so Spring does not make the decorator transactional.
         */
        @Test
        void shouldMonitorTransactionalBeanOnlyOnceWhenWrappedByAnotherBean() {
            runner(MonitoringConfiguration.class, CglibProxyConfiguration.class, DecoratorConfiguration.class)
                    .run(context -> {
                        Advised decorated = (Advised) context.getBean(ImplementationOnlyAnnotatedImpl.class);
                        Advised decorator = (Advised) context.getBean("decorator");

                        assertThat(AopProxyUtils.getSingletonTarget(decorator)).isSameAs(decorated);
                        assertThat(adviceTypes(decorator)).doesNotContain(TransactionInterceptor.class);
                        assertThat(adviceTypes(decorated))
                                .containsExactly(TransactionMonitoringInterceptor.class, TransactionInterceptor.class);
                    });
        }

        @Test
        void shouldSkipRawJdkProxies() {
            runner(MonitoringConfiguration.class, RawJdkProxyConfiguration.class)
                    .run(context -> {
                        for (Placement placement : INTERFACE_BASED) {
                            Object bean = context.getBean(placement.lookupType());
                            assertThat(Proxy.isProxyClass(bean.getClass())).isTrue();
                            assertThat(bean).isNotInstanceOf(Advised.class);
                        }
                        assertNothingRecorded(context, INTERFACE_BASED);
                    });
        }

        /**
         * Spring finds the annotation on the interface and makes the raw proxy transactional, yet there is no
         * way to tell which implementation is behind it - so it cannot be attributed and is not monitored.
         */
        @Test
        void shouldSkipRawJdkProxiesWrappedIntoSpringTransactionalProxy() {
            runner(MonitoringConfiguration.class, RawJdkProxyConfiguration.class, JdkProxyConfiguration.class)
                    .run(context -> {
                        Object bean = context.getBean(TransactionalOnInterface.class);
                        assertThat(adviceTypes((Advised) bean)).containsExactly(TransactionInterceptor.class);

                        assertNothingRecorded(context, INTERFACE_BASED);
                    });
        }
    }

    private static boolean hasMonitoringInterceptor(Object bean) {
        for (Object current = bean; current instanceof Advised; current = AopProxyUtils.getSingletonTarget(current)) {
            if (adviceTypes((Advised) current).contains(TransactionMonitoringInterceptor.class)) {
                return true;
            }
        }
        return false;
    }

    private static void assertMonitoringAdvisorIsRightBeforeTransactionAdvisor(Advised proxy) {
        List<Class<?>> adviceTypes = adviceTypes(proxy);

        assertThat(adviceTypes).contains(TransactionMonitoringInterceptor.class);
        assertThat(adviceTypes.get(adviceTypes.indexOf(TransactionMonitoringInterceptor.class) + 1))
                .isEqualTo(TransactionInterceptor.class);
    }

    private static List<Class<?>> adviceTypes(Advised proxy) {
        return Arrays.stream(proxy.getAdvisors())
                .map(Advisor::getAdvice)
                .<Class<?>>map(Object::getClass)
                .toList();
    }

    private TransactionMonitoringBeanPostProcessor newStandaloneProcessor() {
        return newStandaloneProcessor(new StaticApplicationContext());
    }

    private TransactionMonitoringBeanPostProcessor newStandaloneProcessor(ApplicationContext applicationContext) {
        return newStandaloneProcessor(new NoOpTransactionStatsCollector(), applicationContext);
    }

    private TransactionMonitoringBeanPostProcessor newStandaloneProcessor(
            TransactionStatsCollector statsCollector, ApplicationContext applicationContext) {
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
                statsCollector,
                noopProvider,
                new TransactionAccessor(),
                new TransactionAttributesRegistry(),
                applicationContext);
    }

    /**
     * A transactional bean of the shared fixtures: the class it is attributed to, the type it can be looked up
     * by whatever proxy it became, and its transactional method.
     */
    private record Placement(Class<?> beanType, Class<?> lookupType, String methodName) {

        Placement(Class<?> beanType, String methodName) {
            this(beanType, beanType, methodName);
        }
    }

    private static final List<Placement> INTERFACE_BASED = List.of(
            new Placement(TransactionalOnInterfaceImpl.class, TransactionalOnInterface.class, "doWork"),
            new Placement(ChildInterfaceImpl.class, ChildInterface.class, "doWork"),
            new Placement(ClassLevelInterfaceImpl.class, ClassLevelInterface.class, "work"),
            new Placement(TransactionalDefaultMethodOnlyImpl.class, TransactionalDefaultMethodOnly.class, "doWork"),
            new Placement(ImplementationOnlyAnnotatedImpl.class, ImplementationOnlyAnnotated.class, "run"),
            new Placement(BothAnnotatedImpl.class, BothAnnotated.class, "doWork"));

    private static final List<Placement> CLASS_BASED = List.of(
            new Placement(ClassLevelService.class, "first"),
            new Placement(ConcreteFromAbstract.class, "doWork"),
            new Placement(ImplementsTransactionalAbstractMethod.class, "doWork"),
            new Placement(ClassLevelConcrete.class, "inherited"),
            new Placement(ChildOverridesWithoutAnnotation.class, "doWork"),
            new Placement(MetaAnnotatedService.class, "doWork"),
            new Placement(JakartaAnnotatedService.class, "doWork"));

    private static final List<Placement> ALL_PLACEMENTS =
            Stream.concat(INTERFACE_BASED.stream(), CLASS_BASED.stream()).toList();

    private static ApplicationContextRunner runner(Class<?>... configurations) {
        ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(configurations);
        for (Placement placement : ALL_PLACEMENTS) {
            runner = runner.withBean(placement.beanType());
        }
        return runner;
    }

    private static void assertRecorded(AssertableApplicationContext context, List<Placement> placements) {
        TransactionStatsCollector collector = context.getBean(TransactionStatsCollector.class);
        SoftAssertions softly = new SoftAssertions();
        for (Placement placement : placements) {
            collector.clear();
            invoke(context, placement);
            softly.assertThat(collector.getCopyOfStats().keySet())
                    .as(placement.toString())
                    .hasSize(1)
                    .allMatch(key -> key.getIdentityClass() == placement.beanType()
                            && key.getMethod().getName().equals(placement.methodName()));
        }
        softly.assertAll();
    }

    private static void assertNothingRecorded(AssertableApplicationContext context, List<Placement> placements) {
        TransactionStatsCollector collector = context.getBean(TransactionStatsCollector.class);
        for (Placement placement : placements) {
            invoke(context, placement);
        }
        assertThat(collector.getCopyOfStats()).isEmpty();
    }

    private static void invoke(ApplicationContext context, Placement placement) {
        Method method = ReflectionUtils.findMethod(placement.lookupType(), placement.methodName());
        ReflectionUtils.makeAccessible(method);
        ReflectionUtils.invokeMethod(method, context.getBean(placement.lookupType()));
    }

    static class MonitoringConfiguration {

        @Bean
        TransactionStatsCollector transactionStatsCollector() {
            return new DefaultTransactionStatsCollector();
        }

        @Bean
        TransactionAccessor transactionAccessor() {
            return new TransactionAccessor();
        }

        @Bean
        TransactionAttributesRegistry transactionAttributesRegistry() {
            return new TransactionAttributesRegistry();
        }

        @Bean
        PlatformTransactionManager transactionManager() {
            return new NoOpTransactionManager();
        }

        @Bean
        TransactionMonitoringBeanPostProcessor transactionMonitoringBeanPostProcessor(
                TransactionStatsCollector transactionStatsCollector,
                ObjectProvider<AxelixMetricsPublisher> metricsPublisherObjectProvider,
                TransactionAccessor transactionAccessor,
                TransactionAttributesRegistry transactionAttributesRegistry,
                ApplicationContext applicationContext) {
            return new TransactionMonitoringBeanPostProcessor(
                    transactionStatsCollector,
                    metricsPublisherObjectProvider,
                    transactionAccessor,
                    transactionAttributesRegistry,
                    applicationContext);
        }
    }

    @EnableTransactionManagement(proxyTargetClass = true)
    static class CglibProxyConfiguration {}

    @EnableTransactionManagement(proxyTargetClass = false)
    static class JdkProxyConfiguration {}

    /** Makes Spring build its transactional proxies frozen. */
    @EnableTransactionManagement(proxyTargetClass = true)
    static class FrozenProxyConfiguration {

        @Bean
        static BeanFactoryPostProcessor frozenAutoProxyCreator() {
            return beanFactory -> beanFactory
                    .getBeanDefinition(AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME)
                    .getPropertyValues()
                    .add("frozen", true);
        }
    }

    static class DecoratorConfiguration {

        @Bean
        ImplementationOnlyAnnotated decorator(ImplementationOnlyAnnotatedImpl decorated) {
            return (ImplementationOnlyAnnotated) new ProxyFactory(decorated).getProxy();
        }
    }

    static class RawJdkProxyConfiguration {

        @Bean
        static RawJdkProxyingPostProcessor rawJdkProxyingPostProcessor() {
            return new RawJdkProxyingPostProcessor();
        }
    }

    /** Turns the interface-based beans into raw JDK proxies before any other post-processor sees them. */
    static class RawJdkProxyingPostProcessor implements BeanPostProcessor, PriorityOrdered {

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (INTERFACE_BASED.stream().noneMatch(placement -> placement.beanType() == bean.getClass())) {
                return bean;
            }
            InvocationHandler handler = (proxy, method, args) -> method.invoke(bean, args);
            return Proxy.newProxyInstance(
                    bean.getClass().getClassLoader(), bean.getClass().getInterfaces(), handler);
        }
    }

    static class NoOpTransactionManager implements PlatformTransactionManager {

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {}

        @Override
        public void rollback(TransactionStatus status) {}
    }

    interface Storage {
        void save();

        void read();
    }

    static class StorageImpl implements Storage {
        @Override
        public void save() {}

        @Override
        public void read() {}
    }

    interface Greeter {
        String greet();
    }

    static class PlainGreeterImpl implements Greeter {
        @Override
        @Transactional
        public String greet() {
            return "hi";
        }
    }

    static final class FinalGreeterImpl implements Greeter {
        @Override
        @Transactional
        public String greet() {
            return "hi";
        }
    }
}
