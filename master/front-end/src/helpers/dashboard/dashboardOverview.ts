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
import type { EInstanceStatus, IChartData, IDistribution, IExtendedChartData, IHealthStatus } from "models";
import { HEALTH_STATUSES_COLORS } from "utils";

export const prepareHealthStatusesChartData = (statuses: IHealthStatus["statuses"]) => {
    const statusesEntries = Object.entries(statuses) as [EInstanceStatus, number][];

    return statusesEntries.map(([name, value]) => ({
        name: name,
        value: value,
        statusColor: HEALTH_STATUSES_COLORS[name],
    }));
};

export const prepareDistributionDataPerChart = (distributions: IDistribution[]): IExtendedChartData[] => {
    return distributions.map(({ softwareComponentName, versions }) => {
        const parsedVersions = Object.entries(versions).map(
            ([version, value]) =>
                ({
                    categoryName: version,
                    value: value,
                }) as IChartData,
        );

        return {
            softwareComponentName: softwareComponentName,
            versions: parsedVersions,
        };
    });
};

export const findMostUsed = (records: IChartData[]) => {
    return records.reduce((currentMostUsed, incoming) => {
        if (incoming.value > currentMostUsed.value) {
            return incoming;
        }

        return currentMostUsed;
    }).categoryName;
};
