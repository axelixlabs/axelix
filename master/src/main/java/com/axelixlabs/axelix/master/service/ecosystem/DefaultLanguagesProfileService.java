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
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.axelixlabs.axelix.common.utils.SemanticVersion;
import com.axelixlabs.axelix.master.api.external.response.dashboard.LanguagesProfileResponse;
import com.axelixlabs.axelix.master.domain.JdkVendor;
import com.axelixlabs.axelix.master.repository.HistoricalApplicationSnapshotRepository;
import com.axelixlabs.axelix.master.repository.HistoricalApplicationSnapshotRepository.ApplicationLanguages;

import static com.axelixlabs.axelix.master.api.external.response.dashboard.LanguagesProfileResponse.JavaReleaseUsage;
import static com.axelixlabs.axelix.master.api.external.response.dashboard.LanguagesProfileResponse.JdkVendorUsage;
import static com.axelixlabs.axelix.master.api.external.response.dashboard.LanguagesProfileResponse.KotlinReleaseUsage;
import static com.axelixlabs.axelix.master.api.external.response.dashboard.LanguagesProfileResponse.LanguageMix;

/**
 * Default implementation of {@link LanguagesProfileService}.
 *
 * @author Mikhail Polivakha
 */
public class DefaultLanguagesProfileService implements LanguagesProfileService {

    private final HistoricalApplicationSnapshotRepository snapshotRepository;

    public DefaultLanguagesProfileService(HistoricalApplicationSnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    @Override
    public LanguagesProfileResponse getLanguagesProfile() {
        List<ApplicationLanguages> applications = snapshotRepository.findLatestLanguagesPerService();

        int applicationsTotal = applications.size();

        List<JavaReleaseUsage> javaReleases = buildJavaReleaseUsage(applications);

        int applicationsOnLts = javaReleases.stream()
                .filter(JavaReleaseUsage::lts)
                .mapToInt(JavaReleaseUsage::applicationCount)
                .sum();

        List<JdkVendorUsage> jdkVendors = buildJdkVendorsUsage(applications, applicationsTotal);

        // Kotlin adoption, per application. An application uses Kotlin when it reports a Kotlin version.
        List<String> kotlinReleasesPerApplication = applications.stream()
                .map(ApplicationLanguages::kotlinVersion)
                .filter(Objects::nonNull)
                .map(kotlinVersion -> SemanticVersion.tryParse(kotlinVersion)
                        .map(SemanticVersion::majorMinor)
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        int kotlinApplicationsCount = kotlinReleasesPerApplication.size();

        LanguageMix languageMix = null;
        List<KotlinReleaseUsage> kotlinReleases = List.of();

        if (kotlinApplicationsCount > 0) {
            int javaOnlyApplications = applicationsTotal - kotlinApplicationsCount;

            languageMix = new LanguageMix(
                    javaOnlyApplications,
                    percentageOf(javaOnlyApplications, applicationsTotal),
                    kotlinApplicationsCount,
                    percentageOf(kotlinApplicationsCount, applicationsTotal));

            kotlinReleases = buildKotlinReleaseUsage(kotlinReleasesPerApplication, kotlinApplicationsCount);
        }

        LocalDate lastScannedAt = LocalDate.now();

        return new LanguagesProfileResponse(
                applicationsOnLts,
                applicationsTotal - applicationsOnLts,
                javaReleases,
                jdkVendors,
                languageMix,
                kotlinReleases,
                lastScannedAt);
    }

    private static List<KotlinReleaseUsage> buildKotlinReleaseUsage(
            List<String> kotlinReleasesPerApplication, int kotlinApplicationsCount) {

        Map<String, Long> applicationsPerKotlinRelease = kotlinReleasesPerApplication.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<KotlinReleaseUsage> kotlinReleases = applicationsPerKotlinRelease.entrySet().stream()
                .map(entry -> new KotlinReleaseUsage(
                        entry.getKey(),
                        entry.getValue().intValue(),
                        percentageOf(entry.getValue(), kotlinApplicationsCount)))
                .sorted(Comparator.comparing(KotlinReleaseUsage::release).reversed())
                .toList();

        return kotlinReleases;
    }

    private static List<JdkVendorUsage> buildJdkVendorsUsage(
            List<ApplicationLanguages> applications, int applicationsTotal) {

        Map<JdkVendor, Long> applicationsPerJdkVendor = applications.stream()
                .map(ApplicationLanguages::jdkVendor)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<JdkVendorUsage> jdkVendors = applicationsPerJdkVendor.entrySet().stream()
                .map(entry -> new JdkVendorUsage(
                        entry.getKey().getDisplayName(),
                        entry.getValue().intValue(),
                        percentageOf(entry.getValue(), applicationsTotal)))
                .sorted(Comparator.comparingInt(JdkVendorUsage::applicationCount)
                        .reversed())
                .toList();

        return jdkVendors;
    }

    private static List<JavaReleaseUsage> buildJavaReleaseUsage(List<ApplicationLanguages> applications) {

        Map<Integer, Long> applicationsPerJavaRelease = applications.stream()
                .map(ApplicationLanguages::javaVersion)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<JavaReleaseUsage> javaReleases = applicationsPerJavaRelease.entrySet().stream()
                .map(entry -> new JavaReleaseUsage(
                        entry.getKey(),
                        entry.getValue().intValue(),
                        percentageOf(entry.getValue(), applications.size()),
                        isLtsRelease(entry.getKey())))
                .sorted(Comparator.comparingInt(JavaReleaseUsage::release).reversed())
                .toList();

        return javaReleases;
    }

    /**
     * Whether the given Java release is a long-term-support release. Java 8, 11 and 17 are LTS; from Java 21 onwards
     * LTS lands every four releases (21, 25, 29, ...).
     */
    private static boolean isLtsRelease(int featureJavaVersion) {
        if (featureJavaVersion == 8 || featureJavaVersion == 11 || featureJavaVersion == 17) {
            return true;
        }
        return featureJavaVersion >= 21 && (featureJavaVersion - 21) % 4 == 0;
    }

    private static int percentageOf(long count, int total) {
        return total == 0 ? 0 : (int) Math.round((count * 100.0) / total);
    }
}
