package com.axelixlabs.axelix.sbs.spring.core.transactions

/**
 * Record of a single query execution for monitoring purposes.
 *
 * @param sql               the executed SQL statement
 * @param durationMs        query execution duration in milliseconds.
 * @param startTimestampMs  unix timestamp (milliseconds from epoch) when the query started.
 * @param endTimestampMs    unix timestamp (milliseconds since epoch) when the query finished.
 *
 * @author Sergey Cherkasov
 */
data class TransactionQueryRecord(
    val sql: String,
    val durationMs: Long,
    val startTimestampMs: Long,
    val endTimestampMs: Long
)