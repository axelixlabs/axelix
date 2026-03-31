package com.axelixlabs.axelix.sbs.spring.core.transactions

import javax.sql.DataSource

/**
 * A {@link DataSource} wrapper that returns proxy {@link Connection} instances
 * capable of collecting SQL query execution statistics.
 *
 * @author Sergey Cherkasov
 */
class ProxyingDataSource(private val delegate : DataSource, private val statsCollector : QueriesRecorder) : DataSource by delegate {

    override fun getConnection() = ProxyingConnection(delegate.getConnection(), statsCollector)

    override fun getConnection(username: String?, password: String?) = ProxyingConnection(delegate.getConnection(username, password), statsCollector)
}
