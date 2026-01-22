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

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 *
 * @since 22.01.2026
 * @author Nikita Kirillov
 */
public class TransactionStats {

    private final int maxTransactionsPerMethod;
    private final ConcurrentLinkedDeque<TransactionRecord> recordedTransactions;
    private final AtomicInteger dequeSize;

    public TransactionStats(Integer maxTransactionsPerMethod) {
        this.maxTransactionsPerMethod = maxTransactionsPerMethod;
        this.dequeSize = new AtomicInteger(0);
        this.recordedTransactions = new ConcurrentLinkedDeque<>();
    }

    public void addTransactionRecord(TransactionRecord transactionRecord) {
        recordedTransactions.addLast(transactionRecord);
        dequeSize.incrementAndGet();
    }

    // maxTransactionsPerMethod
    public List<TransactionRecord> getRecordedTransactions() {
        var copy = new LinkedList<>(recordedTransactions);
        clear(copy.size(), copy);
        return copy;
    }

    public void clear() {
        this.clear(dequeSize.get(), this.recordedTransactions);
    }

    public void clear(int dequeSize, Deque<TransactionRecord> recordedTransactions) {
        int toShrink = dequeSize - maxTransactionsPerMethod;

        while (toShrink > 0) {
            recordedTransactions.removeFirst();
            toShrink--;
        }
    }
}
