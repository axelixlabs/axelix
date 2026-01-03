/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
