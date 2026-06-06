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
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Service providing access to queries in transaction monitoring data and statistics.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public final class DefaultQueriesRecorder implements QueriesRecorder {

    private static final ThreadLocal<QueriesStack> threadLocalStack = new ThreadLocal<>();

    @Override
    public void startNewContext() {
        QueriesStack stack = threadLocalStack.get();

        if (stack == null) {
            stack = new QueriesStack();
            threadLocalStack.set(stack);
        }

        stack.push(new ArrayList<>());
    }

    @Override
    public void recordQuery(SqlQueryRecord query) {
        QueriesStack stack = threadLocalStack.get();

        if (stack == null) {
            return;
        }

        List<SqlQueryRecord> currentTxQueries = stack.peek();

        if (currentTxQueries != null) {
            currentTxQueries.add(query);
        }
    }

    @Override
    public List<SqlQueryRecord> popAllRecords() {
        QueriesStack stack = threadLocalStack.get();

        if (stack == null || stack.isEmpty()) {
            return new ArrayList<>();
        }

        List<SqlQueryRecord> queries = stack.pop();

        if (stack.isEmpty()) {
            threadLocalStack.remove();
        }

        return queries;
    }

    /**
     * A convenient type-alias for the stack-like data structure of sql queries, executed within the given transactions.
     * <p>
     * We need a stack to account for REQUIRES_NEW transaction isolation level.
     *
     * @author Mikhail Polivakha
     * @author Nikita Kirillov
     */
    static class QueriesStack extends ArrayDeque<List<SqlQueryRecord>> {

        public QueriesStack() {
            super(1); // assume no REQUIRES_NEW transactions by default
        }
    }
}
