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
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the {@link HistoricalApplicationSnapshotService}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest
class HistoricalApplicationSnapshotServiceTest {

    @Autowired
    private HistoricalApplicationSnapshotService subject;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @BeforeEach
    @AfterEach
    void cleanHistoricalApplicationSnapshots() {
        jdbcAggregateTemplate.deleteAll(HistoricalApplicationSnapshot.class);
    }

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
        assertThat(updatedSnapshot.insights().springFramework().osivEnabled()).isFalse();
    }

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
        assertThat(otherAppSnapshot.insights().springFramework().osivEnabled()).isFalse();
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
                softwareVersions,
                HealthStatus.UP,
                new MemoryDetails(12_000),
                insights);
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
                softwareVersions,
                HealthStatus.DOWN,
                new MemoryDetails(8_000),
                insights);
    }
}
