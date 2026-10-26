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
    EDependencyEcosystem,
    ESupportStatus,
    IDependencyLibrary,
    ISoftwareProjectReference,
    ISoftwareProjectSuccession,
} from "@/models";

/**
 * Response body of {@code GET /api/external/dashboard/dependencies}.
 */
export interface IDependenciesDashboardResponseBody {
    totalApplications: number;
    applicationsWithRiskyDependencies: number;
    applicationsByStatus: Partial<Record<ESupportStatus, number>>;
    totalLibraries: number;
    librariesByStatus: Partial<Record<ESupportStatus, number>>;
    riskyLibrariesByEcosystem: Partial<Record<EDependencyEcosystem, Partial<Record<ESupportStatus, number>>>>;
    lastScannedAt: string;
}

export interface IVersionUsage {
    version: string;
    applicationCount: number;
}

/**
 * A library used by at least one application. The curation fields are {@code null} for the libraries Axelix does
 * not curate, e.g. an organization's internal ones.
 */
export interface IUsedLibrary {
    library: IDependencyLibrary;
    applicationCount: number;
    share: number;
    versions: IVersionUsage[];
    status: ESupportStatus | null;
    ecosystem: EDependencyEcosystem | null;
    summary: string | null;
    succession: ISoftwareProjectSuccession | null;
    reference: ISoftwareProjectReference | null;
}

export interface IDependencyLibrariesRequestParams {
    search?: string;
    status: ESupportStatus[];
    ecosystem: EDependencyEcosystem[];
    page: number;
    size: number;
}

/**
 * Response body of {@code GET /api/external/dashboard/dependencies/libraries}.
 */
export interface IDependencyLibrariesResponseBody {
    libraries: IUsedLibrary[];
    matchingLibraries: number;
    page: number;
    size: number;
}

/**
 * A ring of the applications donut: the applications that use the libraries of one support status, or none of them.
 */
export interface IApplicationsRing {
    key: string;
    color: string;
    label: string;
    hint?: string;
    count: number;
    percentage: number;
}
