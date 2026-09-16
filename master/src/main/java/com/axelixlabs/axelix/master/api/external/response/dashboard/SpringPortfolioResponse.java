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

import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformName;

/**
 * The fleet-wide Spring Boot / Spring Framework version portfolio: how applications are spread across release
 * lines, and which of those lines are still OSS supported.
 *
 * @param applicationsTotal              the number of distinct applications considered
 * @param applicationsFullyOssSupported  the number of applications whose Spring Boot line and Spring Framework line
 *                                       are both still OSS supported
 * @param springBoot                     the Spring Boot distribution
 * @param springFramework                the Spring Framework distribution
 * @param linesInUse                     every release line (of either platform) currently in use, for the
 *                                       maintenance window timeline
 *
 * @author Nikita Kirillov
 */
public record SpringPortfolioResponse(
        int applicationsTotal,
        int applicationsFullyOssSupported,
        PlatformDistribution springBoot,
        PlatformDistribution springFramework,
        List<MaintenanceWindowEntry> linesInUse) {

    /**
     * How the fleet's applications are spread across a platform's release lines.
     *
     * @param platform                       the platform this distribution is about
     * @param applicationsOnOssSupportedLine the number of applications currently on a release line that is still
     *                                       OSS supported
     * @param applicationsTotal              the total number of applications considered
     * @param majors                         the major generations in use, newest first; an application whose
     *                                       version does not resolve to any curated release line is not
     *                                       represented here
     */
    public record PlatformDistribution(
            PlatformName platform,
            int applicationsOnOssSupportedLine,
            int applicationsTotal,
            List<PlatformMajorGroup> majors) {}

    /**
     * One major generation of a platform , and the release lines within it that the fleet actually runs.
     *
     * @param major                  the major generation
     * @param applicationPercentage  the share of the fleet's applications on this major generation
     * @param lines                  the release lines within this major generation that are in use, newest first
     */
    public record PlatformMajorGroup(String major, int applicationPercentage, List<PlatformLineUsage> lines) {}

    /**
     * How many applications in the fleet run a given release line, and whether that line is still OSS supported.
     *
     * @param line                   the release line, e.g. {@code 3.5.x}
     * @param applicationCount       the number of applications currently on this line
     * @param applicationPercentage  the share of the fleet's applications currently on this line
     * @param ossSupported           whether the line is still within its OSS maintenance window
     */
    public record PlatformLineUsage(
            String line, int applicationCount, int applicationPercentage, boolean ossSupported) {}

    /**
     * One release line currently in use across the fleet, with its OSS maintenance window.
     *
     * @param platform         the platform the line belongs to
     * @param line             the release line, e.g. {@code 3.5.x}
     * @param releasedAt       the date the line was first released
     * @param ossSupportEndsAt the date OSS maintenance of the line ended, or ends
     * @param ossSupported     whether the line is still within its OSS maintenance window
     * @param applicationCount the number of applications currently on this line
     */
    public record MaintenanceWindowEntry(
            PlatformName platform,
            String line,
            LocalDate releasedAt,
            LocalDate ossSupportEndsAt,
            boolean ossSupported,
            int applicationCount) {}
}
