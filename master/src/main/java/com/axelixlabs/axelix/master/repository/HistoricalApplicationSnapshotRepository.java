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
package com.axelixlabs.axelix.master.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;

/**
 * Repository for the {@link HistoricalApplicationSnapshot} aggregate.
 *
 * @author Mikhail Polivakha
 */
public interface HistoricalApplicationSnapshotRepository extends Repository<HistoricalApplicationSnapshot, SnapshotId> {

    /**
     * Aggregates the adoption of the tracked Java/JVM features across the entire ecosystem. For every
     * service (identified by its {@code group_id} + {@code artifact_id}) only the most recent snapshot
     * is taken into account, so that a service is counted exactly once. The query is intentionally
     * kept vendor-agnostic so that it runs identically on all RDBMS vendors.
     *
     * @return the single-row aggregate with the total number of services and per-feature usage counters.
     */
    @Query("""
            SELECT
                COUNT(*) AS total_services,
                COALESCE(SUM(CASE WHEN s.app_cds_enabled = TRUE THEN 1 ELSE 0 END), 0) AS app_cds_enabled_count,
                COALESCE(SUM(CASE WHEN s.aot_cache_enabled = TRUE THEN 1 ELSE 0 END), 0) AS aot_cache_enabled_count,
                COALESCE(SUM(CASE WHEN s.gc_logging_enabled = TRUE THEN 1 ELSE 0 END), 0) AS gc_logging_enabled_count,
                COALESCE(SUM(CASE WHEN s.compact_object_headers_enabled = TRUE THEN 1 ELSE 0 END), 0) AS compact_object_headers_enabled_count
            FROM historical_application_snapshots s
            WHERE s.date = (
                SELECT MAX(latest.date)
                FROM historical_application_snapshots latest
                WHERE latest.group_id = s.group_id
                  AND latest.artifact_id = s.artifact_id
            )
            """)
    JavaInsightsAggregate aggregateLatestJavaInsights();

    /**
     * Aggregates the adoption of the tracked Spring Framework features across the entire ecosystem. For
     * every service (identified by its {@code group_id} + {@code artifact_id}) only the most recent
     * snapshot is taken into account, so that a service is counted exactly once. The query is intentionally
     * kept vendor-agnostic so that it runs identically on all RDBMS vendors.
     *
     * @return the single-row aggregate with the total number of services and per-feature usage counters.
     */
    @Query("""
            SELECT
                COUNT(*) AS total_services,
                COALESCE(SUM(CASE WHEN s.osiv_enabled = TRUE THEN 1 ELSE 0 END), 0) AS osiv_enabled_count
            FROM historical_application_snapshots s
            WHERE s.date = (
                SELECT MAX(latest.date)
                FROM historical_application_snapshots latest
                WHERE latest.group_id = s.group_id
                  AND latest.artifact_id = s.artifact_id
            )
            """)
    SpringFrameworkInsightsAggregate aggregateLatestSpringFrameworkInsights();

    // Select * is generally a bad idea. Here, it does not cost that much, but still.
    @Query(value = """
        SELECT *
        FROM historical_application_snapshots s
        WHERE
            group_id = :groupId
            AND artifact_id = :artifactId
            AND s.date = (
                SELECT MAX(latest.date)
                FROM historical_application_snapshots latest
                WHERE latest.group_id = :groupId
                    AND latest.artifact_id = :artifactId
            )
        """)
    HistoricalApplicationSnapshot findLatestApplicationSnapshot(
            @Param("groupId") String groupId, @Param("artifactId") String artifactId);

    /**
     * Aggregated, ecosystem-wide adoption counters for the tracked Java/JVM features.
     *
     * @param totalServices the total number of distinct services that reported at least one snapshot.
     * @param appCdsEnabledCount how many services have AppCDS enabled.
     * @param aotCacheEnabledCount how many services have the AOT cache enabled.
     * @param gcLoggingEnabledCount how many services have GC logging enabled.
     * @param compactObjectHeadersEnabledCount how many services have compact object headers enabled.
     */
    record JavaInsightsAggregate(
            long totalServices,
            long appCdsEnabledCount,
            long aotCacheEnabledCount,
            long gcLoggingEnabledCount,
            long compactObjectHeadersEnabledCount) {}

    /**
     * Aggregated, ecosystem-wide adoption counters for the tracked Spring Framework features.
     *
     * @param totalServices the total number of distinct services that reported at least one snapshot.
     * @param osivEnabledCount how many services have OSIV enabled.
     */
    record SpringFrameworkInsightsAggregate(long totalServices, long osivEnabledCount) {}
}
