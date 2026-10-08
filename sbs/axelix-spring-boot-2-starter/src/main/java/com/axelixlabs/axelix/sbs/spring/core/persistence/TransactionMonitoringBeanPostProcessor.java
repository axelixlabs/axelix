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
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;

import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;
import com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringInterceptor.MonitoredMethod;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAttributesRegistry;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionDefinitionAttributes;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;

/**
 * BeanPostProcessor that adds transaction monitoring to the beans Spring itself made transactional,
 * to collect statistics of the transactions they open.
 *
 * <p>Whether a method is transactional, and with which attributes, is not decided here: it is asked from the
 * {@link TransactionAttributeSource} of the transaction interceptor Spring already put on the bean's proxy.
 * A bean without such an interceptor has no declarative transactions and is left untouched.
 *
 * @since 22.01.2026
 * @author Nikita Kirillov
 */
public class TransactionMonitoringBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(TransactionMonitoringBeanPostProcessor.class);

    private static final Set<Propagation> TRANSACTION_CREATING_TYPES =
            EnumSet.of(Propagation.REQUIRED, Propagation.REQUIRES_NEW, Propagation.NESTED);

    private final TransactionStatsCollector statsCollector;
    private final TransactionAccessor transactionAccessor;
    private final TransactionAttributesRegistry transactionAttributesRegistry;
    private final ObjectProvider<AxelixMetricsPublisher> metricsPublisherObjectProvider;
    private final ApplicationContext applicationContext;

    public TransactionMonitoringBeanPostProcessor(
            TransactionStatsCollector statsCollector,
            ObjectProvider<AxelixMetricsPublisher> metricsPublisherObjectProvider,
            TransactionAccessor transactionAccessor,
            TransactionAttributesRegistry transactionAttributesRegistry,
            ApplicationContext applicationContext) {
        this.transactionAccessor = transactionAccessor;
        this.statsCollector = statsCollector;
        this.metricsPublisherObjectProvider = metricsPublisherObjectProvider;
        this.transactionAttributesRegistry = transactionAttributesRegistry;
        this.applicationContext = applicationContext;
    }

    /**
     * Called when a bean of a circular reference has to be handed over to other beans before it is initialized.
     * Spring builds the transactional proxy of such a bean at this point and caches it. Later it passes the raw
     * bean, not the proxy, to {@link #postProcessAfterInitialization}, and only after that puts the cached proxy
     * in its place.
     */
    @Override
    public Object getEarlyBeanReference(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        return postProcessAfterInitialization(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        try {
            return monitorIfTransactional(bean, beanName);
        } catch (Exception e) {
            log.warn("Cannot enable transaction monitoring for bean '{}' because of an unexpected error.", beanName, e);
            return bean;
        }
    }

    /**
     * The bean never gets wrapped into a proxy of our own here: the monitoring advisor is only added to
     * the Spring proxy the bean already is, and only if all of the following holds:
     * <ol>
     *     <li>the bean is a Spring AOP proxy, i.e. it is {@link Advised};</li>
     *     <li>Spring's transaction interceptor is in the advisor chain of that proxy, or of a proxy nested
     *     in it;</li>
     *     <li>the proxy holding the transaction interceptor is not frozen;</li>
     *     <li>the class to attribute the transactions to can be determined (there is no raw JDK dynamic
     *     proxy at the bottom), and the pointcut of the transaction advisor is not evaluated at runtime;</li>
     *     <li>there is at least one method Spring may start a transaction for.</li>
     * </ol>
     * Otherwise the bean is returned as it arrived.
     */
    private Object monitorIfTransactional(Object bean, String beanName) {
        if (!(bean instanceof Advised)) {
            // An opaque Spring proxy does not expose its advisors.
            if (bean instanceof SpringProxy) {
                log.debug(
                        "Transaction monitoring is skipped for bean '{}': it is an opaque Spring proxy, which "
                                + "cannot be inspected for transactions.",
                        beanName);
            }
            return bean;
        }

        // The transaction interceptor may sit on an inner proxy, e.g. a Spring Data repository wrapped by a caching one
        Advised springProxy = (Advised) bean;
        Object current = springProxy;
        while (current instanceof Advised) {
            Advised advised = (Advised) current;
            Advisor[] advisors = advised.getAdvisors();
            for (int i = 0; i < advisors.length; i++) {
                if (advisors[i].getAdvice() instanceof TransactionMonitoringInterceptor) {
                    return bean;
                }
                if (advisors[i].getAdvice() instanceof TransactionAspectSupport) {
                    return addMonitoringAdvisor(
                            springProxy, beanName, advised, i, (TransactionAspectSupport) advisors[i].getAdvice());
                }
            }
            current = AopProxyUtils.getSingletonTarget(advised);
        }
        return bean;
    }

    /**
     * Adds the monitoring advisor to the proxy holding the transaction advisor, right before the latter.
     *
     * @return the bean to expose: the given one - monitored or left as it is - or a new proxy wrapping it.
     */
    private Object addMonitoringAdvisor(
            Advised bean,
            String beanName,
            Advised advised,
            int transactionAdvisorIndex,
            TransactionAspectSupport transactionAspect) {
        TransactionAttributeSource attributeSource = transactionAspect.getTransactionAttributeSource();
        if (attributeSource == null) {
            return bean;
        }

        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(advised);
        Class<?> identityClass = resolveIdentityClass(beanName, targetClass);
        Advisor transactionAdvisor = advised.getAdvisors()[transactionAdvisorIndex];

        if (!canBeMonitoredBean(beanName, advised, transactionAdvisor, identityClass)) {
            return bean;
        }

        Map<Method, MonitoredMethod> monitoredMethods =
                collectMonitoredMethods(advised, transactionAdvisor, attributeSource, targetClass, identityClass);

        if (monitoredMethods.isEmpty()) {
            return bean;
        }

        TransactionMonitoringInterceptor interceptor = new TransactionMonitoringInterceptor(
                monitoredMethods, statsCollector, metricsPublisherObjectProvider.getIfAvailable(), transactionAccessor);

        // Pointcut provides fast filtering at the proxy level and is necessary for performance
        DefaultPointcutAdvisor advisor =
                new DefaultPointcutAdvisor(createTransactionMonitoringPointcut(monitoredMethods), interceptor);

        // Right before the transaction interceptor, so that an advice which short-circuits the call
        // (e.g. a cache hit) does not get recorded as a transaction.
        advised.addAdvisor(transactionAdvisorIndex, advisor);
        return bean;
    }

    private boolean canBeMonitoredBean(
            String beanName, Advised advised, Advisor transactionAdvisor, Class<?> identityClass) {
        if (Proxy.isProxyClass(identityClass)) {
            log.debug(
                    "Cannot enable transaction monitoring for bean '{}': it is transactional, but there is a raw "
                            + "JDK dynamic proxy behind its Spring proxy, so the class its transactions belong to "
                            + "cannot be determined. Transaction monitoring is skipped for this bean.",
                    beanName);
            return false;
        }

        // Whoever froze the proxy asked for its advisor chain to stay as it is.
        if (advised.isFrozen()) {
            log.debug(
                    "Cannot enable transaction monitoring for bean '{}': its transactional proxy is frozen, so "
                            + "no advisor can be added to it. Transaction monitoring is skipped for this bean.",
                    beanName);
            return false;
        }

        // A pointcut deciding by the arguments of each call: which methods reach the transaction interceptor
        // cannot be told ahead of the calls.
        if (transactionAdvisor instanceof PointcutAdvisor
                && ((PointcutAdvisor) transactionAdvisor)
                        .getPointcut()
                        .getMethodMatcher()
                        .isRuntime()) {
            log.debug(
                    "Cannot enable transaction monitoring for bean '{}': the pointcut of its transaction advisor "
                            + "is evaluated at runtime. Transaction monitoring is skipped for this bean.",
                    beanName);
            return false;
        }

        return true;
    }

    private Map<Method, MonitoredMethod> collectMonitoredMethods(
            Advised advised,
            Advisor transactionAdvisor,
            TransactionAttributeSource attributeSource,
            Class<?> targetClass,
            Class<?> identityClass) {
        // The very class the transaction interceptor resolves the attributes against.
        Object target = AopProxyUtils.getSingletonTarget(advised);
        Class<?> attributeTargetClass = target != null ? AopUtils.getTargetClass(target) : advised.getTargetClass();

        Map<Method, MonitoredMethod> monitoredMethods = new HashMap<>();
        for (Method method : findInvocableMethods(advised, targetClass)) {
            if (!isAppliedTo(transactionAdvisor, method, advised.getTargetClass())) {
                continue;
            }
            MonitoredMethod monitoredMethod =
                    resolveMonitoredMethod(method, attributeSource, attributeTargetClass, targetClass, identityClass);
            if (monitoredMethod != null) {
                monitoredMethods.put(method, monitoredMethod);
            }
        }
        return monitoredMethods;
    }

    /**
     * A bean is attributed to the class actually executing - unless it is declared as an interface that class
     * does not implement. That is the case when the implementation is a generic one, shared by many beans
     * (e.g. {@code SimpleJpaRepository} behind every Spring Data repository): such a bean is attributed to
     * the interface it is declared with.
     */
    private Class<?> resolveIdentityClass(String beanName, Class<?> targetClass) {
        Class<?> implementationClass = ClassUtils.getUserClass(targetClass);
        Class<?> declaredType = findDeclaredType(beanName);

        if (declaredType != null && declaredType.isInterface() && !declaredType.isAssignableFrom(implementationClass)) {
            return declaredType;
        }
        return implementationClass;
    }

    private @Nullable Class<?> findDeclaredType(String beanName) {
        try {
            return applicationContext.getType(beanName, false);
        } catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }

    private Set<Method> findInvocableMethods(Advised advised, Class<?> targetClass) {
        MethodFilter proxyableMethodFilter = method -> !ReflectionUtils.isObjectMethod(method)
                && !Modifier.isPrivate(method.getModifiers())
                && !Modifier.isStatic(method.getModifiers());

        Class<?>[] types =
                AopUtils.isJdkDynamicProxy(advised) ? advised.getProxiedInterfaces() : new Class<?>[] {targetClass};

        Set<Method> methods = new LinkedHashSet<>();
        for (Class<?> type : types) {
            methods.addAll(Arrays.asList(ReflectionUtils.getUniqueDeclaredMethods(type, proxyableMethodFilter)));
        }
        return methods;
    }

    /**
     * Tells whether the transaction advisor gets applied to the given method at all - the same way Spring
     * decides it when building the interceptor chain of a call. The pointcut of a hand-made transaction
     * advisor may be narrower than what its {@link TransactionAttributeSource} calls transactional.
     */
    private static boolean isAppliedTo(Advisor transactionAdvisor, Method method, @Nullable Class<?> targetClass) {
        if (!(transactionAdvisor instanceof PointcutAdvisor)) {
            return true;
        }
        Class<?> actualClass = targetClass != null ? targetClass : method.getDeclaringClass();
        Pointcut pointcut = ((PointcutAdvisor) transactionAdvisor).getPointcut();

        return pointcut.getClassFilter().matches(actualClass)
                && pointcut.getMethodMatcher().matches(method, actualClass);
    }

    private @Nullable MonitoredMethod resolveMonitoredMethod(
            Method method,
            TransactionAttributeSource attributeSource,
            @Nullable Class<?> attributeTargetClass,
            Class<?> targetClass,
            Class<?> identityClass) {
        TransactionAttribute attribute = attributeSource.getTransactionAttribute(method, attributeTargetClass);
        if (attribute == null) {
            return null;
        }

        Propagation propagation = propagationOf(attribute);
        if (!canCreateTransaction(propagation)) {
            return null;
        }

        // The method as it is implemented by the target class - even when it was invoked through an interface
        // on a JDK proxy - attributed to the identity class.
        MethodClassKey key = new MethodClassKey(AopUtils.getMostSpecificMethod(method, targetClass), identityClass);
        transactionAttributesRegistry.register(
                key,
                new TransactionDefinitionAttributes(
                        propagation.name(), isolationOf(attribute).name(), attribute.isReadOnly()));
        return new MonitoredMethod(key, propagation);
    }

    private static Propagation propagationOf(TransactionAttribute attribute) {
        for (Propagation propagation : Propagation.values()) {
            if (propagation.value() == attribute.getPropagationBehavior()) {
                return propagation;
            }
        }
        throw new IllegalArgumentException("Unknown propagation behavior: " + attribute.getPropagationBehavior());
    }

    private boolean canCreateTransaction(Propagation propagation) {
        return TRANSACTION_CREATING_TYPES.contains(propagation);
    }

    private static Isolation isolationOf(TransactionAttribute attribute) {
        for (Isolation isolation : Isolation.values()) {
            if (isolation.value() == attribute.getIsolationLevel()) {
                return isolation;
            }
        }
        throw new IllegalArgumentException("Unknown isolation level: " + attribute.getIsolationLevel());
    }

    private Pointcut createTransactionMonitoringPointcut(Map<Method, MonitoredMethod> monitoredMethods) {
        return new StaticMethodMatcherPointcut() {
            @Override
            public boolean matches(@NonNull Method method, @NonNull Class<?> clazz) {
                return monitoredMethods.containsKey(method);
            }
        };
    }
}
