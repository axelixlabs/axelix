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
package com.axelixlabs.axelix.master.service.upgrades;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.common.utils.SemanticVersion;
import com.axelixlabs.axelix.master.api.external.response.upgrades.CeilingBlocker;
import com.axelixlabs.axelix.master.api.external.response.upgrades.StarterVersionUsage;
import com.axelixlabs.axelix.master.api.external.response.upgrades.UpgradesResponse;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.Insights;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultUpgradesService}.
 *
 * @author Nikita Kirillov
 */
@DatabaseMatrixTest
class DefaultUpgradesServiceTest {

    @Autowired
    private UpgradesService subject;

    @Autowired
    private AxelixVersionDiscoverer axelixVersionDiscoverer;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    private String masterVersion;

    @BeforeEach
    @AfterEach
    void cleanHistoricalApplicationSnapshots() {
        jdbcAggregateTemplate.deleteAll(HistoricalApplicationSnapshot.class);
    }

    @BeforeEach
    void setUp() {
        masterVersion =
                SemanticVersion.parse(axelixVersionDiscoverer.getVersion()).majorMinor();
    }

    @Test
    void returnsAnEmptyResponseWhenNoServiceHasBeenSeen() {
        // when.
        UpgradesResponse response = subject.getUpgrades();

        // then.
        assertThat(response.masterVersion()).isEqualTo(masterVersion);
        assertThat(response.servicesTotal()).isZero();
        assertThat(response.oldestStarterVersion()).isNull();
        assertThat(response.compatibilityWindow()).isEqualTo(4);
        assertThat(response.safeUpgradeCeiling()).isNull();
        assertThat(response.starterVersions()).isEmpty();
        assertThat(response.ceilingBlockers()).isEmpty();
    }

    @Test
    void computesTheSafeUpgradeCeilingFromTheOldestStarterLine() {
        // given. four services within the 30-day window; app-c and app-d are the oldest, on 1.2.x.
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        jdbcAggregateTemplate.insertAll(List.of(
                snapshot("app-a", today, "1.4.2"),
                snapshot("app-b", today, "1.3.1"),
                snapshot("app-c", today.minusDays(5), "1.2.1"),
                snapshot("app-d", today, "1.2.6")));

        // when.
        UpgradesResponse response = subject.getUpgrades();

        // then.
        assertThat(response.masterVersion()).isEqualTo(masterVersion);
        assertThat(response.servicesTotal()).isEqualTo(4);
        assertThat(response.oldestStarterVersion()).isEqualTo("1.2");
        assertThat(response.safeUpgradeCeiling()).isEqualTo("1.5"); // 1.2 + (compatibility window 4 - 1)

        assertThat(response.starterVersions())
                .extracting(StarterVersionUsage::version, StarterVersionUsage::serviceCount)
                .containsExactly(Tuple.tuple("1.4", 1), Tuple.tuple("1.3", 1), Tuple.tuple("1.2", 2));

        assertThat(response.ceilingBlockers())
                .extracting(
                        CeilingBlocker::artifactId,
                        CeilingBlocker::groupId,
                        CeilingBlocker::starterVersion,
                        CeilingBlocker::lastSeen)
                .containsExactly(
                        Tuple.tuple("app-c", "com.example", "1.2.1", today.minusDays(5)),
                        Tuple.tuple("app-d", "com.example", "1.2.6", today));
    }

    @Test
    void ignoresServicesNotSeenWithinTheObservationWindow() {
        // given. one service seen today, another only 31 days ago (just outside the 30-day window).
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        jdbcAggregateTemplate.insertAll(
                List.of(snapshot("current", today, "1.4.0"), snapshot("stale", today.minusDays(31), "1.0.0")));

        // when.
        UpgradesResponse response = subject.getUpgrades();

        // then.
        assertThat(response.servicesTotal()).isEqualTo(1);
        assertThat(response.oldestStarterVersion()).isEqualTo("1.4");
    }

    @Test
    void onlyTheLatestSnapshotOfEachServiceWithinTheWindowCounts() {
        // given. the same service reported an older version yesterday, and a newer one today.
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        jdbcAggregateTemplate.insertAll(
                List.of(snapshot("app-a", today.minusDays(1), "1.2.0"), snapshot("app-a", today, "1.4.0")));

        // when.
        UpgradesResponse response = subject.getUpgrades();

        // then.
        assertThat(response.servicesTotal()).isEqualTo(1);
        assertThat(response.oldestStarterVersion()).isEqualTo("1.4");
        assertThat(response.ceilingBlockers())
                .extracting(CeilingBlocker::starterVersion, CeilingBlocker::lastSeen)
                .containsExactly(Tuple.tuple("1.4.0", today));
    }

    private static HistoricalApplicationSnapshot snapshot(String artifactId, LocalDate date, String starterVersion) {
        return new HistoricalApplicationSnapshot(
                new SnapshotId("com.example", artifactId, date),
                new Insights(
                        new Insights.HotSpot(
                                new Insights.HotSpot.ProjectLeyden(false, false),
                                new Insights.HotSpot.GarbageCollector(false, GarbageCollector.G1),
                                new Insights.HotSpot.ProjectLilliput(false)),
                        new Insights.SpringFramework(false, null, null),
                        new PersistenceInsights(List.of())),
                starterVersion);
    }
}
