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
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.axelixlabs.axelix.common.api.registration.BasicRegistrationMetadata;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionAggregatedProfile;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionOrigin;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionOverallStats;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionalKey;
import com.axelixlabs.axelix.common.domain.insights.FeatureId;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.master.api.external.response.dashboard.AggregatedFeature;
import com.axelixlabs.axelix.master.api.external.response.dashboard.JavaDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.SpringFrameworkDashboardResponse;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.utils.TestMetadataFactory;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Base class for integration tests of the {@link DatabaseHistoricalApplicationSnapshotService}.
 *
 * @author Mikhail Polivakha
 */
@DatabaseMatrixTest
class DatabaseHistoricalApplicationSnapshotServiceTest {

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
            BasicRegistrationMetadata metadata = petclinicMetadata(true, true);

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

        @Test
        void shouldPersistPersistenceTransactionalInsightsAsJson() {
            // given.
            TransactionAggregatedProfile profile = new TransactionAggregatedProfile(
                    TransactionOrigin.APPLICATION_DECLARATIVE,
                    new TransactionalKey("com.example.OwnerService", "saveOwner"),
                    new TransactionOverallStats(1, 10, 5),
                    List.of(),
                    Map.of("com.example.Pet", 2));
            BasicRegistrationMetadata metadata = TestMetadataFactory.withPersistenceInsights(
                    "org.springframework.samples", "petclinic", new PersistenceInsights(List.of(profile)));

            // when.
            subject.reloadCurrentState(metadata);

            // then.
            HistoricalApplicationSnapshot snapshot = jdbcAggregateTemplate.findById(
                    new SnapshotId("org.springframework.samples", "petclinic", LocalDate.now(ZoneOffset.UTC)),
                    HistoricalApplicationSnapshot.class);

            assertThat(snapshot).isNotNull();
            assertThat(snapshot.insights().persistenceInsights().getTransactions())
                    .hasSize(1)
                    .first()
                    .satisfies(stored -> {
                        assertThat(stored.getTransactionOrigin()).isEqualTo(TransactionOrigin.APPLICATION_DECLARATIVE);
                        assertThat(stored.getTransactionalKey().getClassName()).isEqualTo("com.example.OwnerService");
                        assertThat(stored.getTransactionalKey().getMethodName()).isEqualTo("saveOwner");
                        assertThat(stored.getTransactionOverallStats().getMinMs())
                                .isEqualTo(1);
                        assertThat(stored.getTransactionOverallStats().getMaxMs())
                                .isEqualTo(10);
                        assertThat(stored.getTransactionOverallStats().getAverageMs())
                                .isEqualTo(5);
                        assertThat(stored.getInMemoryPagination()).containsEntry("com.example.Pet", 2);
                    });
        }
    }

    @Nested
    class ReloadCurrentStateBulk {

        @Test
        void shouldReloadCurrentStateBulk() {
            // given.
            BasicRegistrationMetadata petclinic = petclinicMetadata(true, true);
            BasicRegistrationMetadata otherApp = otherAppMetadata();

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
            assertThat(result).isNotNull();
            assertThat(result.snapshotId())
                    .isEqualTo(new SnapshotId("com.example", "service-a", LocalDate.now(ZoneOffset.UTC)));
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
            BasicRegistrationMetadata first =
                    TestMetadataFactory.withFeatures("com.example", "service-a", true, true, true, false, true);
            BasicRegistrationMetadata second =
                    TestMetadataFactory.withFeatures("com.example", "service-b", true, false, false, false, false);
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
            assertThat(dashboard.garbageCollectorDistribution()).containsEntry(GarbageCollector.G1, 100.0);
            assertThat(dashboard.garbageCollectorDistribution()).hasSize(1);
            assertThat(dashboard.projectLilliput())
                    .extracting(AggregatedFeature::featureId, AggregatedFeature::adoptionPercentage)
                    .containsExactly(tuple(FeatureId.COMPACT_OBJECT_HEADERS.getId(), 0.0));
        }

        @Test
        void shouldAggregateGarbageCollectorDistributionAcrossServices() {
            // given.
            BasicRegistrationMetadata first = TestMetadataFactory.withFeatures(
                    "com.example", "service-a", false, false, false, false, false, GarbageCollector.G1);
            BasicRegistrationMetadata second = TestMetadataFactory.withFeatures(
                    "com.example", "service-b", false, false, false, false, false, GarbageCollector.G1);
            BasicRegistrationMetadata third = TestMetadataFactory.withFeatures(
                    "com.example", "service-c", false, false, false, false, false, GarbageCollector.ZGC);
            subject.reloadCurrentStateBulk(List.of(first, second, third));

            // when.
            JavaDashboardResponse dashboard = subject.getJavaDashboard();

            // then.
            assertThat(dashboard.garbageCollectorDistribution())
                    .containsEntry(GarbageCollector.G1, 200.0 / 3)
                    .containsEntry(GarbageCollector.ZGC, 100.0 / 3);
        }

        @Test
        void shouldCountOnlyTheLatestSnapshotPerService() {
            // given a service whose latest snapshot has AppCDS disabled.
            BasicRegistrationMetadata staleMetadata =
                    TestMetadataFactory.withFeatures("com.example", "service-a", true, true, true, true, true);
            BasicRegistrationMetadata current =
                    TestMetadataFactory.withFeatures("com.example", "service-a", false, false, false, false, false);
            subject.reloadCurrentState(staleMetadata);
            subject.reloadCurrentState(current);

            // when.
            JavaDashboardResponse dashboard = subject.getJavaDashboard();

            // then.
            assertThat(dashboard.projectLeyden())
                    .extracting(AggregatedFeature::featureId, AggregatedFeature::adoptionPercentage)
                    .containsExactly(tuple(FeatureId.APP_CDS.getId(), 0.0), tuple(FeatureId.AOT_CACHE.getId(), 0.0));
            assertThat(dashboard.garbageCollectorDistribution()).containsEntry(GarbageCollector.G1, 100.0);
            assertThat(dashboard.garbageCollectorDistribution()).hasSize(1);
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
            assertThat(dashboard.garbageCollectorDistribution()).isEmpty();
        }
    }

    @Nested
    class GetSpringFrameworkDashboard {

        @Test
        void shouldAggregateSpringFrameworkFeaturesAdoptionAcrossServices() {
            // given three services, two of which have OSIV enabled.
            BasicRegistrationMetadata first =
                    TestMetadataFactory.withFeatures("com.example", "service-a", false, false, false, false, true);
            BasicRegistrationMetadata second =
                    TestMetadataFactory.withFeatures("com.example", "service-b", false, false, false, false, true);
            BasicRegistrationMetadata third =
                    TestMetadataFactory.withFeatures("com.example", "service-c", false, false, false, false, false);
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

    private static BasicRegistrationMetadata petclinicMetadata(boolean appCdsEnabled, boolean osivEnabled) {
        return TestMetadataFactory.withFeatures(
                "org.springframework.samples", "petclinic", appCdsEnabled, false, false, false, osivEnabled);
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
                        new com.axelixlabs.axelix.master.domain.Insights.SpringFramework(false),
                        new PersistenceInsights(List.of())));
    }

    private static BasicRegistrationMetadata otherAppMetadata() {
        return TestMetadataFactory.withFeatures(
                "com.example", "other-app", false, false, true, true, false, GarbageCollector.ZGC);
    }
}
