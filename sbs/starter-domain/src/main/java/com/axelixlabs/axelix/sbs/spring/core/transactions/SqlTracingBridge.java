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

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A thread-local bridge that connects the core JDBC proxy module with the Spring-specific
 * tracing infrastructure.
 * <p>
 * This class uses a {@link ThreadLocal} storage mechanism to register and propagate
 * a {@link SqlTracingInterceptor} callback from the transaction layer down to the pure JDBC layer,
 * enabling query-level execution tracing without cross-module dependency coupling.
 * Supports nested transactions by maintaining an interceptor execution hierarchy.
 *
 * @author Nikita Kirillov
 */
public final class SqlTracingBridge {

    private SqlTracingBridge() {}

    /**
     * Functional interface triggered at the absolute start of a SQL query execution.
     * Responsible for creating and starting a child tracing span for the given SQL statement.
     */
    @FunctionalInterface
    public interface SqlTracingInterceptor {
        SqlTracingCallback startSpan(String sql);
    }

    /**
     * Lifecycle handle triggered at the end of a SQL query execution.
     * Implements {@link AutoCloseable} to provide a seamless mechanism for closing
     * and submitting the database query span to the tracing backend.
     */
    @FunctionalInterface
    public interface SqlTracingCallback extends AutoCloseable {
        @Override
        void close();
    }

    private static final ThreadLocal<Deque<SqlTracingInterceptor>> INTERCEPTOR_STACK_HOLDER =
            ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * Pushes the given SQL tracing interceptor onto the thread-local context stack.
     * Used when entering a new transaction scope (including nested ones).
     *
     * @param sqlTracingInterceptor the interceptor logic to register
     */
    public static void setInterceptor(SqlTracingInterceptor sqlTracingInterceptor) {
        INTERCEPTOR_STACK_HOLDER.get().push(sqlTracingInterceptor);
    }

    /**
     * Retrieves the active SQL tracing interceptor currently on top of the thread context stack.
     *
     * @return the active thread-local {@link SqlTracingInterceptor}, or {@code null} if stack is empty
     */
    public static SqlTracingInterceptor getInterceptor() {
        return INTERCEPTOR_STACK_HOLDER.get().peek();
    }

    /**
     * Pops the current SQL tracing interceptor from the thread context stack upon transaction completion.
     * If the stack becomes completely empty, evicts the thread-local allocation entirely to prevent pool leaks.
     */
    public static void clear() {
        Deque<SqlTracingInterceptor> stack = INTERCEPTOR_STACK_HOLDER.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }

        if (stack.isEmpty()) {
            INTERCEPTOR_STACK_HOLDER.remove();
        }
    }
}
