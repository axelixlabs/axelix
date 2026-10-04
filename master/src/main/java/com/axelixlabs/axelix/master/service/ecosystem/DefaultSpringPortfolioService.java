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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.axelixlabs.axelix.master.api.external.response.dashboard.SpringPortfolioResponse;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.Platform;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformName;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformReleaseLine;
import com.axelixlabs.axelix.master.repository.HistoricalApplicationSnapshotRepository;
import com.axelixlabs.axelix.master.repository.HistoricalApplicationSnapshotRepository.ApplicationPlatformVersions;
import com.axelixlabs.axelix.master.service.ecosystem.platform.PlatformCatalog;

import static com.axelixlabs.axelix.master.api.external.response.dashboard.SpringPortfolioResponse.MaintenanceWindowEntry;
import static com.axelixlabs.axelix.master.api.external.response.dashboard.SpringPortfolioResponse.PlatformDistribution;
import static com.axelixlabs.axelix.master.api.external.response.dashboard.SpringPortfolioResponse.PlatformLineUsage;
import static com.axelixlabs.axelix.master.api.external.response.dashboard.SpringPortfolioResponse.PlatformMajorGroup;

/**
 * Default implementation of {@link SpringPortfolioService}.
 *
 * @author Nikita Kirillov
 */
public class DefaultSpringPortfolioService implements SpringPortfolioService {

    private final HistoricalApplicationSnapshotRepository snapshotRepository;
    private final PlatformCatalog platformCatalog;

    public DefaultSpringPortfolioService(
            HistoricalApplicationSnapshotRepository snapshotRepository, PlatformCatalog platformCatalog) {
        this.snapshotRepository = snapshotRepository;
        this.platformCatalog = platformCatalog;
    }

    @Override
    public SpringPortfolioResponse getSpringPortfolio() {
        List<ApplicationPlatformVersions> applications = snapshotRepository.findLatestPlatformVersionsPerService();

        LocalDate today = LocalDate.now();
        Platform springBoot = platformCatalog.find(PlatformName.SPRING_BOOT);
        Platform springFramework = platformCatalog.find(PlatformName.SPRING_FRAMEWORK);

        int applicationsTotal = applications.size();

        int fullyOssSupported = (int) applications.stream()
                .filter(app -> isFullyOssSupported(app, springBoot, springFramework, today))
                .count();

        List<PlatformReleaseLine> bootLines = applications.stream()
                .map(app -> springBoot.lineOf(app.springBootVersion()))
                .flatMap(Optional::stream)
                .toList();

        List<PlatformReleaseLine> frameworkLines = applications.stream()
                .map(app -> springFramework.lineOf(app.springFrameworkVersion()))
                .flatMap(Optional::stream)
                .toList();

        PlatformDistribution springBootPlatformDistribution =
                distribution(PlatformName.SPRING_BOOT, bootLines, applicationsTotal, today);

        PlatformDistribution springFrameworkPlatformDistribution =
                distribution(PlatformName.SPRING_FRAMEWORK, frameworkLines, applicationsTotal, today);

        List<MaintenanceWindowEntry> linesInUse = Stream.concat(
                        linesInUse(PlatformName.SPRING_BOOT, bootLines, today).stream(),
                        linesInUse(PlatformName.SPRING_FRAMEWORK, frameworkLines, today).stream())
                .toList();

        return new SpringPortfolioResponse(
                applicationsTotal,
                fullyOssSupported,
                springBootPlatformDistribution,
                springFrameworkPlatformDistribution,
                linesInUse);
    }

    private boolean isFullyOssSupported(
            ApplicationPlatformVersions app, Platform springBoot, Platform springFramework, LocalDate today) {

        Optional<PlatformReleaseLine> bootLine = springBoot.lineOf(app.springBootVersion());
        Optional<PlatformReleaseLine> frameworkLine = springFramework.lineOf(app.springFrameworkVersion());

        return bootLine.map(line -> line.ossSupportedAt(today)).orElse(false)
                && frameworkLine.map(line -> line.ossSupportedAt(today)).orElse(false);
    }

    private PlatformDistribution distribution(
            PlatformName platformName,
            List<PlatformReleaseLine> resolvedLines,
            int applicationsTotal,
            LocalDate today) {

        Map<PlatformReleaseLine, Long> applicationsPerLine = countByLine(resolvedLines);

        int applicationsOnOssSupportedLine = applicationsPerLine.entrySet().stream()
                .filter(entry -> entry.getKey().ossSupportedAt(today))
                .mapToInt(entry -> entry.getValue().intValue())
                .sum();

        List<PlatformMajorGroup> majors = applicationsPerLine.entrySet().stream()
                .collect(Collectors.groupingBy(entry -> majorOf(entry.getKey())))
                .entrySet()
                .stream()
                .map(byMajor -> toMajorGroup(byMajor.getKey(), byMajor.getValue(), applicationsTotal, today))
                .sorted(Comparator.comparingInt((PlatformMajorGroup group) -> Integer.parseInt(group.major()))
                        .reversed())
                .toList();

        return new PlatformDistribution(platformName, applicationsOnOssSupportedLine, applicationsTotal, majors);
    }

    private List<MaintenanceWindowEntry> linesInUse(
            PlatformName platformName, List<PlatformReleaseLine> resolvedLines, LocalDate today) {
        return countByLine(resolvedLines).entrySet().stream()
                .map(entry -> new MaintenanceWindowEntry(
                        platformName,
                        entry.getKey().line(),
                        entry.getKey().releasedAt(),
                        entry.getKey().ossSupportEndsAt(),
                        entry.getKey().ossSupportedAt(today),
                        entry.getValue().intValue()))
                .sorted(Comparator.comparing(MaintenanceWindowEntry::releasedAt))
                .toList();
    }

    private static Map<PlatformReleaseLine, Long> countByLine(List<PlatformReleaseLine> resolvedLines) {
        return resolvedLines.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private PlatformMajorGroup toMajorGroup(
            String major, List<Map.Entry<PlatformReleaseLine, Long>> lines, int applicationsTotal, LocalDate today) {

        List<PlatformLineUsage> lineUsages = lines.stream()
                .map(entry -> new PlatformLineUsage(
                        entry.getKey().line(),
                        entry.getValue().intValue(),
                        percentageOf(entry.getValue(), applicationsTotal),
                        entry.getKey().ossSupportedAt(today)))
                .sorted(Comparator.comparing(PlatformLineUsage::line).reversed())
                .toList();

        long applicationsOnMajor = lines.stream().mapToLong(Map.Entry::getValue).sum();

        return new PlatformMajorGroup(
                major, applicationsOnMajor, percentageOf(applicationsOnMajor, applicationsTotal), lineUsages);
    }

    /**
     * The major generation a release line belongs to, e.g. {@code "3"} for the line {@code 3.5.x}.
     */
    private static String majorOf(PlatformReleaseLine platformReleaseLine) {
        return platformReleaseLine
                .line()
                .substring(0, platformReleaseLine.line().indexOf('.'));
    }

    private static int percentageOf(long count, int total) {
        return total == 0 ? 0 : (int) Math.round((count * 100.0) / total);
    }
}
