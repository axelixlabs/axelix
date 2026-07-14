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

export const prepareDistributionDataPerChart = (distributions: IDistribution[]) => {
    return distributions.map(({ softwareComponentName, versions }) => {
        const parsedVersions = Object.entries(versions)
            .sort(([ver1], [ver2]) => {
                return ver1.localeCompare(ver2);
            })
            .map(([version, value]) => ({
                categoryName: version,
                value: value,
            }));

        return {
            softwareComponentName: softwareComponentName,
            versions: parsedVersions,
        };
    });
};
