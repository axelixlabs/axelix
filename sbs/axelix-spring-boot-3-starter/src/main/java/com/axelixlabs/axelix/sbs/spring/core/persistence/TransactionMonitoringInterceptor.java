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
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionExecutionProfile;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;

/**
 * {@link MethodInterceptor} that monitors transaction execution and collects performance statistics.
 *
 * <p>This interceptor tracks execution of transactional methods and records metrics
 * when new transactions are created.
 *
 * @since 22.01.2026
 * @author Nikita Kirillov
 */
public class TransactionMonitoringInterceptor implements MethodInterceptor {

    record MonitoredMethod(MethodClassKey key, Propagation propagation) {}

    /**
     * All the methods monitored by this interceptor,
     * i.e. the transactional methods of the single bean it is attached to.
     */
    private final Map<Method, MonitoredMethod> monitoredMethods;

    private final TransactionStatsCollector statsCollector;
    private final @Nullable AxelixMetricsPublisher metricsPublisher;
    private final TransactionAccessor transactionAccessor;

    public TransactionMonitoringInterceptor(
            Map<Method, MonitoredMethod> monitoredMethods,
            TransactionStatsCollector statsCollector,
            @Nullable AxelixMetricsPublisher metricsPublisher,
            TransactionAccessor transactionAccessor) {
        this.monitoredMethods = monitoredMethods;
        this.statsCollector = statsCollector;
        this.metricsPublisher = metricsPublisher;
        this.transactionAccessor = transactionAccessor;
    }

    @Override
    @Nullable
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        MonitoredMethod monitoredMethod = monitoredMethods.get(method);

        if (monitoredMethod != null && shouldCreateNewTransaction(monitoredMethod.propagation())) {
            MethodClassKey key = monitoredMethod.key();

            transactionAccessor.recordNewTransactionStarted();

            try {
                return invocation.proceed();
            } finally {

                TransactionExecutionProfile transactionExecutionProfile =
                        transactionAccessor.recordTransactionCompletion();

                statsCollector.recordTransaction(key, transactionExecutionProfile);

                if (metricsPublisher != null) {
                    metricsPublisher.publishTransactionMetrics(
                            key.getIdentityClass().getSimpleName(), method.getName(), transactionExecutionProfile);
                }
            }
        }

        return invocation.proceed();
    }

    private boolean shouldCreateNewTransaction(Propagation propagation) {
        boolean hasActiveTransaction = TransactionSynchronizationManager.isActualTransactionActive();

        return switch (propagation) {
            case REQUIRES_NEW -> true;

            case REQUIRED, NESTED -> !hasActiveTransaction;

            case SUPPORTS, MANDATORY, NOT_SUPPORTED, NEVER -> false;
        };
    }
}
