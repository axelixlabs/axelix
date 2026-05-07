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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ConnectionBuilder;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.ShardingKeyBuilder;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * A {@link DataSource} wrapper that returns proxy {@link Connection} instances
 * capable of collecting SQL query execution statistics.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
public class ProxyingDataSource implements DataSource {

    private final DataSource delegate;
    private final QueriesRecorder queriesRecorder;

    public ProxyingDataSource(DataSource delegate, QueriesRecorder queriesRecorder) {
        this.delegate = delegate;
        this.queriesRecorder = queriesRecorder;
    }

    @Override
    public ConnectionBuilder createConnectionBuilder() throws SQLException {
        return delegate.createConnectionBuilder();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new ProxyingConnection(delegate.getConnection(), queriesRecorder);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new ProxyingConnection(delegate.getConnection(username, password), queriesRecorder);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    @Override
    public ShardingKeyBuilder createShardingKeyBuilder() throws SQLException {
        return delegate.createShardingKeyBuilder();
    }
}
