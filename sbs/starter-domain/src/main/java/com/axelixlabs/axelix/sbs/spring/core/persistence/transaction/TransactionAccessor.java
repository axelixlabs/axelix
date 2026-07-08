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
package com.axelixlabs.axelix.sbs.spring.core.persistence.transaction;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.utils.Assert;
import com.axelixlabs.axelix.sbs.spring.core.persistence.SimpleSqlQueryRecord;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.LazyLoadingTarget;

/**
 * Thread-safe accessor to the currently active Spring's transaction. This is the entrypoint
 * for the entire transaction lifecycle: initiation, recording of stats and termination.
 * <p>
 * The fundamental assumption that this class makes is that each thread has exactly one transaction
 * that is active at a time. It may have multiple transactions bounded in case of REQUIRES_NEW propagation,
 * we have the {@link #TRANSACTIONS_STACK Stack} for this, but generally, this Stack will have the size of 1.
 *
 * @see TransactionExecutionProfile
 * @author Mikhail Polivakha
 */
public class TransactionAccessor {

    private static final ThreadLocal<Deque<TransactionExecutionProfile>> TRANSACTIONS_STACK =
            ThreadLocal.withInitial(() -> new ArrayDeque<>(1));

    public void recordNewTransactionStarted() {
        TRANSACTIONS_STACK.get().add(new TransactionExecutionProfile(Instant.now()));
    }

    /**
     * Record the given sql query to the current transaction.
     * <p>
     * There may be the case that there is no explicitly opened Spring's transaction for
     * this {@link Thread} (e.g. in case of OSIV being used). If there is no active transaction
     * for the current {@link Thread}, the sql query is just ignored, as it does not pertain
     * to the current transaction.
     */
    public void recordSqlQuery(SimpleSqlQueryRecord sqlQueryRecord) {
        TransactionExecutionProfile currentTransactionProfile = getCurrentTransactionProfile();

        if (currentTransactionProfile != null) {
            currentTransactionProfile.recordQuery(sqlQueryRecord);
        }
    }

    public void clearAll() {
        TRANSACTIONS_STACK.remove();
    }

    /**
     * Record the lazy loading that has happened inside the transaction.
     * <p>
     * There may be the case that there is no explicitly opened Spring's transaction for
     * this {@link Thread} (e.g. in case of OSIV being used). If there is no active transaction
     * for the current {@link Thread}, the lazy loading event is just ignored, as it does not pertain
     * to the current transaction.
     *
     * @implNote This call will mark the latest observed query as the one that has performed the lazy loading.
     *           This is reasonable, since the queries recording and hibernate events listening happens in different
     *           parts of the Axelix Starter, but we can generally be sure that the lazy loading event will be fired
     *           right after the query that did lazy loading, and right before the next.
     *
     * @throws IllegalStateException in case there were no queries in transaction yet.
     * @param lazyLoadingTarget the handle that describes the lazy loading that has happened.
     */
    public void recordLazyLoading(LazyLoadingTarget lazyLoadingTarget) throws IllegalStateException {
        TransactionExecutionProfile currentTransactionProfile = getCurrentTransactionProfile();

        if (currentTransactionProfile != null) {
            currentTransactionProfile.recordLazyLoading(lazyLoadingTarget);
        }
    }

    public TransactionExecutionProfile recordTransactionCompletion() {
        Deque<TransactionExecutionProfile> transactionExecutionProfiles = TRANSACTIONS_STACK.get();

        Assert.state(
                () -> !transactionExecutionProfiles.isEmpty(),
                "Cannot complete the transaction - no active Spring Transaction detected");

        TransactionExecutionProfile transactionExecutionProfile =
                transactionExecutionProfiles.removeLast().complete();

        if (transactionExecutionProfiles.isEmpty()) {
            TRANSACTIONS_STACK.remove();
        }

        return transactionExecutionProfile;
    }

    /**
     * @return returns the currently active transaction if there is one. If there is no active
     * transaction, then returns {@code null}.
     */
    @Nullable
    private static TransactionExecutionProfile getCurrentTransactionProfile() {
        Deque<TransactionExecutionProfile> transactionExecutionProfiles = TRANSACTIONS_STACK.get();

        return transactionExecutionProfiles.peekLast();
    }
}
