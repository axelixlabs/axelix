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
import type {
    IBuildRulerTicksArgs,
    IBuildRulerTicksResult,
    IParsedVersion,
    IRulerBounds,
    IRulerTick,
    IStarterVersion,
    ITickAnnotation,
} from "@/models";
import { ERulerTickAnnotation } from "@/models";
import { UPGRADES_RULER_PADDING_BEFORE } from "@/utils";

const parseMinorVersion = (version: string): IParsedVersion => {
    const [major, minor] = version.split(".").map(Number);

    return {
        major: major,
        minor: minor,
    };
};

const formatMinorVersion = (major: number, minor: number): string => {
    return `${major}.${minor}`;
};

const getTickAnnotation = (minor: number, bounds: IRulerBounds): ITickAnnotation => {
    const { masterMinor, oldestMinor, lastSupportedMinor, dropMinor } = bounds;

    if (minor === oldestMinor) {
        return {
            annotation: ERulerTickAnnotation.OLDEST_SEEN,
        };
    }

    if (minor === masterMinor) {
        return {
            annotation: ERulerTickAnnotation.MASTER_NOW,
        };
    }

    if (minor > masterMinor && minor <= lastSupportedMinor) {
        return {
            annotation: ERulerTickAnnotation.SAFE_NEXT,
            offset: minor - masterMinor,
        };
    }

    if (minor === dropMinor) {
        return {
            annotation: ERulerTickAnnotation.DROP,
            offset: minor - masterMinor,
        };
    }

    return {};
};

export const buildRulerTicks = ({
    masterVersion,
    oldestStarterVersion,
    compatibilityWindowSize,
}: IBuildRulerTicksArgs): IBuildRulerTicksResult => {
    const master = parseMinorVersion(masterVersion);
    const oldest = parseMinorVersion(oldestStarterVersion);

    const masterMinor = master.minor;
    const oldestMinor = oldest.minor;

    const versionsAfterOldest = compatibilityWindowSize - 1;
    const lastSupportedMinor = oldestMinor + versionsAfterOldest;
    const dropMinor = lastSupportedMinor + 1;

    const leftmostMinor = Math.min(oldestMinor, masterMinor);
    const paddedStartMinor = leftmostMinor - UPGRADES_RULER_PADDING_BEFORE;
    const startMinor = Math.max(0, paddedStartMinor);

    const endMinor = Math.max(dropMinor, masterMinor);

    const bounds: IRulerBounds = { masterMinor, oldestMinor, lastSupportedMinor, dropMinor };

    const ticks: IRulerTick[] = [];

    for (let minor = startMinor; minor <= endMinor; minor += 1) {
        const version = formatMinorVersion(master.major, minor);
        const isSupported = minor >= oldestMinor && minor <= lastSupportedMinor;

        ticks.push({
            version: version,
            isSupported: isSupported,
            ...getTickAnnotation(minor, bounds),
        });
    }

    return {
        ticks: ticks,
        dropVersion: formatMinorVersion(master.major, dropMinor),
        headroomOffset: lastSupportedMinor - masterMinor,
    };
};

export const getOldestStarterVersion = (starterVersions: IStarterVersion[]): string | undefined => {
    const versions = starterVersions.map(({ version }) => version);

    const sortedVersions = versions.sort((a, b) => a.localeCompare(b, undefined, { numeric: true }));

    const [oldestVersion] = sortedVersions;

    return oldestVersion;
};

export const getTotalServiceCount = (starterVersions: IStarterVersion[]): number => {
    return starterVersions.reduce((summary, { serviceCount }) => summary + serviceCount, 0);
};

export const getOldestStarterServiceCount = (starterVersions: IStarterVersion[]): number => {
    const oldestStarterVersion = getOldestStarterVersion(starterVersions);

    return starterVersions.find(({ version }) => version === oldestStarterVersion)?.serviceCount ?? 0;
};
