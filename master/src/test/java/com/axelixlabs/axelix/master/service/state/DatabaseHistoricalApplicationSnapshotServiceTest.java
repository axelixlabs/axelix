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

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.HealthStatus;
import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.HotSpot;
import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.InsightFeature;
import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.Insights;
import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.MemoryDetails;
import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.SoftwareVersions;
import com.axelixlabs.axelix.common.domain.insights.FeatureId;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.master.api.external.response.dashboard.AggregatedFeature;
import com.axelixlabs.axelix.master.api.external.response.dashboard.JavaDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.SpringFrameworkDashboardResponse;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Base class for integration tests of the {@link DatabaseHistoricalApplicationSnapshotService}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest
abstract class DatabaseHistoricalApplicationSnapshotServiceTest {

    @Autowired
    private DatabaseHistoricalApplicationSnapshotService subject;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @BeforeEach
    @AfterEach
    void cleanHistoricalApplicationSnapshots() {
        jdbcAggregateTemplate.deleteAll(HistoricalApplicationSnapshot.class);
    }

    @Nested
    class ReloadCurrentState {

        @Test
        void shouldReloadCurrentState() {
            // given.
            BasicDiscoveryMetadata metadata = petclinicMetadata(true, true);

            // and given initially loaded state.
            subject.reloadCurrentState(metadata);
            var snapshotId = new SnapshotId("org.springframework.samples", "petclinic", LocalDate.now(ZoneOffset.UTC));
            var snapshot = jdbcAggregateTemplate.findById(snapshotId, HistoricalApplicationSnapshot.class);
            assertThat(snapshot).isNotNull();
            assertThat(snapshot.insights().hotSpot().projectLeyden().appCdsEnabled())
                    .isTrue();
            assertThat(snapshot.insights().springFramework().osivEnabled()).isTrue();

            // when.
            subject.reloadCurrentState(petclinicMetadata(false, false));

            // then.
            var updatedSnapshot = jdbcAggregateTemplate.findById(snapshotId, HistoricalApplicationSnapshot.class);

            assertThat(updatedSnapshot).isNotNull();
            assertThat(updatedSnapshot.insights().hotSpot().projectLeyden().appCdsEnabled())
                    .isFalse();
            assertThat(updatedSnapshot.insights().springFramework().osivEnabled())
                    .isFalse();
        }
    }

    @Nested
    class ReloadCurrentStateBulk {

        @Test
        void shouldReloadCurrentStateBulk() {
            // given.
            BasicDiscoveryMetadata petclinic = petclinicMetadata(true, true);
            BasicDiscoveryMetadata otherApp = otherAppMetadata();

            // when.
            subject.reloadCurrentStateBulk(List.of(petclinic, otherApp));

            // then.
            HistoricalApplicationSnapshot petclinicSnapshot = jdbcAggregateTemplate.findById(
                    new SnapshotId("org.springframework.samples", "petclinic", LocalDate.now(ZoneOffset.UTC)),
                    HistoricalApplicationSnapshot.class);
            HistoricalApplicationSnapshot otherAppSnapshot = jdbcAggregateTemplate.findById(
                    new SnapshotId("com.example", "other-app", LocalDate.now(ZoneOffset.UTC)),
                    HistoricalApplicationSnapshot.class);

            assertThat(petclinicSnapshot).isNotNull();
            assertThat(petclinicSnapshot.insights().hotSpot().projectLeyden().appCdsEnabled())
                    .isTrue();
            assertThat(otherAppSnapshot).isNotNull();
            assertThat(otherAppSnapshot.insights().springFramework().osivEnabled())
                    .isFalse();
        }
    }

    @Nested
    class GetCurrentRecord {

        @Test
        void shouldReturnLatestSnapshotForApplication() {
            // given.
            HistoricalApplicationSnapshot previous = snapshot(
                    "com.example", "service-a", LocalDate.now(ZoneOffset.UTC).minusDays(1), GarbageCollector.G1);
            HistoricalApplicationSnapshot current =
                    snapshot("com.example", "service-a", LocalDate.now(ZoneOffset.UTC), GarbageCollector.ZGC);
            HistoricalApplicationSnapshot otherApplication =
                    snapshot("com.example", "service-b", LocalDate.now(ZoneOffset.UTC), GarbageCollector.PARALLEL);
            jdbcAggregateTemplate.insertAll(List.of(previous, current, otherApplication));

            // when.
            HistoricalApplicationSnapshot result =
                    subject.getCurrentRecord(ApplicationId.of("com.example", "service-a"));

            // then.
            assertThat(result).isEqualTo(current);
            assertThat(result.insights().hotSpot().gc().gcInUse()).isEqualTo(GarbageCollector.ZGC);
        }

        @Test
        void shouldReturnNullWhenApplicationHasNoSnapshots() {
            // given.
            jdbcAggregateTemplate.insert(
                    snapshot("com.example", "service-a", LocalDate.now(ZoneOffset.UTC), GarbageCollector.G1));

            // when.
            HistoricalApplicationSnapshot result =
                    subject.getCurrentRecord(ApplicationId.of("com.example", "missing-service"));

            // then.
            assertThat(result).isNull();
        }
    }

    @Nested
    class GetJavaDashboard {

        @Test
        void shouldAggregateJavaFeaturesAdoptionAcrossServices() {
            // given two distinct services with different Java/JVM features enabled.
            BasicDiscoveryMetadata first = metadata("com.example", "service-a", true, true, true, false, true);
            BasicDiscoveryMetadata second = metadata("com.example", "service-b", true, false, false, false, false);
            subject.reloadCurrentStateBulk(List.of(first, second));

            // when.
            JavaDashboardResponse dashboard = subject.getJavaDashboard();

            // then.
            assertThat(dashboard.projectLeyden())
                    .extracting(AggregatedFeature::featureId, AggregatedFeature::adoptionPercentage)
                    .containsExactly(tuple(FeatureId.APP_CDS.getId(), 100.0), tuple(FeatureId.AOT_CACHE.getId(), 50.0));
            assertThat(dashboard.gc())
                    .extracting(AggregatedFeature::featureId, AggregatedFeature::adoptionPercentage)
                    .containsExactly(tuple(FeatureId.GC_LOGGING_ENABLED.getId(), 50.0));
            assertThat(dashboard.projectLilliput())
                    .extracting(AggregatedFeature::featureId, AggregatedFeature::adoptionPercentage)
                    .containsExactly(tuple(FeatureId.COMPACT_OBJECT_HEADERS.getId(), 0.0));
        }

        @Test
        void shouldCountOnlyTheLatestSnapshotPerService() {
            // given a service whose latest snapshot has AppCDS disabled.
            BasicDiscoveryMetadata current = metadata("com.example", "service-a", false, false, false, false, false);
            subject.reloadCurrentState(metadata("com.example", "service-a", true, true, true, true, true));
            subject.reloadCurrentState(current);

            // when.
            JavaDashboardResponse dashboard = subject.getJavaDashboard();

            // then.
            assertThat(dashboard.projectLeyden())
                    .extracting(AggregatedFeature::featureId, AggregatedFeature::adoptionPercentage)
                    .containsExactly(tuple(FeatureId.APP_CDS.getId(), 0.0), tuple(FeatureId.AOT_CACHE.getId(), 0.0));
        }

        @Test
        void shouldReturnZeroAdoptionWhenNoSnapshotsExist() {
            // when.
            JavaDashboardResponse dashboard = subject.getJavaDashboard();

            // then.
            assertThat(dashboard.projectLeyden())
                    .extracting(AggregatedFeature::adoptionPercentage)
                    .containsOnly(0.0);
            assertThat(dashboard.gc())
                    .extracting(AggregatedFeature::adoptionPercentage)
                    .containsOnly(0.0);
            assertThat(dashboard.projectLilliput())
                    .extracting(AggregatedFeature::adoptionPercentage)
                    .containsOnly(0.0);
        }
    }

    @Nested
    class GetSpringFrameworkDashboard {

        @Test
        void shouldAggregateSpringFrameworkFeaturesAdoptionAcrossServices() {
            // given three services, two of which have OSIV enabled.
            BasicDiscoveryMetadata first = metadata("com.example", "service-a", false, false, false, false, true);
            BasicDiscoveryMetadata second = metadata("com.example", "service-b", false, false, false, false, true);
            BasicDiscoveryMetadata third = metadata("com.example", "service-c", false, false, false, false, false);
            subject.reloadCurrentStateBulk(List.of(first, second, third));

            // when.
            SpringFrameworkDashboardResponse dashboard = subject.getSpringFrameworkDashboard();

            // then.
            assertThat(dashboard.features())
                    .extracting(AggregatedFeature::featureId, AggregatedFeature::adoptionPercentage)
                    .containsExactly(tuple(FeatureId.OSIV.getId(), 200.0 / 3));
        }

        @Test
        void shouldReturnZeroAdoptionWhenNoSnapshotsExist() {
            // when.
            SpringFrameworkDashboardResponse dashboard = subject.getSpringFrameworkDashboard();

            // then.
            assertThat(dashboard.features())
                    .extracting(AggregatedFeature::featureId, AggregatedFeature::adoptionPercentage)
                    .containsExactly(tuple(FeatureId.OSIV.getId(), 0.0));
        }
    }

    private static BasicDiscoveryMetadata petclinicMetadata(boolean appCdsEnabled, boolean osivEnabled) {
        SoftwareVersions softwareVersions = new SoftwareVersions("25", "3.5.0", "6.1.2", null);

        Insights insights = new Insights(
                new HotSpot(
                        List.of(new InsightFeature("AppCDS", appCdsEnabled)),
                        List.of(new InsightFeature("GCLoggingEnabled", false)),
                        List.of()),
                List.of(new InsightFeature("OSIV", osivEnabled)));

        return new BasicDiscoveryMetadata(
                "1.0.0-SNAPSHOT",
                "3.5.0-SNAPSHOT",
                "org.springframework.samples",
                "petclinic",
                "a8b0929",
                "BellSoft",
                GarbageCollector.G1,
                softwareVersions,
                HealthStatus.UP,
                new MemoryDetails(12_000),
                insights);
    }

    private static HistoricalApplicationSnapshot snapshot(
            String groupId, String artifactId, LocalDate date, GarbageCollector garbageCollector) {

        return new HistoricalApplicationSnapshot(
                new SnapshotId(groupId, artifactId, date),
                new com.axelixlabs.axelix.master.domain.Insights(
                        new com.axelixlabs.axelix.master.domain.Insights.HotSpot(
                                new com.axelixlabs.axelix.master.domain.Insights.HotSpot.ProjectLeyden(false, false),
                                new com.axelixlabs.axelix.master.domain.Insights.HotSpot.GarbageCollector(
                                        false, garbageCollector),
                                new com.axelixlabs.axelix.master.domain.Insights.HotSpot.ProjectLilliput(false)),
                        new com.axelixlabs.axelix.master.domain.Insights.SpringFramework(false)));
    }

    private static BasicDiscoveryMetadata otherAppMetadata() {
        SoftwareVersions softwareVersions = new SoftwareVersions("21", "1.0.0", "6.2.0", null);

        Insights insights = new Insights(
                new HotSpot(
                        List.of(new InsightFeature("AppCDS", false)),
                        List.of(new InsightFeature("GCLoggingEnabled", true)),
                        List.of(new InsightFeature("CompactObjectHeaders", true))),
                List.of(new InsightFeature("OSIV", false)));

        return new BasicDiscoveryMetadata(
                "1.0.0-SNAPSHOT",
                "1.0.0",
                "com.example",
                "other-app",
                "910230",
                "Eclipse Temurin",
                GarbageCollector.ZGC,
                softwareVersions,
                HealthStatus.DOWN,
                new MemoryDetails(8_000),
                insights);
    }

    private static BasicDiscoveryMetadata metadata(
            String groupId,
            String artifactId,
            boolean appCdsEnabled,
            boolean aotCacheEnabled,
            boolean gcLoggingEnabled,
            boolean compactObjectHeadersEnabled,
            boolean osivEnabled) {
        SoftwareVersions softwareVersions = new SoftwareVersions("25", "3.5.0", "6.2.0", null);

        Insights insights = new Insights(
                new HotSpot(
                        List.of(
                                new InsightFeature(FeatureId.APP_CDS.getId(), appCdsEnabled),
                                new InsightFeature(FeatureId.AOT_CACHE.getId(), aotCacheEnabled)),
                        List.of(new InsightFeature(FeatureId.GC_LOGGING_ENABLED.getId(), gcLoggingEnabled)),
                        List.of(new InsightFeature(
                                FeatureId.COMPACT_OBJECT_HEADERS.getId(), compactObjectHeadersEnabled))),
                List.of(new InsightFeature(FeatureId.OSIV.getId(), osivEnabled)));

        return new BasicDiscoveryMetadata(
                "1.0.0-SNAPSHOT",
                "3.5.0",
                groupId,
                artifactId,
                "a8b0929",
                "BellSoft",
                GarbageCollector.G1,
                softwareVersions,
                HealthStatus.UP,
                new MemoryDetails(12_000),
                insights);
    }
}
