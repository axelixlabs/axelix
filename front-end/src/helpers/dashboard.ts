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
import type { EInstanceStatus, IDistribution, IHealthStatus } from "models";
import { HEALTH_STATUSES_COLORS } from "utils";

export const prepareHealthStatusesChartData = (statuses: IHealthStatus["statuses"]) => {
    const statusesEntries = Object.entries(statuses) as [EInstanceStatus, number][];

    return statusesEntries.map(([name, value]) => ({
        name: name,
        value: value,
        statusColor: HEALTH_STATUSES_COLORS[name],
    }));
};

export const getTotalStatusesCount = (statuses: IHealthStatus["statuses"]): number => {
    return Object.entries(statuses).reduce((acc, [, statusCount]) => acc + statusCount, 0);
};

const generateRandomColor = (): string => {
    return `#${Math.floor(Math.random() * 16777215)
        .toString(16)
        .padStart(6, "0")}`;
};

export const prepareDistributionDataPerChart = (distributions: IDistribution[]) => {
    return distributions.map(({ softwareComponentName, versions }) => {
        const parsedVersions = Object.entries(versions).map(([version, value]) => ({
            name: version,
            value: value,
            versionColor: generateRandomColor(),
        }));

        return {
            softwareComponentName: softwareComponentName,
            versions: parsedVersions,
        };
    });
};
