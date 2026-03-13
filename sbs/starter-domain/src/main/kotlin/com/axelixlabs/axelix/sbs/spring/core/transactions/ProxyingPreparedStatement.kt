package com.axelixlabs.axelix.sbs.spring.core.transactions

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * A {@link PreparedStatement} wrapper that records execution statistics
 * for executed SQL queries.
 *
 * @author Sergey Cherkasov
 */
class ProxyingPreparedStatement(
    private val sql: String,
    private val delegate: PreparedStatement,
    private val startPreparedStatement: Long,
    private val statsCollector: QueriesStatsCollector
) : PreparedStatement by delegate {

    override fun executeQuery(): ResultSet {
        return recordStats {
            delegate.executeQuery()
        }
    }

    override fun executeUpdate(): Int {
        return recordStats {
            delegate.executeUpdate()
        }
    }

    override fun execute(): Boolean {
        return recordStats {
            delegate.execute()
        }
    }

    override fun executeLargeUpdate(): Long {
        return recordStats {
            delegate.executeLargeUpdate()
        }
    }

    override fun executeBatch(): IntArray {
        return recordStats {
            delegate.executeBatch()
        }
    }

    private fun <T> recordStats(supplier: () -> T): T {
        try {
            return supplier()
        } finally {
            val endTimeNano: Long = System.nanoTime()
            val duration: Long = endTimeNano - startPreparedStatement
            val endTimestamp: Long = startPreparedStatement + duration

            statsCollector.recordQueries(
                TransactionQueryRecord(
                    sql,
                    duration / 1_000_000,
                    startPreparedStatement / 1_000_000,
                    endTimestamp / 1_000_000
                )
            )
        }
    }
}
