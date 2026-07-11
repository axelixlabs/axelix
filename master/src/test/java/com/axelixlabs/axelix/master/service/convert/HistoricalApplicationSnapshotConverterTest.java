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

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.registration.BasicRegistrationMetadata;
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
    }
}
