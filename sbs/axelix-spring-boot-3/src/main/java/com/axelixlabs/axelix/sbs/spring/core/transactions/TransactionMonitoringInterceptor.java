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
    private final QueriesStatsCollector queriesStatsCollector;

    public TransactionMonitoringInterceptor(
            Map<MethodClassKey, Propagation> propagationCache,
            TransactionStatsCollector statsCollector,
            QueriesStatsCollector queriesStatsCollector) {
        this.propagationCache = propagationCache;
        this.statsCollector = statsCollector;
        this.queriesStatsCollector = queriesStatsCollector;
    }

    @Override
    @Nullable
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Class<?> declaringClass = method.getDeclaringClass();

        MethodClassKey key = new MethodClassKey(method, declaringClass);
        Propagation propagation = propagationCache.get(key);

        if (propagation != null && shouldCreateNewTransaction(propagation)) {
            long startTime = System.nanoTime();

            queriesStatsCollector.clearAllStats();

            try {
                return invocation.proceed();
            } finally {
                long duration = System.nanoTime() - startTime;
                List<TransactionQueryRecord> queries = queriesStatsCollector.getAllStats();

                statsCollector.recordTransaction(
                        key, new TransactionRecord(duration / 1_000_000, startTime / 1_000_000, queries));
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
