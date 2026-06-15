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

import java.util.Comparator;
import java.util.List;

import org.jspecify.annotations.NullMarked;

/**
 * Utility class for transactional operations interception.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@NullMarked
public class TransactionUtils {

    /**
     * <p>Why do we need this method:</p>
     *
     * In case the transaction is very short-lived (e.g. it is executed within milliseconds), then this might be possible, that
     * any particular query executed in the transaction, i.e. last {@link SqlQueryRecord}, will have the timestamp that is slightly
     * ahead of the transaction end time as we calculate it.
     * <p>
     * Why? Because of the rounding errors and non-deterministic granularity of {@link System#currentTimeMillis()}.
     * So we have to assume, that if for some reason any sql query got larger end timestamp than that of a transaction -
     * we just take this end timestamp of this query as the transaction end timestamp and that is it.
     */
    public static long calculateTransactionEndTimestamp(long transactionEndTimestamp, List<SqlQueryRecord> queries) {
        if (queries.isEmpty()) {
            return transactionEndTimestamp;
        }

        return Math.max(transactionEndTimestamp, maxQueriesTimestamp(queries));
    }

    @SuppressWarnings("NullAway")
    private static Long maxQueriesTimestamp(List<SqlQueryRecord> queries) {
        return queries.stream()
                .max(Comparator.comparing(SqlQueryRecord::getEndTimestampMs))
                .map(SqlQueryRecord::getEndTimestampMs)
                .orElse(null);
    }
}
