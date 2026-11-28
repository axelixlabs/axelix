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
import {
    EFrameworkSupportStatus,
    EPlatformName,
    type IPlatformDistribution,
    type ISpringPortfolioApplication,
    type ISpringSlice,
} from "@/models";
import { PLATFORM_ORDER } from "@/utils";

export const LINE_COLORS = [
    "#2EC4B6",
    "#60A5FA",
    "#A78BFA",
    "#F5A623",
    "#F472B6",
    "#34D399",
    "#FB923C",
    "#818CF8",
    "#E879F9",
    "#22D3EE",
];

export const buildLineColorMap = (distribution: IPlatformDistribution): Map<string, string> => {
    const colors = new Map<string, string>();
    let index = 0;

    for (const major of distribution.majors) {
        for (const line of major.lines) {
            colors.set(line.line, LINE_COLORS[index % LINE_COLORS.length]);
            index += 1;
        }
    }

    return colors;
};

export const parseIsoDate = (value: string | number[]): Date => {
    if (Array.isArray(value)) {
        const [year, month, day] = value;
        return new Date(year, month - 1, day);
    }

    return new Date(`${value}T00:00:00`);
};

export const formatMonthYear = (value: string | number[]): string => {
    return parseIsoDate(value).toLocaleDateString("en-US", { month: "short", year: "numeric" });
};

// TODO: Revisit once the applications come from the API: the version format, an empty list, and Spring Framework versions overridden by the application
const compareVersions = (left: string, right: string): number =>
    left.localeCompare(right, undefined, { numeric: true });

export const springSliceVersionOf = (application: ISpringPortfolioApplication, platform: EPlatformName): string => {
    const line = platform === EPlatformName.SPRING_BOOT ? application.springBootLine : application.springFrameworkLine;

    return line.replace(/\.x$/, "");
};

export const getSpringSliceVersions = (
    applications: ISpringPortfolioApplication[],
    platform: EPlatformName,
): string[] => {
    return [...new Set(applications.map((application) => springSliceVersionOf(application, platform)))].sort(
        compareVersions,
    );
};

export const getSpringSliceVersionsCount = (applications: ISpringPortfolioApplication[]): number => {
    return Math.max(...PLATFORM_ORDER.map((platform) => getSpringSliceVersions(applications, platform).length));
};

const isInSpringSliceRange = (application: ISpringPortfolioApplication, slice: ISpringSlice): boolean => {
    const version = springSliceVersionOf(application, slice.platform);

    return compareVersions(version, slice.from) >= 0 && compareVersions(version, slice.to) <= 0;
};

export const matchesSpringSlice = (application: ISpringPortfolioApplication, slice: ISpringSlice): boolean => {
    const status = application.ossSupported
        ? EFrameworkSupportStatus.OSS_SUPPORTED
        : EFrameworkSupportStatus.OUT_OF_OSS_MAINTENANCE;

    return isInSpringSliceRange(application, slice) && (slice.status === "ALL" || slice.status === status);
};

export const isSpringSliceFullRange = (applications: ISpringPortfolioApplication[], slice: ISpringSlice): boolean => {
    const versions = getSpringSliceVersions(applications, slice.platform);

    return slice.from === versions[0] && slice.to === versions[versions.length - 1];
};

export const normalizeSpringSlice = (
    applications: ISpringPortfolioApplication[],
    slice: ISpringSlice,
): ISpringSlice | null => {
    return slice.status === "ALL" && isSpringSliceFullRange(applications, slice) ? null : slice;
};

export const formatSpringSliceRange = (from: string, to: string): string => {
    return from === to ? from : `${from} – ${to}`;
};
