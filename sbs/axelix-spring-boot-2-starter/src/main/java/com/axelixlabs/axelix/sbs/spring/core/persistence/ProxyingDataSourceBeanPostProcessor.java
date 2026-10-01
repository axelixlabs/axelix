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
import java.sql.Connection;

import javax.sql.DataSource;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;

import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;
import com.axelixlabs.axelix.sbs.spring.core.utils.ProxyingUtils;

/**
 * {@link BeanPostProcessor} that wraps {@link DataSource} beans with a monitoring proxy to collect
 * real-time SQL query execution statistics.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
public class ProxyingDataSourceBeanPostProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(ProxyingDataSourceBeanPostProcessor.class);

    private final TransactionAccessor transactionAccessor;

    public ProxyingDataSourceBeanPostProcessor(TransactionAccessor transactionAccessor) {
        this.transactionAccessor = transactionAccessor;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (!(bean instanceof DataSource)) {
            return bean;
        }
        DataSource dataSource = (DataSource) bean;

        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(
                createGetConnectionPointcut(), new ConnectionProxyingInterceptor(transactionAccessor));

        return ProxyingUtils.tryCreateProxy(dataSource, advisor).orElseGet(() -> {
            log.warn(
                    "Cannot enable SQL monitoring for DataSource bean '{}' of class {}: CGLIB cannot proxy a final "
                            + "class or override a final method. SQL monitoring is skipped for this bean; the bean "
                            + "itself and every other Axelix feature are unaffected.",
                    beanName,
                    ClassUtils.getUserClass(dataSource.getClass()).getName());
            return bean;
        });
    }

    private Pointcut createGetConnectionPointcut() {
        return new StaticMethodMatcherPointcut() {
            @Override
            public boolean matches(@NonNull Method method, @NonNull Class<?> targetClass) {
                return method.getName().equals("getConnection") && method.getReturnType() == Connection.class;
            }
        };
    }

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
            if (result instanceof Connection) {
                return new ProxyingConnection((Connection) result, transactionAccessor);
            }
            return result;
        }
    }
}
