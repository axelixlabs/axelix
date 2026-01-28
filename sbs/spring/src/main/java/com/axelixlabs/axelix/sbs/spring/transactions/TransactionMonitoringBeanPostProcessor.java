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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.RepeatableContainers;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;

/**
 * BeanPostProcessor that creates AOP proxies for beans with @Transactional methods
 * to enable real-time transaction monitoring and statistics collection.
 *
 * <p>This processor scans Spring beans during initialization, identifies methods
 * annotated with @Transactional, and wraps eligible beans with monitoring proxies.
 *
 * @since 22.01.2026
 * @author Nikita Kirillov
 */
public class TransactionMonitoringBeanPostProcessor implements BeanPostProcessor {

    private final Map<MethodClassKey, Propagation> propagationCache;

    private final TransactionStatsCollector statsCollector;

    public TransactionMonitoringBeanPostProcessor(TransactionStatsCollector statsCollector) {
        this.propagationCache = new ConcurrentHashMap<>();
        this.statsCollector = statsCollector;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        boolean hasTransactionalMethods = false;
        Set<Class<?>> classesToCheck = new LinkedHashSet<>();
        Class<?> targetClass = AopUtils.getTargetClass(bean);

        classesToCheck.add(targetClass);

        // Process Spring Data repository interfaces: @Transactional annotations on repository interface methods
        // (e.g., default methods in JPA repositories).
        if (AopUtils.isAopProxy(bean)) {
            try {
                Class<?>[] classes = AopProxyUtils.proxiedUserInterfaces(bean);
                classesToCheck.addAll(Arrays.stream(classes).toList());
            } catch (IllegalArgumentException ignored) {
            }
        }

        for (Class<?> clazz : classesToCheck) {
            hasTransactionalMethods |= preloadMethodPropagationCacheForClass(clazz);
        }

        if (hasTransactionalMethods) {
            return createTransactionalProxy(bean);
        } else {
            return bean;
        }
    }

    /**
     * Processes all public/protected/package-private methods (including inherited and interface methods).
     * Supported since Spring 6.0+
     *
     * @see <a href="https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html#transaction-declarative-annotations-method-visibility">
     *      Spring Framework Documentation - Transaction Method Visibility</a>
     */
    private boolean preloadMethodPropagationCacheForClass(Class<?> targetClass) {
        boolean canCreateTransaction = false;

        MethodFilter proxyableMethodFilter = method -> !ReflectionUtils.isObjectMethod(method)
                && !Modifier.isPrivate(method.getModifiers())
                && !Modifier.isStatic(method.getModifiers());

        Method[] uniqueMethods = ReflectionUtils.getUniqueDeclaredMethods(targetClass, proxyableMethodFilter);

        for (Method method : uniqueMethods) {
            canCreateTransaction |= processMethod(method, targetClass);
        }

        return canCreateTransaction;
    }

    private boolean processMethod(Method method, Class<?> targetClass) {
        MethodClassKey key = new MethodClassKey(method, targetClass);
        Propagation propagation = resolveTransactionPropagation(method, targetClass);

        if (propagation != null) {
            propagationCache.put(key, propagation);
            return canCreateTransaction(propagation);
        }

        return false;
    }

    private Object createTransactionalProxy(Object bean) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(bean);
        proxyFactory.setProxyTargetClass(true);

        TransactionMonitoringInterceptor interceptor =
                new TransactionMonitoringInterceptor(propagationCache, statsCollector);

        // Pointcut provides fast filtering at the proxy level and is necessary for performance
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(createTransactionMonitoringPointcut(), interceptor);

        proxyFactory.addAdvisor(advisor);
        return proxyFactory.getProxy();
    }

    private Pointcut createTransactionMonitoringPointcut() {
        return new StaticMethodMatcherPointcut() {
            @Override
            public boolean matches(@NonNull Method method, @NonNull Class<?> clazz) {
                MethodClassKey key = new MethodClassKey(method, method.getDeclaringClass());
                Propagation propagation = propagationCache.get(key);

                if (propagation != null) {
                    return canCreateTransaction(propagation);
                }

                return false;
            }
        };
    }

    @Nullable
    private Propagation resolveTransactionPropagation(Method method, Class<?> clazz) {
        Propagation methodPropagation = findPropagation(method);
        if (methodPropagation != null) {
            return methodPropagation;
        }

        return findPropagation(clazz);
    }

    @Nullable
    private Propagation findPropagation(AnnotatedElement element) {
        MergedAnnotation<Transactional> txAnnotation = MergedAnnotations.from(
                        element, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY, RepeatableContainers.none())
                .get(Transactional.class);

        return txAnnotation.isPresent() ? txAnnotation.getEnum("propagation", Propagation.class) : null;
    }

    private boolean canCreateTransaction(Propagation propagation) {
        return switch (propagation) {
            case REQUIRED, REQUIRES_NEW, NESTED -> true;
            case SUPPORTS, MANDATORY, NOT_SUPPORTED, NEVER -> false;
        };
    }
}
