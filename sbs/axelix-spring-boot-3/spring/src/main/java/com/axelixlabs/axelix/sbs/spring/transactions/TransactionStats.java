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
package com.axelixlabs.axelix.sbs.spring.transactions;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Stores transaction execution records for a specific method with size limits.
 * Maintains a rolling window of recent transactions up to the configured maximum.
 *
 * @since 22.01.2026
 * @author Nikita Kirillov
 */
public class TransactionStats {

    private final int maxTransactionsPerMethod;
    private final ConcurrentLinkedDeque<TransactionRecord> recordedTransactions;

    public TransactionStats(Integer maxTransactionsPerMethod) {
        this.maxTransactionsPerMethod = maxTransactionsPerMethod;
        this.recordedTransactions = new ConcurrentLinkedDeque<>();
    }

    public void addTransactionRecord(TransactionRecord transactionRecord) {
        recordedTransactions.addLast(transactionRecord);
    }

    public List<TransactionRecord> getRecordedTransactions() {
        var copy = new LinkedList<>(recordedTransactions);

        if (copy.size() > maxTransactionsPerMethod) {
            return copy.subList(copy.size() - maxTransactionsPerMethod, copy.size());
        }

        return copy;
    }

    public void clear() {
        int currentSize = recordedTransactions.size();

        if (currentSize <= maxTransactionsPerMethod) {
            return;
        }

        // We're not draining the queue till the 'maxTransactionsPerMethod'
        // size to avoid potential infinite loop
        int toRemove = currentSize - maxTransactionsPerMethod;
        for (int i = 0; i < toRemove; i++) {
            if (recordedTransactions.pollFirst() == null) {
                break;
            }
        }
    }
}
