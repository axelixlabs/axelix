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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.axelixlabs.axelix.sbs.spring.core.persistence.MethodClassKey;

/**
 * Default implementation {@link TransactionStatsCollector}.
 *
 * <p>Collects and stores transaction execution statistics for monitoring.
 *
 * @since 22.01.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public class DefaultTransactionStatsCollector implements TransactionStatsCollector {

    private final ConcurrentHashMap<MethodClassKey, TransactionStats> statsMap;

    public DefaultTransactionStatsCollector() {
        this.statsMap = new ConcurrentHashMap<>();
    }

    @Override
    public void recordTransaction(MethodClassKey key, TransactionExecutionProfile transactionRecord) {
        statsMap.compute(key, (k, stats) -> {
            if (stats == null) {
                stats = new TransactionStats();
            }
            stats.put(transactionRecord);
            return stats;
        });
    }

    @Override
    public Map<MethodClassKey, TransactionStats> getCopyOfStats() {
        return Collections.unmodifiableMap(statsMap);
    }

    @Override
    public void clear() {
        statsMap.clear();
    }
}
