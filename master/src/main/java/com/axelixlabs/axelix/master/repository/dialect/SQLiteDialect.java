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
package com.axelixlabs.axelix.master.repository.dialect;

import org.jspecify.annotations.NonNull;

import org.springframework.data.jdbc.core.dialect.JdbcArrayColumns;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.relational.core.dialect.AnsiDialect;
import org.springframework.data.relational.core.dialect.LockClause;
import org.springframework.data.relational.core.sql.LockOptions;

/**
 * SQLite-specific dialect for Spring Data JDBC.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public class SQLiteDialect extends AnsiDialect implements JdbcDialect {

    @Override
    public @NonNull JdbcArrayColumns getArraySupport() {
        return JdbcArrayColumns.Unsupported.INSTANCE;
    }

    @Override
    public @NonNull LockClause lock() {

        return new LockClause() {

            @Override
            public @NonNull String getLock(@NonNull LockOptions lockOptions) {
                return "";
            }

            @Override
            public @NonNull Position getClausePosition() {
                return Position.AFTER_ORDER_BY;
            }
        };
    }
}
