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

/**
 * Record of a single SQL query execution.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
public class SqlQueryRecord {

    private final String sql;
    private final long durationMs;
    private final long startTimestampMs;

    /**
     * @param sql               the executed SQL statement
     * @param durationMs        query execution duration in milliseconds.
     * @param startTimestampMs  unix timestamp (milliseconds from epoch) when the query started.
     */
    public SqlQueryRecord(String sql, long durationMs, long startTimestampMs) {
        this.sql = sql;
        this.durationMs = durationMs;
        this.startTimestampMs = startTimestampMs;
    }

    public String getSql() {
        return sql;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public long getStartTimestampMs() {
        return startTimestampMs;
    }
}
