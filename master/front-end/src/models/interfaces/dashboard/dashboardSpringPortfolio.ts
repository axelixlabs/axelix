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

/**
 * Mirrors the {@code PlatformName} enum on the Master backend. Jackson serializes
 * the enum by its constant name, so the wire values are the upper-cased constants.
 */
export type IPlatformName = "SPRING_BOOT" | "SPRING_FRAMEWORK";

/**
 * A single Spring release line (e.g. {@code 3.5.x}) and how many applications run it.
 */
export interface IPlatformLineUsage {
    line: string;
    applicationCount: number;
    applicationPercentage: number;
    ossSupported: boolean;
}

/**
 * A major generation (e.g. {@code "3"}) that groups the release lines within it.
 */
export interface IPlatformMajorGroup {
    major: string;
    applicationCount: number;
    applicationPercentage: number;
    lines: IPlatformLineUsage[];
}

/**
 * The fleet-wide distribution of a single platform (Spring Boot or Spring Framework)
 * across its major generations and release lines.
 */
export interface IPlatformDistribution {
    platform: IPlatformName;
    applicationsOnOssSupportedLine: number;
    applicationsTotal: number;
    majors: IPlatformMajorGroup[];
}

/**
 * One row of the OSS maintenance ladder: a release line placed on the published
 * open-source maintenance schedule. Dates are ISO {@code YYYY-MM-DD} strings.
 */
export interface IMaintenanceWindowEntry {
    platform: IPlatformName;
    line: string;
    releasedAt: string;
    ossSupportEndsAt: string;
    ossSupported: boolean;
    applicationCount: number;
}

/**
 * Response body of {@code GET /api/external/dashboard/spring-portfolio}.
 */
export interface IDashboardSpringPortfolioResponseBody {
    applicationsTotal: number;
    applicationsFullyOssSupported: number;
    springBoot: IPlatformDistribution;
    springFramework: IPlatformDistribution;
    linesInUse: IMaintenanceWindowEntry[];
}
