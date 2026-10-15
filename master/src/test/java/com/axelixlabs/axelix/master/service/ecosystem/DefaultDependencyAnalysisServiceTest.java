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
package com.axelixlabs.axelix.master.service.ecosystem;

import java.time.Instant;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.axelixlabs.axelix.master.api.external.response.dashboard.DependenciesDashboardResponse;
import com.axelixlabs.axelix.master.domain.ApplicationDependencies;
import com.axelixlabs.axelix.master.domain.ApplicationDependencies.ApplicationDependenciesId;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.utils.CycloneDxDocuments;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultDependencyAnalysisService}.
 *
 * @author Nikita Kirillov
 */
@DatabaseMatrixTest
class DefaultDependencyAnalysisServiceTest {

    private static final ApplicationId APPLICATION_ID = ApplicationId.of("com.axelixlabs.test", "app");

    @Autowired
    private DependencyAnalysisService subject;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @BeforeEach
    void setUp() {
        jdbcAggregateTemplate.deleteAll(ApplicationDependencies.class);
    }

    @Test
    void shouldReplaceTheSbomOfTheApplicationOnEveryStore() {
        // given.
        subject.store(
                APPLICATION_ID, CycloneDxDocuments.of("com.axelixlabs.test:app:1.0.0", "org.slf4j:slf4j-api:2.0.9"));

        // when. a newer version of the same application registers.
        String newer = CycloneDxDocuments.of("com.axelixlabs.test:app:1.1.0", "org.slf4j:slf4j-api:2.0.13");
        subject.store(APPLICATION_ID, newer);

        // then.
        assertThat(jdbcAggregateTemplate.findAll(ApplicationDependencies.class))
                .singleElement()
                .extracting(ApplicationDependencies::sbom)
                .isEqualTo(newer);
    }

    @Test
    void shouldKeepThePreviousSbomWhenTheNewOneIsMalformed() {
        // given.
        String previous = CycloneDxDocuments.of("com.axelixlabs.test:app:1.0.0", "org.slf4j:slf4j-api:2.0.9");
        subject.store(APPLICATION_ID, previous);

        // when.
        subject.store(APPLICATION_ID, "{ \"bomFormat\": \"CycloneDX\"");

        // then.
        assertThat(jdbcAggregateTemplate.findById(
                        ApplicationDependenciesId.of(APPLICATION_ID), ApplicationDependencies.class))
                .extracting(ApplicationDependencies::sbom)
                .isEqualTo(previous);
    }

    @Test
    void shouldStoreAnSbomLargerThanAPlainTextColumnHolds() {
        // given. MySQL caps TEXT at 64 KiB, which a large application can outgrow.
        String sbom = CycloneDxDocuments.of(
                "com.axelixlabs.test:app:1.0.0",
                IntStream.range(0, 500)
                        .mapToObj(index -> "com.example.library:artifact-number-" + index + ":1.0.0")
                        .toArray(String[]::new));

        // when.
        subject.store(APPLICATION_ID, sbom);

        // then.
        assertThat(sbom.length()).isGreaterThan(64 * 1024);
        assertThat(jdbcAggregateTemplate.findById(
                        ApplicationDependenciesId.of(APPLICATION_ID), ApplicationDependencies.class))
                .extracting(ApplicationDependencies::sbom)
                .isEqualTo(sbom);
    }

    @Test
    void shouldReturnAnEmptyDashboardWhenNoApplicationReportedItsSbom() {
        // when.
        DependenciesDashboardResponse dashboard = subject.getDashboard();

        // then.
        assertThat(dashboard.totalServices()).isZero();
        assertThat(dashboard.servicesWithRiskyDependencies()).isZero();
        assertThat(dashboard.servicesByStatus()).isEmpty();
        assertThat(dashboard.trackedProjects()).isEmpty();
    }

    @Test
    void shouldSkipAnUnparsableStoredSbomOnTheDashboard() {
        // given. a stored SBOM that no longer parses, e.g. one written before a parser change.
        jdbcAggregateTemplate.upsert(new ApplicationDependencies(
                ApplicationDependenciesId.of(ApplicationId.of("com.axelixlabs.test", "broken")),
                "{ \"bomFormat\": \"CycloneDX\"",
                Instant.now()));
        subject.store(
                APPLICATION_ID,
                CycloneDxDocuments.of("com.axelixlabs.test:app:1.0.0", "org.hdrhistogram:HdrHistogram:2.2.2"));

        // when.
        DependenciesDashboardResponse dashboard = subject.getDashboard();

        // then. the dashboard is built from the remaining SBOMs only.
        assertThat(dashboard.totalServices()).isEqualTo(1);
        assertThat(dashboard.servicesWithRiskyDependencies()).isEqualTo(1);
        assertThat(dashboard.trackedProjects()).singleElement().satisfies(project -> {
            assertThat(project.serviceCount()).isEqualTo(1);
            assertThat(project.share()).isEqualTo(100.0);
        });
    }
}
