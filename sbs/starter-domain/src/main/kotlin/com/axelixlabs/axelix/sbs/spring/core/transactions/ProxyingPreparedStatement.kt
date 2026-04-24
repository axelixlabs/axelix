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
package com.axelixlabs.axelix.sbs.spring.core.transactions

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * A {@link PreparedStatement} wrapper that records execution statistics
 * for executed SQL queries.
 *
 * @author Sergey Cherkasov
 */
class ProxyingPreparedStatement(
    private val sql: String,
    private val delegate: PreparedStatement,
    private val statsCollector: QueriesRecorder
) : PreparedStatement by delegate {

    override fun executeQuery(): ResultSet {
        return execute {
            delegate.executeQuery()
        }
    }

    override fun executeUpdate(): Int {
        return execute {
            delegate.executeUpdate()
        }
    }

    override fun execute(): Boolean {
        return execute {
            delegate.execute()
        }
    }

    override fun executeLargeUpdate(): Long {
        return execute {
            delegate.executeLargeUpdate()
        }
    }

    override fun executeBatch(): IntArray {
        return execute {
            delegate.executeBatch()
        }
    }

    private fun <T> execute(action: () -> T): T {
        val startTimestampMs: Long = System.currentTimeMillis();
        val txStartTime: Long = System.nanoTime()

        try {
            return action()
        } finally {
            val duration: Long = System.nanoTime() - txStartTime

            statsCollector.recordQuery(
                SqlQueryRecord(
                    sql,
                    duration / 1_000_000,
                    startTimestampMs
                )
            )
        }
    }
}
