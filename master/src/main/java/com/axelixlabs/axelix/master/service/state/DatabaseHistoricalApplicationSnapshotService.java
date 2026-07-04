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
package com.axelixlabs.axelix.master.service.state;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.domain.insights.FeatureId;
import com.axelixlabs.axelix.master.api.external.response.dashboard.AggregatedFeature;
import com.axelixlabs.axelix.master.api.external.response.dashboard.JavaDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.SpringFrameworkDashboardResponse;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.repository.HistoricalApplicationSnapshotRepository;
import com.axelixlabs.axelix.master.repository.HistoricalApplicationSnapshotRepository.JavaInsightsAggregate;
import com.axelixlabs.axelix.master.repository.HistoricalApplicationSnapshotRepository.SpringFrameworkInsightsAggregate;
import com.axelixlabs.axelix.master.service.convert.HistoricalApplicationSnapshotConverter;

/**
 * Service for operating over the {@link HistoricalApplicationSnapshot}.
 *
 * @author Mikhail Polivakha
 */
// TODO: Extract an interface
@Service
public class DatabaseHistoricalApplicationSnapshotService {

    private final HistoricalApplicationSnapshotConverter converter;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;
    private final HistoricalApplicationSnapshotRepository repository;

    public DatabaseHistoricalApplicationSnapshotService(
            HistoricalApplicationSnapshotConverter converter,
            JdbcAggregateTemplate jdbcAggregateTemplate,
            HistoricalApplicationSnapshotRepository repository) {
        this.converter = converter;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
        this.repository = repository;
    }

    /**
     * Builds the aggregated, ecosystem-wide Java/JVM features adoption view used to render the Java dashboard.
     *
     * @return the {@link JavaDashboardResponse} with the adoption percentage of every tracked Java/JVM feature.
     */
    @Transactional(readOnly = true)
    public JavaDashboardResponse getJavaDashboard() {
        JavaInsightsAggregate aggregate = repository.aggregateLatestJavaInsights();
        long total = aggregate.totalServices();

        List<AggregatedFeature> projectLeyden = List.of(
                new AggregatedFeature(
                        FeatureId.APP_CDS.getId(), adoptionPercentage(aggregate.appCdsEnabledCount(), total)),
                new AggregatedFeature(
                        FeatureId.AOT_CACHE.getId(), adoptionPercentage(aggregate.aotCacheEnabledCount(), total)));

        List<AggregatedFeature> gc = List.of(new AggregatedFeature(
                FeatureId.GC_LOGGING_ENABLED.getId(), adoptionPercentage(aggregate.gcLoggingEnabledCount(), total)));

        List<AggregatedFeature> projectLilliput = List.of(new AggregatedFeature(
                FeatureId.COMPACT_OBJECT_HEADERS.getId(),
                adoptionPercentage(aggregate.compactObjectHeadersEnabledCount(), total)));

        return new JavaDashboardResponse(projectLeyden, gc, projectLilliput);
    }

    /**
     * Builds the aggregated, ecosystem-wide Spring Framework features adoption view used to render the
     * Spring Framework dashboard.
     *
     * @return the {@link SpringFrameworkDashboardResponse} with the adoption percentage of every tracked
     *         Spring Framework feature.
     */
    @Transactional(readOnly = true)
    public SpringFrameworkDashboardResponse getSpringFrameworkDashboard() {
        SpringFrameworkInsightsAggregate aggregate = repository.aggregateLatestSpringFrameworkInsights();
        long total = aggregate.totalServices();

        return new SpringFrameworkDashboardResponse(List.of(new AggregatedFeature(
                FeatureId.OSIV.getId(), adoptionPercentage(aggregate.osivEnabledCount(), total))));
    }

    @Transactional(readOnly = true)
    public HistoricalApplicationSnapshot getCurrentRecord(ApplicationId applicationId) {
        return repository.findLatestApplicationSnapshot(applicationId.groupId(), applicationId.artifactId());
    }

    // TODO:
    //
    // Although Spring Data JDBC supports upserts, we cannot use it here, since we by default Axelix Master works
    // with sqlite, and we even have the custom dialect. Still, Spring Data JDBC does not allow for extension of UPSERTs
    // for custom dialects. I have filed a ticket for that, so, I hope this is gonna get done.
    @Transactional
    public void reloadCurrentState(BasicDiscoveryMetadata metadata) {
        HistoricalApplicationSnapshot applicationSnapshot = converter.currentSnapshot(metadata);

        jdbcAggregateTemplate.deleteById(applicationSnapshot.snapshotId(), HistoricalApplicationSnapshot.class);
        jdbcAggregateTemplate.insert(applicationSnapshot);
    }

    @Transactional
    public void reloadCurrentStateBulk(Collection<BasicDiscoveryMetadata> metadata) {
        // TODO:
        //  The incoming metadata array can generally be quite large (up to a couple of thousand of instances).
        //  By mapping inside the transaction in the loop, we're effectively elongating the transaction unnecessarily
        //  which is generally a bad practise.
        Set<HistoricalApplicationSnapshot> snapshots =
                metadata.stream().map(converter::currentSnapshot).collect(Collectors.toSet());

        jdbcAggregateTemplate.deleteAllById(
                snapshots.stream()
                        .map(HistoricalApplicationSnapshot::snapshotId)
                        .toList(),
                HistoricalApplicationSnapshot.class);

        jdbcAggregateTemplate.insertAll(snapshots);
    }

    private static double adoptionPercentage(long enabledCount, long total) {
        if (total == 0) {
            return 0.0;
        }
        return (enabledCount * 100.0) / total;
    }
}
