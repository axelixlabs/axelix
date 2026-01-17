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
import type { EInstanceStatus } from "models";

export interface IDistribution {
    /**
     * Name of the software component
     */
    softwareComponentName: string;

    /**
     * Key–value map of versions.
     *
     * Key - the version, value - the amount of software components of that version deployed.
     */
    versions: Record<string, number>;
}

export interface IHealthStatus {
    /**
     * Key–value map of statuses
     */
    statuses: Record<EInstanceStatus, number>;
}

export interface IDashboardResponseBody {
    /**
     * List of distributions
     */
    distributions: IDistribution[];

    /**
     * Overall health status
     */
    healthStatus: IHealthStatus;

    /**
     * Memory usage metrics
     */
    memoryUsage: IMemoryUsage;
}

export interface IMemoryUsage {
    /**
     * Average resident set size (RSS) memory usage
     */
    averageHeapSize: IMemoryMetric;

    /**
     * Total resident set size (RSS) memory usage
     */
    totalHeapSize: IMemoryMetric;
}

interface IMemoryMetric {
    /**
     * Unit of the memory metric (e.g., "MB")
     * */
    unit: string;

    /**
     * Value of the memory metric
     * */
    value: number;
}
