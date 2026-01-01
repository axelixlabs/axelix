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
import type { PieLabelRenderProps } from "recharts";

import type { EInstanceStatus, IDistribution, IHealthStatus } from "models";
import { HEALTH_STATUSES_COLORS, RADIAN, pickColor } from "utils";

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

export const prepareDistributionDataPerChart = (distributions: IDistribution[]) => {
    return distributions.map(({ softwareComponentName, versions }) => {
        const parsedVersions = Object.entries(versions)
            .sort(([ver1], [ver2]) => {
                return ver1.localeCompare(ver2);
            })
            .map(([version, value]) => ({
                name: version,
                value: value,
                versionColor: pickColor(softwareComponentName, version),
            }));

        return {
            softwareComponentName: softwareComponentName,
            versions: parsedVersions,
        };
    });
};

/**
 * Function that renders an inner label (the actual value for the given category)
 */
export const calculateInnerValueCoordinates = (props: PieLabelRenderProps, totalValuesCount: number) => {
    const { cx, cy, midAngle, innerRadius, outerRadius, value } = props;

    const hasOnlyOneCategory = totalValuesCount == value;

    // Converting the radian-based coordinates to cartesian.
    // If it has only one category then display in the center
    const radius = hasOnlyOneCategory ? 0 : innerRadius + (outerRadius - innerRadius) * 0.5;
    const x = cx + radius * Math.cos(-midAngle! * RADIAN);
    const y = cy + radius * Math.sin(-midAngle! * RADIAN);

    const percentage = Math.floor((value / totalValuesCount) * 100);
    const displayedValue = `${value} (${percentage}%)`;

    return [x, y, displayedValue];
};
