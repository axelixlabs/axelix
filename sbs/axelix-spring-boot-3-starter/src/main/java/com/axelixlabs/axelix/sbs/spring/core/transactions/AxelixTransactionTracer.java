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

/**
 * Defines Contract for managing the distributed tracing lifecycle of Axelix transactions.
 *
 * @author Nikita Kirillov
 */
public interface AxelixTransactionTracer {

    /**
     * Starts a new parent transaction span, activates its context scope, and registers
     * a thread-local SQL interceptor to trace subsequent database queries as child spans.
     *
     * @param className  the simple name of the target class executing the transaction
     * @param methodName the name of the target method executing the transaction
     * @return a {@link TransactionTraceContext} containing the started span and its active scope
     */
    TransactionTraceContext startTransaction(String className, String methodName);

    /**
     * Attaches exception details and failure metadata to the active transaction span.
     *
     * @param context the current transaction tracing context
     * @param e       the exception thrown during transaction execution
     */
    void logException(TransactionTraceContext context, Throwable e);

    /**
     * Cleans up thread-local storage, restores the prior thread context by closing the scope,
     * and finalizes the root transaction span for submission to the tracing system.
     *
     * @param context the transaction tracing context to complete
     */
    void completeTransaction(TransactionTraceContext context);
}
