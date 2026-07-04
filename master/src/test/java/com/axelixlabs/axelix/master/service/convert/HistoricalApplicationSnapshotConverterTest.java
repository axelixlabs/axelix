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

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;

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
        BasicDiscoveryMetadata metadata = sampleMetadata();

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
    }

    private static BasicDiscoveryMetadata sampleMetadata() {
        BasicDiscoveryMetadata.SoftwareVersions softwareVersions =
                new BasicDiscoveryMetadata.SoftwareVersions("25", "3.5.0", "6.1.2", null);

        BasicDiscoveryMetadata.MemoryDetails memoryDetails = new BasicDiscoveryMetadata.MemoryDetails(12_000);

        BasicDiscoveryMetadata.Insights insights = new BasicDiscoveryMetadata.Insights(
                new BasicDiscoveryMetadata.HotSpot(
                        List.of(
                                new BasicDiscoveryMetadata.InsightFeature("AotCache", false),
                                new BasicDiscoveryMetadata.InsightFeature("AppCDS", true)),
                        List.of(
                                new BasicDiscoveryMetadata.InsightFeature("GCLoggingEnabled", false),
                                new BasicDiscoveryMetadata.InsightFeature("GCLogFileSpecified", false)),
                        List.of(new BasicDiscoveryMetadata.InsightFeature("CompactObjectHeaders", false))),
                List.of(new BasicDiscoveryMetadata.InsightFeature("OSIV", true)));

        return new BasicDiscoveryMetadata(
                "1.0.0-SNAPSHOT",
                "3.5.0-SNAPSHOT",
                "org.springframework.samples",
                "petclinic",
                "a8b0929",
                "BellSoft",
                GarbageCollector.G1,
                softwareVersions,
                BasicDiscoveryMetadata.HealthStatus.UP,
                memoryDetails,
                insights);
    }
}
