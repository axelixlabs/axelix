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
package com.axelixlabs.axelix.master.service.convert;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.master.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.master.contract.metadata.ExecutionStats;
import com.axelixlabs.axelix.master.contract.metadata.HealthStatus;
import com.axelixlabs.axelix.master.contract.metadata.MemoryDetails;
import com.axelixlabs.axelix.master.contract.metadata.PersistenceInsights;
import com.axelixlabs.axelix.master.contract.metadata.SoftwareVersions;
import com.axelixlabs.axelix.master.contract.metadata.TransactionAggregatedProfile;
import com.axelixlabs.axelix.master.contract.metadata.TransactionOrigin;
import com.axelixlabs.axelix.master.contract.metadata.TransactionalKey;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.utils.TestMetadataFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link HistoricalApplicationSnapshotConverter}.
 *
 * @author Mikhail Polivakha
 */
class HistoricalApplicationSnapshotConverterTest {

    private final HistoricalApplicationSnapshotConverter subject = new HistoricalApplicationSnapshotConverter();

    @Test
    void shouldMapAllFieldsCorrectly() {
        // given.
        BasicRegistrationMetadata metadata = TestMetadataFactory.create("org.springframework.samples", "petclinic");

        // when.
        HistoricalApplicationSnapshot snapshot = subject.currentSnapshot(metadata);

        // then.
        SnapshotId expectedSnapshotId =
                new SnapshotId("org.springframework.samples", "petclinic", LocalDate.now(ZoneOffset.UTC));
        assertThat(snapshot.snapshotId()).isEqualTo(expectedSnapshotId);
        assertThat(snapshot.insights().hotSpot().projectLeyden().appCdsEnabled())
                .isTrue();
        assertThat(snapshot.insights().hotSpot().projectLeyden().aotCacheEnabled())
                .isFalse();
        assertThat(snapshot.insights().hotSpot().gc().gcLoggingEnabled()).isFalse();
        assertThat(snapshot.insights().hotSpot().gc().gcInUse()).isEqualTo(GarbageCollector.G1);
        assertThat(snapshot.insights().hotSpot().projectLilliput().compactObjectHeadersEnabled())
                .isFalse();
        assertThat(snapshot.insights().springFramework().osivEnabled()).isTrue();
        assertThat(snapshot.insights().persistenceInsights().getTransactions()).isEmpty();
    }

    @Test
    void shouldMapPersistenceTransactionalInsights() {
        // given.
        TransactionAggregatedProfile profile = new TransactionAggregatedProfile()
                .transactionOrigin(TransactionOrigin.APPLICATION_DECLARATIVE)
                .transactionalKey(new TransactionalKey()
                        .className("com.example.OwnerService")
                        .methodName("saveOwner"))
                .transactionOverallStats(
                        new ExecutionStats().minMs(1L).maxMs(10L).averageMs(5L))
                .lazyLoadingTargets(List.of())
                .inMemoryPagination(Map.of("com.example.Pet", 2))
                .externalCalls(List.of())
                .propagation("REQUIRED")
                .isolation("DEFAULT")
                .readOnly(false);
        BasicRegistrationMetadata metadata = TestMetadataFactory.withPersistenceInsights(
                "org.springframework.samples", "petclinic", new PersistenceInsights().transactions(List.of(profile)));

        // when.
        HistoricalApplicationSnapshot snapshot = subject.currentSnapshot(metadata);

        // then.
        assertThat(snapshot.insights().persistenceInsights().getTransactions())
                .hasSize(1)
                .first()
                .satisfies(mapped -> {
                    assertThat(mapped.getTransactionOrigin()).isEqualTo(TransactionOrigin.APPLICATION_DECLARATIVE);
                    assertThat(mapped.getTransactionalKey().getClassName()).isEqualTo("com.example.OwnerService");
                    assertThat(mapped.getTransactionalKey().getMethodName()).isEqualTo("saveOwner");
                    assertThat(mapped.getTransactionOverallStats().getMinMs()).isEqualTo(1);
                    assertThat(mapped.getTransactionOverallStats().getMaxMs()).isEqualTo(10);
                    assertThat(mapped.getTransactionOverallStats().getAverageMs())
                            .isEqualTo(5);
                    assertThat(mapped.getInMemoryPagination()).containsEntry("com.example.Pet", 2);
                });
    }

    @Test
    void shouldDefaultInsteadOfThrowingWhenGcInUseAndInsightsAreMissing() {
        // given.
        BasicRegistrationMetadata metadata = new BasicRegistrationMetadata()
                .version("1.0.0-SNAPSHOT")
                .serviceVersion("3.5.0-SNAPSHOT")
                .groupId("org.springframework.samples")
                .artifactId("petclinic")
                .commitShortSha("a8b0929")
                .jdkVendor("BellSoft")
                .softwareVersions(
                        new SoftwareVersions().java("25").springBoot("3.5.0").springFramework("6.1.2"))
                .healthStatus(HealthStatus.UP)
                .memoryDetails(new MemoryDetails().heap(12_000L));

        // when.
        HistoricalApplicationSnapshot snapshot = subject.currentSnapshot(metadata);

        // then.
        assertThat(snapshot.insights().hotSpot().gc().gcInUse()).isEqualTo(GarbageCollector.UNKNOWN);
        assertThat(snapshot.insights().hotSpot().projectLeyden().appCdsEnabled())
                .isFalse();
        assertThat(snapshot.insights().springFramework().osivEnabled()).isFalse();
        assertThat(snapshot.insights().persistenceInsights().getTransactions()).isEmpty();
    }

    @Test
    void shouldPreserveReportedGcInUseWhenInsightsIsMissing() {
        // given.
        BasicRegistrationMetadata metadata = new BasicRegistrationMetadata()
                .version("1.0.0-SNAPSHOT")
                .serviceVersion("3.5.0-SNAPSHOT")
                .groupId("org.springframework.samples")
                .artifactId("petclinic")
                .commitShortSha("a8b0929")
                .jdkVendor("BellSoft")
                .gcInUse(com.axelixlabs.axelix.master.contract.metadata.GarbageCollector.G1)
                .softwareVersions(
                        new SoftwareVersions().java("25").springBoot("3.5.0").springFramework("6.1.2"))
                .healthStatus(HealthStatus.UP)
                .memoryDetails(new MemoryDetails().heap(12_000L));
        // insights is deliberately left unset.

        // when.
        HistoricalApplicationSnapshot snapshot = subject.currentSnapshot(metadata);

        // then.
        assertThat(snapshot.insights().hotSpot().gc().gcInUse()).isEqualTo(GarbageCollector.G1);
    }

    @Test
    void shouldDefaultInsteadOfThrowingWhenPersistenceInsightsIsMissing() {
        // given.
        BasicRegistrationMetadata metadata = TestMetadataFactory.create("org.springframework.samples", "petclinic");
        metadata.getInsights().persistenceInsights(null);

        // when / then.
        assertThat(subject.currentSnapshot(metadata)
                        .insights()
                        .persistenceInsights()
                        .getTransactions())
                .isEmpty();
    }
}
