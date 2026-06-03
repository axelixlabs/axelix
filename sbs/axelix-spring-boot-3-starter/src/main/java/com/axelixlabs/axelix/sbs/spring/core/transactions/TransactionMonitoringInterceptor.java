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
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricsPublisher;

import static com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionStatus.error;
import static com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionStatus.success;

/**
 * {@link MethodInterceptor} that monitors transaction execution and collects performance statistics.
 *
 * <p>This interceptor tracks execution of @Transactional methods and records metrics
 * when new transactions are created.
 *
 * @since 22.01.2026
 * @author Nikita Kirillov
 */
public class TransactionMonitoringInterceptor implements MethodInterceptor {

    private final Map<MethodClassKey, Propagation> propagationCache;
    private final TransactionStatsCollector statsCollector;
    private final QueriesRecorder queriesCollector;
    private final @Nullable AxelixMetricsPublisher metricsPublisher;

    public TransactionMonitoringInterceptor(
            Map<MethodClassKey, Propagation> propagationCache,
            TransactionStatsCollector statsCollector,
            QueriesRecorder queriesCollector,
            @Nullable AxelixMetricsPublisher metricsPublisher) {
        this.propagationCache = propagationCache;
        this.statsCollector = statsCollector;
        this.queriesCollector = queriesCollector;
        this.metricsPublisher = metricsPublisher;
    }

    @Override
    @Nullable
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Class<?> declaringClass = method.getDeclaringClass();

        MethodClassKey key = new MethodClassKey(method, declaringClass);
        Propagation propagation = propagationCache.get(key);

        if (propagation != null && shouldCreateNewTransaction(propagation)) {
            long startTimestampMs = System.currentTimeMillis();
            long txStartTime = System.nanoTime();

            queriesCollector.clearAll();

            // METRICS. Create transaction status
            String status = success.name();

            try {
                return invocation.proceed();
            } catch (Throwable e) {

                // METRICS. Change transaction status
                status = error.name();

                throw e;
            } finally {

                long durationNano = System.nanoTime() - txStartTime;
                List<SqlQueryRecord> queries = queriesCollector.popAllRecords();

                statsCollector.recordTransaction(
                        key, new TransactionRecord(durationNano / 1_000_000, startTimestampMs, queries));

                // METRICS. Publish metrics in MeterRegistry
                if (metricsPublisher != null) {
                    metricsPublisher.publishTransactionMetrics(
                            declaringClass.getSimpleName(), method.getName(), durationNano, status, queries.size());
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
