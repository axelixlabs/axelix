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
import type { RulerTickAnnotation } from "../types/upgrade";

export interface IUpgradeArtifact {
    /**
     * Artifact identifier, e.g. "oee-exporter-legacy"
     */
    artifactId: string;

    /**
     * Group id, e.g. "com.northwind.exporting"
     */
    groupId: string;

    /**
     * Version of the starter reported by this artifact
     */
    starterVersion: string;

    /**
     * Version of the plugin used by this artifact
     */
    pluginVersion: string;

    /**
     * Human-readable last seen time, e.g. "26 years ago", "4 days ago"
     */
    lastSeen: string;
}

export interface IUpgradeVersionBar {
    /**
     * Starter version string, e.g. "1.4"
     */
    version: string;

    /**
     * Number of services running this starter version
     */
    serviceCount: number;

    /**
     * True if this version equals the current Master version
     */
    isCurrent: boolean;
}

export interface IUpgradeResponseBody {
    /**
     * Version Master is currently running, e.g. "1.4"
     */
    masterVersion: string;

    /**
     * Oldest starter version observed across the fleet, e.g. "1.2"
     */
    oldestStarterVersion: string;

    /**
     * Number of services running the oldest starter version
     */
    oldestStarterServiceCount: number;

    /**
     * Width of the compatibility window in minor releases, e.g. 4
     */
    compatibilityWindowSize: number;

    /**
     * Services that are lagging behind the newest starter version.
     * Empty when the fleet is up to date.
     */
    laggingArtifacts: IUpgradeArtifact[];

    /**
     * Aggregated starter version distribution across the fleet.
     */
    versionBars: IUpgradeVersionBar[];

    /**
     * Total number of services tracked in the last 30 days.
     */
    totalServiceCount: number;
}

export interface IBuildRulerTicksArgs {
    masterVersion: string;
    oldestStarterVersion: string;
    compatibilityWindowSize: number;
}

export interface IBuildRulerTicksResult {
    ticks: IRulerTick[];
    dropVersion: string;
    headroomOffset: number;
}

export interface IRulerTick {
    version: string;
    isSupported: boolean;
    annotation?: RulerTickAnnotation;
    offset?: number;
}

export interface IParsedVersion {
    major: number;
    minor: number;
}
