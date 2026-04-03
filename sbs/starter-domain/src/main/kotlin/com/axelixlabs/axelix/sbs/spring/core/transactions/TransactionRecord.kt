package com.axelixlabs.axelix.sbs.spring.core.transactions

/**
 * Record of a single transaction execution for monitoring purposes.
 *
 * @param durationMs     transaction execution duration in milliseconds
 * @param startTimestamp transaction start timestamp in milliseconds since epoch
 * @param queries        the list of queries executed during a particular transaction.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
data class TransactionRecord(
    val durationMs: Long,
    val startTimestamp: Long,
    val queries: List<SqlQueryRecord>
)
