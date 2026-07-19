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

import com.axelixlabs.axelix.common.api.registration.BasicRegistrationMetadata;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionAggregatedProfile;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionOrigin;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionOverallStats;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionalKey;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
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
        TransactionAggregatedProfile profile = new TransactionAggregatedProfile(
                TransactionOrigin.APPLICATION_DECLARATIVE,
                new TransactionalKey("com.example.OwnerService", "saveOwner"),
                new TransactionOverallStats(1, 10, 5),
                List.of(),
                Map.of("com.example.Pet", 2),
                List.of());
        BasicRegistrationMetadata metadata = TestMetadataFactory.withPersistenceInsights(
                "org.springframework.samples", "petclinic", new PersistenceInsights(List.of(profile)));

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
}
