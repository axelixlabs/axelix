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
package com.axelixlabs.axelix.master.api.external.endpoint;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.common.utils.SemanticVersion;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.Insights;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoints;
import com.axelixlabs.axelix.master.service.discovery.WindowCompatibilityDetectionStrategy;
import com.axelixlabs.axelix.master.utils.IdentityAwareTestRestTemplate;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.AbstractProtectedEndpointTest;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link UpgradesApi}.
 *
 * @author Nikita Kirillov
 */
class UpgradesApiTest extends AbstractProtectedEndpointTest {

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @Autowired
    private AxelixVersionDiscoverer axelixVersionDiscoverer;

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
    void shouldReturnUpgrades() {
        // given.
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        jdbcAggregateTemplate.insertAll(List.of(snapshot("app-a", today, "1.1.2"), snapshot("app-b", today, "1.2.1")));

        // when.
        IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();
        ResponseEntity<String> response = viewer.getForEntity("/api/external/upgrades", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).node("masterVersion").isString().isEqualTo(masterVersion);
        assertThatJson(response.getBody())
                .node("compatibilityWindow")
                .isEqualTo(WindowCompatibilityDetectionStrategy.WINDOW_SIZE);
        assertThatJson(response.getBody())
                .node("starterVersions[0].version")
                .isString()
                .isEqualTo("1.2");
        assertThatJson(response.getBody())
                .node("starterVersions[0].serviceCount")
                .isEqualTo(1);
        assertThatJson(response.getBody())
                .node("starterVersions[1].version")
                .isString()
                .isEqualTo("1.1");
        assertThatJson(response.getBody())
                .node("starterVersions[1].serviceCount")
                .isEqualTo(1);
        assertThatJson(response.getBody())
                .node("ceilingBlockers[0].artifactId")
                .isString()
                .isEqualTo("app-a");
        assertThatJson(response.getBody())
                .node("ceilingBlockers[0].groupId")
                .isString()
                .isEqualTo("com.example");
        assertThatJson(response.getBody())
                .node("ceilingBlockers[0].starterVersion")
                .isString()
                .isEqualTo("1.1.2");
        assertSuccessfulCallback(MasterWebEndpoints.UPGRADES_READ, viewer.getActor());
    }

    @Test
    void shouldReturnEmptyUpgradesWhenNoServiceHasBeenSeen() {
        // when.
        IdentityAwareTestRestTemplate viewer = restTemplate.asViewer();
        ResponseEntity<String> response = viewer.getForEntity("/api/external/upgrades", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThatJson(response.getBody()).node("masterVersion").isString().isEqualTo(masterVersion);
        assertThatJson(response.getBody())
                .node("compatibilityWindow")
                .isEqualTo(WindowCompatibilityDetectionStrategy.WINDOW_SIZE);
        assertThatJson(response.getBody()).node("starterVersions").isArray().isEmpty();
        assertThatJson(response.getBody()).node("ceilingBlockers").isArray().isEmpty();
        assertSuccessfulCallback(MasterWebEndpoints.UPGRADES_READ, viewer.getActor());
    }

    @Override
    protected Set<TestableMasterWebEndpoint> endpointsUnderTest() {
        return Set.of(new TestableMasterWebEndpoint(MasterWebEndpoints.UPGRADES_READ, "/api/external/upgrades"));
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
