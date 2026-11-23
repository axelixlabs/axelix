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
import type { IStarterVersion } from "@/models";

const parseMinorVersion = (version: string): number[] => version.split(".", 2).map(Number);

export const getUnsupportedServiceCount = (
    targetVersion: string,
    starterVersions: IStarterVersion[],
    compatibilityWindow: number,
): number => {
    const [targetMajor, targetMinor] = parseMinorVersion(targetVersion);

    return starterVersions.reduce((total, { version, serviceCount }) => {
        const [starterMajor, starterMinor] = parseMinorVersion(version);
        const age = targetMinor - starterMinor;
        const isCompatible = targetMajor === starterMajor && age >= 0 && age < compatibilityWindow;

        return total + (isCompatible ? 0 : serviceCount);
    }, 0);
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
