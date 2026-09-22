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
import type { EDependencyEcosystem, ESupportStatus } from "@/models";

export interface ILineWindow {
    line: string;
    releasedAt: string;
    ossSupportEndsAt: string;
    commercialSupportEndsAt: string | null;
}

export interface IFrameworkSupportWindow {
    name: string;
    version: string;
    line: ILineWindow;
    latestKnownLine: ILineWindow;
    oldestSupportedLine: ILineWindow;
}

export interface IDependencyLibrary {
    groupId: string;
    artifactId: string;
}

export interface IResolvedDependencyRef {
    library: IDependencyLibrary;
    version: string;
}

export interface ISoftwareProjectId {
    value: string;
}

export interface ISoftwareProjectReference {
    label: string;
    url: string;
}

export interface ISoftwareProject {
    id: ISoftwareProjectId;
    displayName: string;
    ecosystem: EDependencyEcosystem;
    status: ESupportStatus;
    summary: string;
    libraries: IDependencyLibrary[];
    succession: unknown | null;
    reference: ISoftwareProjectReference;
}

export interface IResolvedDependency {
    dependency: IResolvedDependencyRef;
    resolutionPath: string[];
    softwareProject?: ISoftwareProject | null;
}

export interface IDependenciesAnalysis {
    rootCoordinates: string;
    analyzedAt: string;
    framework: IFrameworkSupportWindow;
    dependencies: IResolvedDependency[];
}
