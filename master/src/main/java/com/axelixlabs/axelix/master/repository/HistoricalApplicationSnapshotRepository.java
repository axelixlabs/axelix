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

import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.axelixlabs.axelix.common.contract.metadata.PersistenceInsights;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.JdkVendor;

/**
 * Repository for the {@link HistoricalApplicationSnapshot} aggregate.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
public interface HistoricalApplicationSnapshotRepository
        extends CrudRepository<HistoricalApplicationSnapshot, SnapshotId> {

    @Query("""
            SELECT
                has.persistence_insights
            FROM historical_application_snapshots has
            INNER JOIN instances i
            ON
                i.artifact_id = has.artifact_id
                AND i.group_id = has.group_id
            WHERE
                i.instance_id = :instanceId
                AND has.date = (
                    SELECT MAX(latest.date)
                    FROM historical_application_snapshots latest
                    WHERE latest.group_id = has.group_id
                      AND latest.artifact_id = has.artifact_id
                )
            """)
    PersistenceInsights findLatestPersistenceInsightsForInstance(@Param("instanceId") String instanceId);

    /**
     * Returns the latest persistence insights of every service in the ecosystem. For every service (identified
     * by its {@code group_id} + {@code artifact_id}) only the most recent snapshot is taken into account, so that
     * a service is represented exactly once. The N + 1 / in-memory pagination counters are aggregated in Java
     * (from the deserialized JSON column) rather than in SQL, since the {@code persistence_insights} payload is
     * stored as a JSON document and cannot be summed in a vendor-agnostic way.
     *
     * @return one row per service, holding its {@code artifactId} and the deserialized persistence insights.
     */
    @Query("""
            SELECT
                s.artifact_id AS artifact_id,
                s.persistence_insights AS persistence_insights
            FROM historical_application_snapshots s
            WHERE s.date = (
                SELECT MAX(latest.date)
                FROM historical_application_snapshots latest
                WHERE latest.group_id = s.group_id
                  AND latest.artifact_id = s.artifact_id
            )
            """)
    List<ServicePersistenceInsights> findLatestPersistenceInsightsPerService();

    /**
     * Returns the latest starter version reported by every service seen at least once within the given window.
     * For every service (identified by its {@code group_id} + {@code artifact_id}) only the most recent snapshot
     * within the window is taken into account, so that a service is represented exactly once.
     *
     * @param since the earliest date (inclusive) a snapshot must fall on to be considered
     *
     * @return one row per service, holding its {@code groupId}, {@code artifactId}, latest {@code starterVersion}
     *         and the {@code date} of that latest snapshot.
     */
    @Query("""
            SELECT
                s.group_id AS group_id,
                s.artifact_id AS artifact_id,
                s.starter_version AS starter_version,
                s.date AS date
            FROM historical_application_snapshots s
            WHERE
                s.date >= :since
                AND s.date = (
                    SELECT MAX(latest.date)
                    FROM historical_application_snapshots latest
                    WHERE latest.group_id = s.group_id
                      AND latest.artifact_id = s.artifact_id
                      AND latest.date >= :since
                )
            """)
    List<LatestStarterVersion> findLatestStarterVersionsSince(@Param("since") LocalDate since);

    // Select * is generally a bad idea. Here, it does not cost that much, but still.
    @Query(value = """
        SELECT *
        FROM historical_application_snapshots s
        WHERE
            s.group_id = :groupId
            AND s.artifact_id = :artifactId
            AND s.date = (
                SELECT MAX(latest.date)
                FROM historical_application_snapshots latest
                WHERE latest.group_id = :groupId
                    AND latest.artifact_id = :artifactId
            )
        """)
    HistoricalApplicationSnapshot findLatestApplicationSnapshot(
            @Param("groupId") String groupId, @Param("artifactId") String artifactId);

    @Query("""
            SELECT
                s.spring_boot_version AS spring_boot_version,
                s.spring_framework_version AS spring_framework_version
            FROM historical_application_snapshots s
            WHERE
                s.date = (
                    SELECT MAX(latest.date)
                    FROM historical_application_snapshots latest
                    WHERE latest.group_id = s.group_id
                      AND latest.artifact_id = s.artifact_id
                )
                AND s.spring_boot_version IS NOT NULL
                AND s.spring_framework_version IS NOT NULL
            """)
    List<ApplicationPlatformVersions> findLatestPlatformVersionsPerService();

    /**
     * Returns the JVM languages profile of every service in the ecosystem. For every service (identified by its
     * {@code group_id} + {@code artifact_id}) only the most recent snapshot is taken into account, so that a service
     * is represented exactly once.
     *
     * @return one row per service, holding its Java feature release, JDK vendor, Kotlin version (or {@code null}) and
     *         the {@code date} of that most recent snapshot.
     */
    @Query("""
            SELECT
                s.java_feature AS java_version,
                s.jdk_vendor AS jdk_vendor,
                s.kotlin_version AS kotlin_version,
                s.date AS date
            FROM historical_application_snapshots s
            WHERE s.date = (
                SELECT MAX(latest.date)
                FROM historical_application_snapshots latest
                WHERE latest.group_id = s.group_id
                  AND latest.artifact_id = s.artifact_id
            )
            """)
    List<ApplicationLanguages> findLatestLanguagesPerService();

    /**
     * The latest persistence insights of a single service.
     *
     * @param artifactId the artifact id of the service (used as its displayable name).
     * @param persistenceInsights the persistence insights taken from the service's most recent snapshot.
     */
    record ServicePersistenceInsights(String artifactId, PersistenceInsights persistenceInsights) {}

    /**
     * The Spring Boot / Spring Framework versions of a single service, taken from its most recent snapshot.
     *
     * @param springBootVersion the Spring Boot version.
     * @param springFrameworkVersion the Spring Framework version.
     */
    record ApplicationPlatformVersions(String springBootVersion, String springFrameworkVersion) {}

    /**
     * The JVM languages profile of a single service, taken from its most recent snapshot.
     *
     * @param javaVersion   the Java feature release the service ran on (e.g. {@code 21}, {@code 25}).
     * @param jdkVendor     the vendor of the JDK distribution the service ran on.
     * @param kotlinVersion the Kotlin version the service ran on, or {@code null} if it does not use Kotlin.
     * @param date          the date of the service's most recent snapshot.
     */
    record ApplicationLanguages(
            int javaVersion, JdkVendor jdkVendor, @Nullable String kotlinVersion, LocalDate date) {}

    /**
     * The latest starter version reported by a single service within a time window.
     *
     * @param groupId        the group id of the service.
     * @param artifactId     the artifact id of the service.
     * @param starterVersion the Axelix starter version from the service's most recent snapshot in the window.
     * @param date           the date of that most recent snapshot.
     */
    record LatestStarterVersion(String groupId, String artifactId, String starterVersion, LocalDate date) {}
}
