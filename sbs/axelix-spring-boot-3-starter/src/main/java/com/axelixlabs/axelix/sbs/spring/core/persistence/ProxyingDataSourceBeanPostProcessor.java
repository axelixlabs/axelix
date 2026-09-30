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
import java.sql.Connection;

import javax.sql.DataSource;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;

import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;

/**
 * {@link BeanPostProcessor} that wraps {@link DataSource} beans with a monitoring proxy to collect
 * real-time SQL query execution statistics.
 *
 * <p>The proxy is a CGLIB subclass of the target's own class (not a hand-written decorator), so it stays
 * assignment-compatible with whatever concrete or intermediate type (e.g. an
 * {@link AbstractRoutingDataSource} subclass) a user's own configuration declares the bean as - a
 * decorator that only implements {@link DataSource} would break such a declaration the moment the same
 * {@code @Bean} method is called again from elsewhere in the same {@code @Configuration} class.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public class ProxyingDataSourceBeanPostProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(ProxyingDataSourceBeanPostProcessor.class);

    private final TransactionAccessor transactionAccessor;

    public ProxyingDataSourceBeanPostProcessor(TransactionAccessor transactionAccessor) {
        this.transactionAccessor = transactionAccessor;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (!(bean instanceof DataSource dataSource)) {
            return bean;
        }

        if (Modifier.isFinal(dataSource.getClass().getModifiers()) || hasFinalProxyableMethod(dataSource.getClass())) {
            log.warn(
                    "Cannot enable SQL monitoring for DataSource bean '{}' of class {}: CGLIB cannot proxy a final "
                            + "class or override a final method. SQL monitoring is skipped for this bean; the bean "
                            + "itself and every other Axelix feature are unaffected.",
                    beanName,
                    dataSource.getClass().getName());
            return bean;
        }

        return createMonitoringProxy(dataSource);
    }

    /**
     * CGLIB can't override a final method, even on a non-final class - calling it on the proxy would run
     * against the proxy's own empty state instead of the real target's.
     */
    private boolean hasFinalProxyableMethod(Class<?> targetClass) {
        MethodFilter finalMethodFilter = method -> !ReflectionUtils.isObjectMethod(method)
                && !Modifier.isPrivate(method.getModifiers())
                && !Modifier.isStatic(method.getModifiers())
                && Modifier.isFinal(method.getModifiers());

        return ReflectionUtils.getUniqueDeclaredMethods(targetClass, finalMethodFilter).length > 0;
    }

    private Object createMonitoringProxy(DataSource dataSource) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(dataSource);
        proxyFactory.setProxyTargetClass(true);

        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(
                createGetConnectionPointcut(), new ConnectionProxyingInterceptor(transactionAccessor));
        proxyFactory.addAdvisor(advisor);

        return proxyFactory.getProxy();
    }

    private Pointcut createGetConnectionPointcut() {
        return new StaticMethodMatcherPointcut() {
            @Override
            public boolean matches(@NonNull Method method, @NonNull Class<?> targetClass) {
                return method.getName().equals("getConnection") && method.getReturnType() == Connection.class;
            }
        };
    }

    /**
     * Wraps the {@link Connection} returned by {@code getConnection()}/{@code getConnection(user, pass)} in a
     * {@link ProxyingConnection}; every other {@link DataSource} method passes through to the real target
     * unmodified.
     */
    private static class ConnectionProxyingInterceptor implements MethodInterceptor {

        private final TransactionAccessor transactionAccessor;

        ConnectionProxyingInterceptor(TransactionAccessor transactionAccessor) {
            this.transactionAccessor = transactionAccessor;
        }

        @Override
        @SuppressWarnings("NullAway") // the pointcut only matches getConnection(), which never legitimately
        // returns null - either a real Connection comes back, or the delegate throws.
        public Object invoke(@NonNull MethodInvocation invocation) throws Throwable {
            Object result = invocation.proceed();
            // Routing/delegating DataSources delegate to a target DataSource that may already be wrapped
            // (e.g. a separately-registered bean) - avoid double-counting the same query.
            if (result instanceof ProxyingConnection) {
                return result;
            }
            if (result instanceof Connection connection) {
                return new ProxyingConnection(connection, transactionAccessor);
            }
            return result;
        }
    }
}
