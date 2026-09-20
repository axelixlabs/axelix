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
import java.util.Map;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.master.api.external.response.dashboard.SpringPortfolioResponse;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.Insights;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.Platform;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformName;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformReleaseLine;
import com.axelixlabs.axelix.master.repository.HistoricalApplicationSnapshotRepository;
import com.axelixlabs.axelix.master.service.ecosystem.platform.PlatformCatalog;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

import static com.axelixlabs.axelix.master.api.external.response.dashboard.SpringPortfolioResponse.MaintenanceWindowEntry;
import static com.axelixlabs.axelix.master.api.external.response.dashboard.SpringPortfolioResponse.PlatformDistribution;
import static com.axelixlabs.axelix.master.api.external.response.dashboard.SpringPortfolioResponse.PlatformLineUsage;
import static com.axelixlabs.axelix.master.api.external.response.dashboard.SpringPortfolioResponse.PlatformMajorGroup;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultSpringPortfolioService}.
 *
 * @author Nikita Kirillov
 */
@DatabaseMatrixTest
class DefaultSpringPortfolioServiceTest {

    private static final String GROUP_ID = "com.axelixlabs.test";

    private static final LocalDate TODAY = LocalDate.now(ZoneOffset.UTC);

    private static final LocalDate FAR_PAST = LocalDate.of(2000, 1, 1);
    private static final LocalDate FAR_FUTURE = LocalDate.of(2099, 1, 1);

    private static final PlatformReleaseLine BOOT_2_7 =
            new PlatformReleaseLine("2.7.x", LocalDate.of(2020, 1, 1), FAR_PAST, null);
    private static final PlatformReleaseLine BOOT_3_4 =
            new PlatformReleaseLine("3.4.x", LocalDate.of(2021, 1, 1), FAR_PAST, null);
    private static final PlatformReleaseLine BOOT_3_5 =
            new PlatformReleaseLine("3.5.x", LocalDate.of(2022, 1, 1), FAR_FUTURE, null);

    private static final PlatformReleaseLine FRAMEWORK_5_3 =
            new PlatformReleaseLine("5.3.x", LocalDate.of(2019, 1, 1), FAR_PAST, null);
    private static final PlatformReleaseLine FRAMEWORK_6_1 =
            new PlatformReleaseLine("6.1.x", LocalDate.of(2020, 6, 1), FAR_PAST, null);
    private static final PlatformReleaseLine FRAMEWORK_6_2 =
            new PlatformReleaseLine("6.2.x", LocalDate.of(2021, 6, 1), FAR_FUTURE, null);

    private static final Map<PlatformName, Platform> PLATFORMS = Map.of(
            PlatformName.SPRING_BOOT, new Platform(PlatformName.SPRING_BOOT, List.of(BOOT_2_7, BOOT_3_4, BOOT_3_5)),
            PlatformName.SPRING_FRAMEWORK,
                    new Platform(PlatformName.SPRING_FRAMEWORK, List.of(FRAMEWORK_5_3, FRAMEWORK_6_1, FRAMEWORK_6_2)));

    private static final PlatformCatalog PLATFORM_CATALOG = PLATFORMS::get;

    @Autowired
    private HistoricalApplicationSnapshotRepository snapshotRepository;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    private DefaultSpringPortfolioService subject;

    @BeforeEach
    void setUp() {
        jdbcAggregateTemplate.deleteAll(HistoricalApplicationSnapshot.class);

        subject = new DefaultSpringPortfolioService(snapshotRepository, PLATFORM_CATALOG);
    }

    @Test
    void aggregatesTheFleetAcrossApplicationsAndReleaseLines() {
        // given.
        jdbcAggregateTemplate.insertAll(List.of(
                snapshot("app-a", TODAY, "3.5.2", "6.2.1"),
                snapshot("app-b", TODAY, "3.4.1", "6.1.0"),
                snapshot("app-c", TODAY, "2.7.0", "5.3.0"),
                snapshot("app-d", TODAY, "1.0.0", "6.2.5"),
                snapshot("app-e", TODAY, "3.5.0", "6.2.0")));

        // when.
        SpringPortfolioResponse response = subject.getSpringPortfolio();

        // then.
        assertThat(response.applicationsTotal()).isEqualTo(5);
        assertThat(response.applicationsFullyOssSupported()).isEqualTo(2);

        PlatformDistribution boot = response.springBoot();
        assertThat(boot.platform()).isEqualTo(PlatformName.SPRING_BOOT);
        assertThat(boot.applicationsTotal()).isEqualTo(5);
        assertThat(boot.applicationsOnOssSupportedLine()).isEqualTo(2);
        assertThat(boot.majors()).extracting(PlatformMajorGroup::major).containsExactly("3", "2");

        PlatformMajorGroup bootMajor3 = boot.majors().get(0);
        assertThat(bootMajor3.applicationPercentage()).isEqualTo(60);
        assertThat(bootMajor3.lines())
                .extracting(
                        PlatformLineUsage::line, PlatformLineUsage::applicationCount, PlatformLineUsage::ossSupported)
                .containsExactly(Tuple.tuple("3.5.x", 2, true), Tuple.tuple("3.4.x", 1, false));

        PlatformMajorGroup bootMajor2 = boot.majors().get(1);
        assertThat(bootMajor2.applicationPercentage()).isEqualTo(20);
        assertThat(bootMajor2.lines())
                .extracting(
                        PlatformLineUsage::line, PlatformLineUsage::applicationCount, PlatformLineUsage::ossSupported)
                .containsExactly(Tuple.tuple("2.7.x", 1, false));

        PlatformDistribution framework = response.springFramework();
        assertThat(framework.platform()).isEqualTo(PlatformName.SPRING_FRAMEWORK);
        assertThat(framework.applicationsTotal()).isEqualTo(5);
        assertThat(framework.applicationsOnOssSupportedLine()).isEqualTo(3);
        assertThat(framework.majors()).extracting(PlatformMajorGroup::major).containsExactly("6", "5");

        PlatformMajorGroup frameworkMajor6 = framework.majors().get(0);
        assertThat(frameworkMajor6.applicationPercentage()).isEqualTo(80);
        assertThat(frameworkMajor6.lines())
                .extracting(
                        PlatformLineUsage::line, PlatformLineUsage::applicationCount, PlatformLineUsage::ossSupported)
                .containsExactly(Tuple.tuple("6.2.x", 3, true), Tuple.tuple("6.1.x", 1, false));

        PlatformMajorGroup frameworkMajor5 = framework.majors().get(1);
        assertThat(frameworkMajor5.applicationPercentage()).isEqualTo(20);
        assertThat(frameworkMajor5.lines())
                .extracting(
                        PlatformLineUsage::line, PlatformLineUsage::applicationCount, PlatformLineUsage::ossSupported)
                .containsExactly(Tuple.tuple("5.3.x", 1, false));

        assertThat(response.linesInUse())
                .extracting(
                        MaintenanceWindowEntry::platform,
                        MaintenanceWindowEntry::line,
                        MaintenanceWindowEntry::applicationCount)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(PlatformName.SPRING_BOOT, "2.7.x", 1),
                        Tuple.tuple(PlatformName.SPRING_BOOT, "3.4.x", 1),
                        Tuple.tuple(PlatformName.SPRING_BOOT, "3.5.x", 2),
                        Tuple.tuple(PlatformName.SPRING_FRAMEWORK, "5.3.x", 1),
                        Tuple.tuple(PlatformName.SPRING_FRAMEWORK, "6.1.x", 1),
                        Tuple.tuple(PlatformName.SPRING_FRAMEWORK, "6.2.x", 3));
    }

    @Test
    void countsEachServiceOnceUsingOnlyItsLatestSnapshot() {
        // given a stale snapshot on old lines superseded by today's snapshot on current lines.
        jdbcAggregateTemplate.insertAll(List.of(
                snapshot("app-a", TODAY.minusDays(2), "2.7.0", "5.3.0"), snapshot("app-a", TODAY, "3.5.2", "6.2.1")));

        // when.
        SpringPortfolioResponse response = subject.getSpringPortfolio();

        // then only the latest snapshot is counted, exactly once.
        assertThat(response.applicationsTotal()).isEqualTo(1);
        assertThat(response.springBoot().majors())
                .flatExtracting(PlatformMajorGroup::lines)
                .extracting(PlatformLineUsage::line, PlatformLineUsage::applicationCount)
                .containsExactly(Tuple.tuple("3.5.x", 1));
        assertThat(response.springFramework().majors())
                .flatExtracting(PlatformMajorGroup::lines)
                .extracting(PlatformLineUsage::line, PlatformLineUsage::applicationCount)
                .containsExactly(Tuple.tuple("6.2.x", 1));
    }

    private static HistoricalApplicationSnapshot snapshot(
            String artifactId, LocalDate date, String springBootVersion, String springFrameworkVersion) {
        return new HistoricalApplicationSnapshot(
                new SnapshotId(GROUP_ID, artifactId, date),
                new Insights(
                        new Insights.HotSpot(
                                new Insights.HotSpot.ProjectLeyden(false, false),
                                new Insights.HotSpot.GarbageCollector(false, GarbageCollector.G1),
                                new Insights.HotSpot.ProjectLilliput(false)),
                        new Insights.SpringFramework(false, springBootVersion, springFrameworkVersion),
                        new PersistenceInsights(List.of())));
    }
}
