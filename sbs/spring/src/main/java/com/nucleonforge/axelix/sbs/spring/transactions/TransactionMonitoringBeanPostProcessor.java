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
package com.nucleonforge.axelix.sbs.spring.transactions;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Pointcut;
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
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 *
 *
 * @author Nikita Kirillov
 */
public class TransactionMonitoringBeanPostProcessor implements BeanPostProcessor {

    private final Map<MethodClassKey, Propagation> propagationCache;

    private final Map<MethodClassKey, Boolean> canCreateTransactionCache;

    private final TransactionStatsCollector statsCollector;

    public TransactionMonitoringBeanPostProcessor(TransactionStatsCollector statsCollector) {
        this.propagationCache = new ConcurrentHashMap<>();
        this.canCreateTransactionCache = new ConcurrentHashMap<>();
        this.statsCollector = statsCollector;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);

        if (preloadMethodPropagationCacheForClass(targetClass)) {
            return createTransactionalProxy(bean, targetClass);
        } else {
            return bean;
        }
    }

    private boolean preloadMethodPropagationCacheForClass(Class<?> targetClass) {

        boolean canCreateTransaciton = false;

        // TODO: why getMethods here and not getDeclaredMethods?
        for (Method method : targetClass.getMethods()) {
            if (ReflectionUtils.isObjectMethod(method)) {
                continue;
            }

            MethodClassKey key = new MethodClassKey(method, targetClass);
            Propagation propagation = resolveTransactionPropagation(method, targetClass);

            if (propagation != null) {
                propagationCache.put(key, propagation);

                boolean thisMethod = canCreateTransaction(propagation);
                canCreateTransaciton |= thisMethod;
                canCreateTransactionCache.put(key, thisMethod);
            }
        }

        return canCreateTransaciton;
    }

    private Object createTransactionalProxy(Object bean, Class<?> targetClass) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(bean);
        proxyFactory.setProxyTargetClass(true);

        TransactionMonitoringInterceptor interceptor =
                new TransactionMonitoringInterceptor(targetClass, propagationCache, statsCollector);

        // TODO:
        //  For me at least it does not seem to be the case that we need this.
        //  TransactionMonitoringInterceptor should already intercept all method invocations
        //  and TransactionMonitoringInterceptor makes the decision internally whether
        //  the given method needs to be intercepted or not.
        DefaultPointcutAdvisor advisor =
                new DefaultPointcutAdvisor(createTransactionMonitoringPointcut(targetClass), interceptor);

        proxyFactory.addAdvisor(advisor);
        return proxyFactory.getProxy();
    }

    private Pointcut createTransactionMonitoringPointcut(Class<?> targetClass) {
        return new StaticMethodMatcherPointcut() {
            @Override
            public boolean matches(@NonNull Method method, @NonNull Class<?> clazz) {
                if (method.getDeclaringClass() == Object.class) {
                    return false;
                }

                MethodClassKey key = new MethodClassKey(method, targetClass);
                Boolean cachedResult = canCreateTransactionCache.get(key);

                if (cachedResult != null) {
                    return cachedResult;
                }

                Propagation propagation = resolveTransactionPropagation(method, targetClass);

                if (propagation == null) {
                    canCreateTransactionCache.put(key, false);
                    return false;
                }

                boolean result = canCreateTransaction(propagation);
                canCreateTransactionCache.put(key, result);
                propagationCache.put(key, propagation);

                return result;
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
