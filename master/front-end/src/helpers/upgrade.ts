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
    IRulerTick,
    RulerTickAnnotation,
} from "@/models";
import { PADDING_BEFORE } from "@/utils";

export const parseMinorVersion = (version: string): IParsedVersion => {
    const [major, minor] = version.split(".").map(Number);
    return { major, minor };
};

export const formatMinorVersion = (major: number, minor: number): string => {
    return `${major}.${minor}`;
};

export const buildRulerTicks = ({
    masterVersion,
    oldestStarterVersion,
    compatibilityWindowSize,
}: IBuildRulerTicksArgs): IBuildRulerTicksResult => {
    const master = parseMinorVersion(masterVersion);
    const oldest = parseMinorVersion(oldestStarterVersion);

    const windowEndMinor = oldest.minor + compatibilityWindowSize - 1;
    const dropMinor = windowEndMinor + 1;

    const startMinor = Math.min(oldest.minor, master.minor) - PADDING_BEFORE;
    const endMinor = Math.max(dropMinor, master.minor);

    const ticks: IRulerTick[] = [];

    for (let minor = startMinor; minor <= endMinor; minor += 1) {
        const version = formatMinorVersion(master.major, minor);
        const isSupported = minor >= oldest.minor && minor <= windowEndMinor;

        let annotation: RulerTickAnnotation | undefined;
        let offset: number | undefined;

        if (minor === oldest.minor) {
            annotation = "oldestSeen";
        } else if (minor === master.minor) {
            annotation = "masterNow";
        } else if (minor > master.minor && minor <= windowEndMinor) {
            annotation = "safeNext";
            offset = minor - master.minor;
        } else if (minor === dropMinor) {
            annotation = "drop";
            offset = minor - master.minor;
        }

        ticks.push({ version, isSupported, annotation, offset });
    }

    return {
        ticks,
        dropVersion: formatMinorVersion(master.major, dropMinor),
        headroomOffset: windowEndMinor - master.minor,
    };
};
