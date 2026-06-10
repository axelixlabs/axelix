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
package com.axelixlabs.axelix.sbs.spring.core.tracing;

/**
 * Utility class containing standardized tracing names and span attributes for the Axelix starter.
 *
 * @author Nikita Kirillov
 */
public class AxelixTraceNames {

    public static final String TRANSACTION_SPAN_NAME = "axelix-transaction";

    public static final String TRANSACTION_CHILD_SPAN_NAME = "DB Query";

    public static final String TAG_SQL_QUERY = "db.statement";

    private AxelixTraceNames() {}
}
