package com.axelixlabs.axelix.sbs.spring.core.transactions

/**
 * Record of a single query execution for monitoring purposes.
 *
 * @param sql               the executed SQL statement
 * @param durationMs        query execution duration in milliseconds.
 * @param startTimestampMs  unix timestamp (milliseconds from epoch) when the query started.
 *
 * @author Sergey Cherkasov
 */
data class TransactionQueryRecord(
    val sql: String,
    val durationMs: Long,
    val startTimestampMs: Long
)