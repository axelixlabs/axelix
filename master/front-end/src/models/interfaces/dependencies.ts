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
import type { EDependencyEcosystem, EDependencyNote, EPlatformSupportStatus, ESupportSignal } from "@/models";

/**
 * The maintenance window of the platform an instance runs on, as read from the running JVM and dated against the
 * upstream support policy.
 */
export interface IPlatformSupportWindow {
    /**
     * The platform name (e.g. {@code Spring Boot}).
     */
    name: string;

    /**
     * The exact version the instance runs (e.g. {@code 3.2.4}).
     */
    version: string;

    /**
     * The release line {@link IPlatformSupportWindow.version} belongs to (e.g. {@code 3.2.x}).
     */
    line: string;

    /**
     * The most recent release line Axelix knows about (e.g. {@code 3.5.x}).
     */
    latestKnownLine: string;

    /**
     * Where {@link IPlatformSupportWindow.line} sits on its OSS maintenance window.
     */
    status: EPlatformSupportStatus;

    /**
     * The ISO date the line was released.
     */
    releasedAt: string;

    /**
     * The ISO date OSS maintenance of the line ended, or ends.
     */
    ossSupportEndsAt: string;

    /**
     * The ISO date commercial support of the line ends, or null when the upstream offers none.
     */
    commercialSupportEndsAt: string | null;

    /**
     * The release line a team on this platform is expected to move to (e.g. {@code 3.5.x}).
     */
    supportedTargetLine: string;

    /**
     * The ISO date OSS maintenance of {@link IPlatformSupportWindow.supportedTargetLine} ends.
     */
    supportedTargetOssEndsAt: string;
}

/**
 * The migration note attached to a dependency whose project is discontinued or in maintenance mode.
 */
export interface IDependencyNote {
    /**
     * How the note should be labelled.
     */
    kind: EDependencyNote;

    /**
     * The note itself (e.g. {@code io.micrometer:micrometer-tracing-bridge-brave}).
     */
    value: string;
}

/**
 * What the curated project registry states about a dependency whose project is no longer developed as usual.
 */
export interface IDependencySupportSignal {
    /**
     * The kind of the signal.
     */
    kind: ESupportSignal;

    /**
     * Why the project carries the signal, in prose.
     */
    reason: string;

    /**
     * What the signal was derived from (e.g. {@code Curated project registry · artifact archived, no release since
     * Jan 2023}).
     */
    evidence: string;

    /**
     * The migration note, or null when the project has no successor to point at.
     */
    note: IDependencyNote | null;
}

/**
 * A single dependency resolved at runtime, together with how it entered the build graph and what the curated project
 * registry states about the project behind it.
 */
export interface IResolvedDependency {
    /**
     * The {@code group:artifact} coordinates (e.g. {@code org.springframework:spring-webmvc}).
     */
    coordinates: string;

    /**
     * The name of the project that publishes the artifact (e.g. {@code Spring Framework}).
     */
    project: string;

    /**
     * The resolved version (e.g. {@code 6.1.5}).
     */
    version: string;

    /**
     * The area of the runtime the dependency belongs to.
     */
    ecosystem: EDependencyEcosystem;

    /**
     * What the registry states about the project, or null when nothing is flagged about it.
     */
    signal: IDependencySupportSignal | null;

    /**
     * The chain of {@code group:artifact:version} coordinates leading from the root application to this dependency,
     * the dependency itself being the last element. A chain of a single element is a direct dependency.
     */
    resolutionPath: string[];

    /**
     * The development state of the project, in prose.
     */
    status: string;

    /**
     * The label of the upstream page the row links to.
     */
    referenceLabel: string;

    /**
     * The URL of the upstream page the row links to.
     */
    referenceUrl: string;
}

/**
 * The dependency analysis of a single instance: its platform maintenance window and every dependency resolved at
 * runtime beneath it.
 */
export interface IDependenciesAnalysis {
    /**
     * The {@code group:artifact:version} coordinates of the analyzed application itself.
     */
    rootCoordinates: string;

    /**
     * The ISO timestamp the analysis was taken at.
     */
    analyzedAt: string;

    /**
     * The maintenance window of the platform the instance runs on.
     */
    platform: IPlatformSupportWindow;

    /**
     * Every dependency resolved at runtime.
     */
    dependencies: IResolvedDependency[];
}
