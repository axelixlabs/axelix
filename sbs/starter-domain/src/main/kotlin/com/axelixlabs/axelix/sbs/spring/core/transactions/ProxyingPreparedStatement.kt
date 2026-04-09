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
