package com.nucleonforge.axile.sbs.postgres;

import org.postgresql.ds.common.BaseDataSource;

import com.nucleonforge.axile.sbs.metrics.AbstractMetricsProvider;
import com.nucleonforge.axile.sbs.metrics.Metrics;

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
