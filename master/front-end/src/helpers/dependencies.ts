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
import dayjs from "dayjs";

import type { EDependencyEcosystem, ESupportStatus, IFrameworkSupportWindow, IResolvedDependency } from "@/models";

/**
 * A single hop of the chain leading from the root application down to a resolved dependency.
 */
export interface IResolutionNode {
    /**
     * The {@code group:artifact:version} coordinates of the hop.
     */
    coordinates: string;

    /**
     * How far below the root the hop sits, the root itself being 0.
     */
    depth: number;

    /**
     * Whether the hop is the analyzed application itself.
     */
    root: boolean;

    /**
     * Whether the hop is the dependency the path was built for, i.e. where the resolution lands.
     */
    resolved: boolean;
}

/**
 * The three horizontal measures, in percent of the full window, that the maintenance window bar is drawn from.
 */
export interface ISupportTimeline {
    /**
     * The share of the window during which the line received OSS releases.
     */
    ossPercentage: number;

    /**
     * The share of the window during which the line is under commercial support only.
     */
    commercialPercentage: number;

    /**
     * Where today sits on the window, clamped to its bounds.
     */
    todayPercentage: number;
}

/**
 * Strips the separators that Maven coordinates are full of, so that searching for {@code springboot} matches
 * {@code org.springframework.boot:spring-boot-starter-web}.
 */
const relax = (value: string): string => {
    return value.toLowerCase().replace(/[-_.: ]/g, "");
};

/**
 * Whether the dependency was declared in the build file itself rather than pulled in by another dependency.
 */
export const isDirectDependency = (dependency: IResolvedDependency): boolean => {
    return dependency.resolutionPath.length === 1;
};

/**
 * Whether a dependency survives the current search, ecosystem tab and support-signal filters. Exposed on its own
 * because the tab and filter counters each re-run it with one of the three relaxed.
 */
export const matchesDependencyFilters = (
    dependency: IResolvedDependency,
    search: string,
    ecosystem: EDependencyEcosystem | null,
    signals: ESupportStatus[],
): boolean => {
    const formattedSearch = search.trim();

    const coordinates = `${dependency.dependency.library.groupId}:${dependency.dependency.library.artifactId}`;

    if (formattedSearch && !relax(coordinates).includes(relax(formattedSearch))) {
        return false;
    }

    if (ecosystem !== null && dependency.softwareProject?.ecosystem !== ecosystem) {
        return false;
    }

    if (
        signals.length > 0 &&
        (dependency.softwareProject === null || !signals.includes(dependency.softwareProject.status))
    ) {
        return false;
    }

    return true;
};

/**
 * Filters the feed down to the dependencies matching the current search, ecosystem tab and support-signal filters.
 */
export const filterDependencies = (
    dependencies: IResolvedDependency[],
    search: string,
    ecosystem: EDependencyEcosystem | null,
    signals: ESupportStatus[],
): IResolvedDependency[] => {
    return dependencies.filter((dependency) => matchesDependencyFilters(dependency, search, ecosystem, signals));
};

/**
 * Expands the resolution path of a dependency into the hops rendered in its expanded detail, prepending the analyzed
 * application as the root of the chain.
 */
export const buildResolutionPath = (rootCoordinates: string, dependency: IResolvedDependency): IResolutionNode[] => {
    const coordinates = [rootCoordinates, ...dependency.resolutionPath];

    return coordinates.map((value, index) => ({
        coordinates: value,
        depth: index,
        root: index === 0,
        resolved: index === coordinates.length - 1,
    }));
};

/**
 * Measures out the maintenance window bar: how much of it the line spent under OSS maintenance, how much it spends
 * under commercial support only, and where today falls. A line without commercial support is entirely OSS.
 */
export const buildSupportTimeline = (platform: IFrameworkSupportWindow): ISupportTimeline => {
    const released = dayjs(platform.line.releasedAt).valueOf();
    const ossEnd = dayjs(platform.line.ossSupportEndsAt).valueOf();
    const end = platform.line.commercialSupportEndsAt ? dayjs(platform.line.commercialSupportEndsAt).valueOf() : ossEnd;
    const span = end - released;

    if (span <= 0) {
        return { ossPercentage: 100, commercialPercentage: 0, todayPercentage: 100 };
    }

    const ossPercentage = ((ossEnd - released) / span) * 100;
    const todayPercentage = ((dayjs().valueOf() - released) / span) * 100;

    return {
        ossPercentage,
        commercialPercentage: 100 - ossPercentage,
        todayPercentage: Math.min(Math.max(todayPercentage, 0), 100),
    };
};

/**
 * How many whole months have passed since the given ISO date, never negative.
 */
export const elapsedMonthsSince = (isoDate: string): number => {
    return Math.max(dayjs().diff(dayjs(isoDate), "month"), 0);
};
