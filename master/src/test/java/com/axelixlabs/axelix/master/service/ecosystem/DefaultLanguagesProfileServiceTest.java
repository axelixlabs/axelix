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

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.master.api.external.response.dashboard.LanguagesProfileResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.LanguagesProfileResponse.JavaReleaseUsage;
import com.axelixlabs.axelix.master.api.external.response.dashboard.LanguagesProfileResponse.JdkVendorUsage;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.Insights;
import com.axelixlabs.axelix.master.domain.JavaVersion;
import com.axelixlabs.axelix.master.domain.JdkVendor;
import com.axelixlabs.axelix.master.repository.HistoricalApplicationSnapshotRepository;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultLanguagesProfileService}.
 *
 * @author Mikhail Polivakha
 */
@DatabaseMatrixTest
class DefaultLanguagesProfileServiceTest {

    private static final String GROUP_ID = "com.axelixlabs.test";

    private static final LocalDate TODAY = LocalDate.now(ZoneOffset.UTC);

    @Autowired
    private HistoricalApplicationSnapshotRepository snapshotRepository;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    private DefaultLanguagesProfileService subject;

    @BeforeEach
    void setUp() {
        jdbcAggregateTemplate.deleteAll(HistoricalApplicationSnapshot.class);

        subject = new DefaultLanguagesProfileService(snapshotRepository);
    }

    @Test
    void shouldAggregateJavaReleasesJdkVendorsAndKotlinAcrossApplications() {
        // given three applications: A (Java 21, Temurin, no Kotlin), B (Java 25, Corretto, Kotlin 2.2) and
        // C (Java 17, Temurin, Kotlin 2.2).
        jdbcAggregateTemplate.insertAll(List.of(
                snapshot("a", TODAY, 21, JdkVendor.ADOPTIUM, null),
                snapshot("b", TODAY, 25, JdkVendor.AMAZON_CORRETTO, "2.2.0"),
                snapshot("c", TODAY, 17, JdkVendor.ADOPTIUM, "2.2.10")));

        // when.
        LanguagesProfileResponse profile = subject.getLanguagesProfile();

        // then.
        assertThat(profile.applicationsOnLts()).isEqualTo(3);
        assertThat(profile.applicationsOnNonLts()).isZero();

        // Java releases, per application, newest first, all LTS.
        assertThat(profile.javaReleases()).extracting(JavaReleaseUsage::release).containsExactly(25, 21, 17);
        assertThat(profile.javaReleases()).allMatch(JavaReleaseUsage::lts);
        assertThat(profile.javaReleases()).allMatch(release -> release.applicationCount() == 1);

        // JDK vendors, per application, most used first, reported with their display name.
        JdkVendorUsage temurin = profile.jdkVendors().get(0);
        assertThat(temurin.vendor()).isEqualTo(JdkVendor.ADOPTIUM.getDisplayName());
        assertThat(temurin.applicationCount()).isEqualTo(2);
        assertThat(temurin.applicationPercentage()).isEqualTo(67);

        JdkVendorUsage corretto = profile.jdkVendors().get(1);
        assertThat(corretto.vendor()).isEqualTo(JdkVendor.AMAZON_CORRETTO.getDisplayName());
        assertThat(corretto.applicationCount()).isEqualTo(1);
        assertThat(corretto.applicationPercentage()).isEqualTo(33);

        // Kotlin: two of three applications contain Kotlin, both on 2.2.
        assertThat(profile.languageMix()).isNotNull();
        assertThat(profile.languageMix().kotlinApplications()).isEqualTo(2);
        assertThat(profile.languageMix().javaOnlyApplications()).isEqualTo(1);
        assertThat(profile.languageMix().kotlinPercentage()).isEqualTo(67);
        assertThat(profile.kotlinReleases()).hasSize(1);
        assertThat(profile.kotlinReleases().getFirst().release()).isEqualTo("2.2");
        assertThat(profile.kotlinReleases().getFirst().applicationCount()).isEqualTo(2);
        assertThat(profile.kotlinReleases().getFirst().applicationPercentage()).isEqualTo(100);

        assertThat(profile.lastScannedAt()).isEqualTo(TODAY);
    }

    @Test
    void shouldReportNoKotlinWhenNoApplicationContainsKotlinCode() {
        // given only Java applications, one of them on a non-LTS release.
        jdbcAggregateTemplate.insertAll(List.of(
                snapshot("a", TODAY, 21, JdkVendor.ADOPTIUM, null),
                snapshot("b", TODAY, 24, JdkVendor.ADOPTIUM, null)));

        // when.
        LanguagesProfileResponse profile = subject.getLanguagesProfile();

        // then.
        assertThat(profile.languageMix()).isNull();
        assertThat(profile.kotlinReleases()).isEmpty();
        assertThat(profile.applicationsOnLts()).isEqualTo(1);
        assertThat(profile.applicationsOnNonLts()).isEqualTo(1);
        assertThat(profile.javaReleases())
                .filteredOn(release -> release.release() == 24)
                .singleElement()
                .matches(release -> !release.lts());
    }

    @Test
    void shouldCountEachApplicationOnceUsingOnlyItsLatestSnapshot() {
        // given a stale snapshot superseded by today's snapshot for the same application.
        jdbcAggregateTemplate.insertAll(List.of(
                snapshot("a", TODAY.minusDays(2), 17, JdkVendor.ADOPTIUM, null),
                snapshot("a", TODAY, 25, JdkVendor.AMAZON_CORRETTO, null)));

        // when.
        LanguagesProfileResponse profile = subject.getLanguagesProfile();

        // then only the latest snapshot is counted, exactly once.
        assertThat(profile.applicationsOnLts() + profile.applicationsOnNonLts()).isEqualTo(1);
        assertThat(profile.javaReleases()).extracting(JavaReleaseUsage::release).containsExactly(25);
        assertThat(profile.jdkVendors())
                .extracting(JdkVendorUsage::vendor)
                .containsExactly(JdkVendor.AMAZON_CORRETTO.getDisplayName());
        assertThat(profile.lastScannedAt()).isEqualTo(TODAY);
    }

    @Test
    void shouldReturnEmptyProfileWhenNoSnapshotsRecorded() {
        // when.
        LanguagesProfileResponse profile = subject.getLanguagesProfile();

        // then.
        assertThat(profile.applicationsOnLts()).isZero();
        assertThat(profile.applicationsOnNonLts()).isZero();
        assertThat(profile.javaReleases()).isEmpty();
        assertThat(profile.jdkVendors()).isEmpty();
        assertThat(profile.kotlinReleases()).isEmpty();
        assertThat(profile.lastScannedAt()).isNotNull();
    }

    private static HistoricalApplicationSnapshot snapshot(
            String artifactId, LocalDate date, int javaVersion, JdkVendor jdkVendor, @Nullable String kotlinVersion) {
        return new HistoricalApplicationSnapshot(
                new SnapshotId(GROUP_ID, artifactId, date),
                new Insights(
                        new Insights.HotSpot(
                                new Insights.HotSpot.ProjectLeyden(false, false),
                                new Insights.HotSpot.GarbageCollector(false, GarbageCollector.G1),
                                new Insights.HotSpot.ProjectLilliput(false)),
                        new Insights.SpringFramework(false),
                        new PersistenceInsights(List.of())),
                new HistoricalApplicationSnapshot.Versions(
                        "1.0.0", "3.5.2", "6.2.1", new JavaVersion(javaVersion, 0, 0, 0), jdkVendor, kotlinVersion));
    }
}
