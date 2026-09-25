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
import { DashboardPagesFirstSection } from "@/components";
import type { IUpgradeResponseBody } from "@/models";

import { UpgradeArtifactsTable } from "./UpgradeArtifactsTable";
import { UpgradeEmptyState } from "./UpgradeEmptyState";
import { UpgradeFleetBars } from "./UpgradeFleetBars";
import { UpgradeHeadroomPanel } from "./UpgradeHeadroomPanel";
import { UpgradeNoBlockersState } from "./UpgradeNoBlockersState";

// TODO: Replace with real API call once the backend endpoint is ready.
const MOCK_DATA: IUpgradeResponseBody = {
    masterVersion: "1.4",
    oldestStarterVersion: "1.2",
    oldestStarterServiceCount: 5,
    compatibilityWindowSize: 4,
    totalServiceCount: 57,
    laggingArtifacts: [
        {
            artifactId: "oee-exporter-legacy",
            groupId: "com.northwind.exporting",
            starterVersion: "1.2.1",
            pluginVersion: "1.2.1",
            lastSeen: "26 years ago",
        },
        {
            artifactId: "pallet-labelling-svc",
            groupId: "com.northwind.logistics",
            starterVersion: "1.2.4",
            pluginVersion: "1.2.4",
            lastSeen: "4 days ago",
        },
        {
            artifactId: "cold-chain-telemetry",
            groupId: "com.northwind.liwel",
            starterVersion: "1.2.4",
            pluginVersion: "1.2.1",
            lastSeen: "11 days ago",
        },
        {
            artifactId: "packaging-scale-bridge",
            groupId: "com.northwind.liwel",
            starterVersion: "1.2.6",
            pluginVersion: "1.2.4",
            lastSeen: "2 days ago",
        },
        {
            artifactId: "shift-handover-api",
            groupId: "com.northwind.operations",
            starterVersion: "1.2.6",
            pluginVersion: "1.2.6",
            lastSeen: "Today 06:20",
        },
    ],
    versionBars: [
        { version: "1.4", serviceCount: 38, isCurrent: true },
        { version: "1.3", serviceCount: 14, isCurrent: false },
        { version: "1.2", serviceCount: 5, isCurrent: false },
    ],
};

const Upgrade = () => {
    const data = MOCK_DATA;
    const isEmpty = data.totalServiceCount === 0;

    const isFleetUpToDate =
        !isEmpty && data.laggingArtifacts.length === 0 && data.versionBars.every(({ isCurrent }) => isCurrent);

    return (
        <>
            <DashboardPagesFirstSection
                title="Upgrades"
                subtitle="Derived from starter versions observed in service registrations over the last 30 days. No external calls."
            />

            {isEmpty ? (
                <UpgradeEmptyState />
            ) : isFleetUpToDate ? (
                <UpgradeNoBlockersState data={data} />
            ) : (
                <>
                    <UpgradeHeadroomPanel data={data} />

                    <UpgradeArtifactsTable artifacts={data.laggingArtifacts} />

                    <UpgradeFleetBars
                        versionBars={data.versionBars}
                        totalServiceCount={data.totalServiceCount}
                        isFleetUpToDate={false}
                        oldestStarterVersion={data.oldestStarterVersion}
                    />
                </>
            )}
        </>
    );
};

export default Upgrade;
