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

import java.util.Map;

import com.axelixlabs.axelix.sbs.spring.core.SlidingWindow;
import com.axelixlabs.axelix.sbs.spring.core.persistence.MethodClassKey;

/**
 * This interface defines the contract for collecting and retrieving transaction monitoring data
 * from @Transactional method executions.
 *
 * @author Nikita Kirillov
 */
public interface TransactionStatsCollector {

    /**
     * Records a transaction execution for statistical tracking.
     *
     * @param key the method and class identifier
     * @param transactionExecutionProfile the transaction execution record
     */
    void recordTransaction(MethodClassKey key, TransactionExecutionProfile transactionExecutionProfile);

    /**
     * Returns the copy of all collected transaction statistics.
     *
     * @return map of method keys to their transaction statistics
     */
    Map<MethodClassKey, SlidingWindow<TransactionExecutionProfile>> getAllStats();

    /**
     * Clears the stats
     */
    void clearStats();
}
