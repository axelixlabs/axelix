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
package com.nucleonforge.axelix.sbs.postgres;

import org.postgresql.ds.common.BaseDataSource;

import com.nucleonforge.axelix.sbs.metrics.AbstractMetricsProvider;
import com.nucleonforge.axelix.sbs.metrics.Metrics;

/**
 * {@code PgDataSourceStatementCacheSizeInQueriesReporter} is a metrics provider
 * that collects PostgreSQL datasource-specific metrics related to statement cache and query preparation.
 *
 * <p>This provider extracts the following metrics from a {@link BaseDataSource} instance:
 * <ul>
 *     <li>{@code PREPARED_STATEMENT_CACHE_QUERIES} — the size of the prepared statement cache,</li>
 *     <li>{@code FETCH_SIZE} - default row fetch size with a descriptive label if zero,</li>
 *     <li>{@code PREPARE_THRESHOLD} — the threshold for query preparation.</li>
 * </ul>
 *
 * @since 23.06.2025
 * @author Mikhail Polivakha
 */
public class PgDataSourceStatementCacheSizeInQueriesReporter extends AbstractMetricsProvider {

    private final BaseDataSource dataSource;

    private static final String PREPARED_STATEMENT_CACHE_QUERIES_COUNT = "PREPARED_STATEMENT_CACHE_QUERIES_COUNT";
    private static final String FETCH_SIZE = "FETCH_SIZE";
    private static final String PREPARE_THRESHOLD = "PREPARE_THRESHOLD";

    public PgDataSourceStatementCacheSizeInQueriesReporter(BaseDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Metrics scratch() {

        Metrics metrics = Metrics.newMetrics(3);

        metrics.fineIntMetric(PREPARED_STATEMENT_CACHE_QUERIES_COUNT, dataSource.getPreparedStatementCacheQueries());

        int fetchSize = dataSource.getDefaultRowFetchSize();

        metrics.fineIntegerMetric(
                FETCH_SIZE, fetchSize, fetchSize == 0 ? "0 (As much as possible)" : String.valueOf(fetchSize));

        int prepareThreshold = dataSource.getPrepareThreshold();

        metrics.fineIntMetric(PREPARE_THRESHOLD, prepareThreshold);

        return metrics;
    }
}
