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
