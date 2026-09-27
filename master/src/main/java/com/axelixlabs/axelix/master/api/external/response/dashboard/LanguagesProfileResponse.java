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
package com.axelixlabs.axelix.master.api.external.response.dashboard;

import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * The fleet-wide languages profile: which JVM languages, Java releases and JDK builds are in use across the estate.
 *
 * <p>Everything is reported per <strong>application</strong> (an application groups all the running instances that
 * share the same group and artifact id). Java is always present, so a Java release and a JDK vendor is reported for
 * every application. The total number of applications considered is the sum of {@code applicationsOnLts} and
 * {@code applicationsOnNonLts}; Kotlin is present exactly when {@code kotlinReleases} is non-empty.
 *
 * @param applicationsOnLts    the number of applications whose Java release is an LTS release
 * @param applicationsOnNonLts the number of applications whose Java release is not an LTS release
 * @param javaReleases         the Java release distribution, per application, newest release first
 * @param jdkVendors           the JDK vendor distribution, per application, most used first
 * @param languageMix          how applications split between Java-only and Java-and-Kotlin; {@code null} when no
 *                             Kotlin is detected
 * @param kotlinReleases       the Kotlin release distribution, per application, newest release first; empty when no
 *                             Kotlin is detected
 * @param lastScannedAt        the date of the most recent snapshot any application reported, or {@code null} if unknown
 *
 * @author Mikhail Polivakha
 */
public record LanguagesProfileResponse(
        int applicationsOnLts,
        int applicationsOnNonLts,
        List<JavaReleaseUsage> javaReleases,
        List<JdkVendorUsage> jdkVendors,
        @Nullable LanguageMix languageMix,
        List<KotlinReleaseUsage> kotlinReleases,
        @Nullable LocalDate lastScannedAt) {

    /**
     * How many applications run a given Java release.
     *
     * @param release               the Java release, e.g. {@code 25}
     * @param applicationCount      the number of applications on this release
     * @param applicationPercentage the share of the fleet's applications on this release
     * @param lts                   whether the release is a long-term-support release
     */
    public record JavaReleaseUsage(int release, int applicationCount, int applicationPercentage, boolean lts) {}

    /**
     * How many applications run a given JDK vendor's build.
     *
     * @param vendor                the JDK vendor, e.g. {@code Eclipse Temurin}
     * @param applicationCount      the number of applications on this vendor's build
     * @param applicationPercentage the share of the fleet's applications on this vendor's build
     */
    public record JdkVendorUsage(String vendor, int applicationCount, int applicationPercentage) {}

    /**
     * How many applications run a given Kotlin release.
     *
     * @param release               the Kotlin release, e.g. {@code 2.2}
     * @param applicationCount      the number of applications on this release
     * @param applicationPercentage the share of the Kotlin-using applications on this release
     */
    public record KotlinReleaseUsage(String release, int applicationCount, int applicationPercentage) {}

    /**
     * How the fleet's applications split between Java-only and Java-and-Kotlin.
     *
     * @param javaOnlyApplications  the number of applications that contain no Kotlin code
     * @param javaOnlyPercentage    the share of the fleet's applications that contain no Kotlin code
     * @param kotlinApplications    the number of applications that contain Kotlin code
     * @param kotlinPercentage      the share of the fleet's applications that contain Kotlin code
     */
    public record LanguageMix(
            int javaOnlyApplications, int javaOnlyPercentage, int kotlinApplications, int kotlinPercentage) {}
}
