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

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import static com.axelixlabs.axelix.sbs.spring.core.tracing.AxelixTraceNames.TAG_SQL_QUERY;
import static com.axelixlabs.axelix.sbs.spring.core.tracing.AxelixTraceNames.TRANSACTION_CHILD_SPAN_NAME;
import static com.axelixlabs.axelix.sbs.spring.core.tracing.AxelixTraceNames.TRANSACTION_SPAN_NAME;

/**
 * Default implementation of {@link AxelixTransactionTracer}.
 *
 * @see SqlTracingBridge
 * @author Nikita Kirillov
 */
public class DefaultAxelixTransactionTracer implements AxelixTransactionTracer {

    private final Tracer tracer;

    public DefaultAxelixTransactionTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public TransactionTraceContext startTransaction(String className, String methodName) {
        Span txSpan = tracer.nextSpan().name(TRANSACTION_SPAN_NAME);
        Tracer.SpanInScope scope = tracer.withSpan(txSpan.start());

        txSpan.tag("class", className);
        txSpan.tag("method", methodName);

        // Register a thread-local callback to intercept subsequent SQL queries
        SqlTracingBridge.setInterceptor(sqlText -> {
            // Create a child span explicitly bound to the current transaction
            Span childSpan = tracer.nextSpan(txSpan)
                    .name(TRANSACTION_CHILD_SPAN_NAME)
                    .tag(TAG_SQL_QUERY, sqlText)
                    .start();

            // Return a callback instance to close the span when query execution finishes
            return childSpan::end;
        });

        return new TransactionTraceContext(txSpan, scope);
    }

    @Override
    public void logException(TransactionTraceContext context, Throwable e) {
        context.txSpan().tag("error.type", e.getClass().getName());
        context.txSpan().error(e);
    }

    @Override
    public void completeTransaction(TransactionTraceContext context) {
        context.scope().close();
        context.txSpan().end();
        // Evict interceptor from thread-local allocation
        SqlTracingBridge.clear();
    }
}
