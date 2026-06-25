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
import com.axelixlabs.axelix.sbs.spring.core.transactions.hibernate.NPlusOneAnalyzer;
import com.axelixlabs.axelix.sbs.spring.core.transactions.hibernate.NPlusOneHolder;
import com.axelixlabs.axelix.sbs.spring.core.transactions.hibernate.NPlusOneHolder.NPlusOneContext;

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
    private final @Nullable NPlusOneHolder nPlusOneHolder;
    private final @Nullable NPlusOneAnalyzer nPlusOneAnalyzer;

    public TransactionMonitoringInterceptor(
            Map<MethodClassKey, Propagation> propagationCache,
            TransactionStatsCollector statsCollector,
            QueriesRecorder queriesCollector,
            @Nullable AxelixMetricsPublisher metricsPublisher,
            @Nullable NPlusOneHolder nPlusOneHolder,
            @Nullable NPlusOneAnalyzer nPlusOneAnalyzer) {
        this.propagationCache = propagationCache;
        this.statsCollector = statsCollector;
        this.queriesCollector = queriesCollector;
        this.metricsPublisher = metricsPublisher;
        this.nPlusOneHolder = nPlusOneHolder;
        this.nPlusOneAnalyzer = nPlusOneAnalyzer;
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

            queriesCollector.startNewContext();
            // N+1
            startNPlusOneContext();

            // METRICS. Create transaction status
            TransactionStatus transactionStatus = TransactionStatus.SUCCESS;

            try {
                return invocation.proceed();
            } catch (Throwable e) {

                // METRICS. Change transaction status
                transactionStatus = TransactionStatus.ERROR;

                throw e;
            } finally {

                long endTimestampMs = System.currentTimeMillis();

                List<SqlQueryRecord> queries = queriesCollector.popAllRecords();

                // N+1
                endNPlusOneContextAndAnalyzeQueries(queries);

                TransactionRecord transactionRecord = new TransactionRecord(endTimestampMs, startTimestampMs, queries);

                statsCollector.recordTransaction(key, transactionRecord);

                // METRICS. Publish metrics in MeterRegistry
                publishInMeterRegistry(
                        declaringClass, method, transactionRecord.getDurationMs(), transactionStatus, queries.size());
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

    private void publishInMeterRegistry(
            Class<?> declaringClass,
            Method method,
            long durationMs,
            TransactionStatus transactionStatus,
            int queriesSize) {
        if (metricsPublisher == null) {
            return;
        }
        try {
            metricsPublisher.publishTransactionMetrics(
                    declaringClass.getSimpleName(), method.getName(), durationMs, transactionStatus, queriesSize);
        } catch (Exception ignored) {
        }
    }

    private void startNPlusOneContext() {
        if (nPlusOneHolder != null) {
            nPlusOneHolder.startContext();
        }
    }

    private void endNPlusOneContextAndAnalyzeQueries(List<SqlQueryRecord> queries) {
        if (nPlusOneHolder == null || nPlusOneAnalyzer == null) {
            return;
        }
        NPlusOneContext context = nPlusOneHolder.popContext();

        if (context != null) {
            nPlusOneAnalyzer.analyzeAndMarkNPlusOneQueries(context, queries);
        }
    }
}
