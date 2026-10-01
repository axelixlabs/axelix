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
import type { ERulerTickAnnotation } from "../enums/upgrades";

export interface IBuildRulerTicksResult {
    ticks: IRulerTick[];
    dropVersion: string;
    headroomOffset: number;
}

export interface IBuildRulerTicksArgs {
    masterVersion: string;
    oldestStarterVersion: string;
    compatibilityWindowSize: number;
}

export interface IUpgradeArtifact {
    artifactId: string;
    groupId: string;
    starterVersion: string;
    pluginVersion: string;
    lastSeen: string;
}

export interface IStarterVersion {
    version: string;
    serviceCount: number;
}

export interface ICeilingBlocker {
    artifactId: string;
    groupId: string;
    starterVersion: string;
    lastSeen: string;
}

export interface IUpgradesData {
    masterVersion: string;
    compatibilityWindow: number;
    starterVersions: IStarterVersion[];
    ceilingBlockers: ICeilingBlocker[];
}

export interface IParsedVersion {
    major: number;
    minor: number;
}

export interface IRulerTick {
    version: string;
    isSupported: boolean;
    annotation?: ERulerTickAnnotation;
    offset?: number;
}

export interface IUpgradesLostApplicationsData {
    targetVersion: string;
    lostApplications: number;
}

export interface IRulerBounds {
    masterMinor: number;
    oldestMinor: number;
    lastSupportedMinor: number;
    dropMinor: number;
}

export interface ITickAnnotation {
    annotation?: ERulerTickAnnotation;
    offset?: number;
}
