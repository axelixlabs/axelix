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

import java.lang.reflect.Method;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 *
 *
 * @author Nikita Kirillov
 */
public class TransactionMonitoringInterceptor implements MethodInterceptor {

    private final Class<?> targetClass;
    private final Map<MethodClassKey, Propagation> propagationCache;
    private final TransactionStatsCollector statsCollector;

    public TransactionMonitoringInterceptor(
            Class<?> targetClass,
            Map<MethodClassKey, Propagation> propagationCache,
            TransactionStatsCollector statsCollector) {
        this.targetClass = targetClass;
        this.propagationCache = propagationCache;
        this.statsCollector = statsCollector;
    }

    @Override
    @Nullable
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();

        MethodClassKey key = new MethodClassKey(method, targetClass);
        Propagation propagation = propagationCache.get(key);

        if (!shouldMonitorTransaction(propagation)) {
            return invocation.proceed();
        }

        long startTime = System.currentTimeMillis();

        try {
            return invocation.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            statsCollector.recordTransaction(key, new TransactionRecord(duration, startTime));
        }
    }

    private boolean shouldMonitorTransaction(@Nullable Propagation propagation) {
        if (propagation == null) {
            return false;
        }
        return shouldUseTransaction(propagation) && shouldUseNewTransaction(propagation);
    }

    private boolean shouldUseTransaction(Propagation propagation) {
        boolean hasActiveTransaction = hasActiveTransaction();
        return switch (propagation) {
            case REQUIRES_NEW, NESTED, REQUIRED -> true;

            case MANDATORY, SUPPORTS -> hasActiveTransaction;

            case NOT_SUPPORTED, NEVER -> false;
        };
    }

    private boolean shouldUseNewTransaction(Propagation propagation) {
        boolean hasActiveTransaction = hasActiveTransaction();

        return switch (propagation) {
            case REQUIRES_NEW -> true;

            case REQUIRED, NESTED -> !hasActiveTransaction;

            case SUPPORTS, MANDATORY, NOT_SUPPORTED, NEVER -> false;
        };
    }

    private boolean hasActiveTransaction() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }
}
