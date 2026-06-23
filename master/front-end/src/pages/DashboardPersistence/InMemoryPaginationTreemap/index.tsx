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
import { ResponsiveContainer, Tooltip, Treemap } from "recharts";

import { DashboardCard } from "components";
import type { IDashboardPersistenceStatsData, IDashboardTreemapEntity, IDashboardTreemapEntityWithColor } from "models";

import { DashboardPersistenceLegend } from "../DashboardPersistenceLegend";
import { DashboardPersistenceStats } from "../DashboardPersistenceStats";
import { TreemapCell } from "../TreemapCell";
import sharedStyles from "../shared.module.css";

import { InMemoryPaginationTreemapTooltip } from "./InMemoryPaginationTreemapTooltip";
import styles from "./styles.module.css";

const mockData: IDashboardTreemapEntity[] = [
    { name: "ProductListAPI", size: 45000, entity: "Product" },
    { name: "OrderHistoryCtrl", size: 32800, entity: "Order" },
    { name: "UserSearchSvc", size: 28600, entity: "User" },
    { name: "ReportExporter", size: 21400, entity: "Report" },
    { name: "AuditViewer", size: 18700, entity: "AuditLog" },
    { name: "LogBrowser", size: 15200, entity: "LogEntry" },
    { name: "InventoryMgr", size: 11900, entity: "Item" },
    { name: "AnalyticsFeed", size: 8400, entity: "Event" },
];

const paginationColors = ["#6366f1", "#8b5cf6", "#a78bfa", "#7c3aed", "#818cf8", "#c4b5fd", "#4f46e5", "#ddd6fe"];

const dataWithColors: IDashboardTreemapEntityWithColor[] = mockData.map((data, index) => ({
    ...data,
    fill: paginationColors[index % paginationColors.length],
}));

const renderPaginationCell = (props: any) => {
    const label = props.size >= 1000 ? `${(props.size / 1000).toFixed(1)}k records` : `${props.size} records`;

    return <TreemapCell {...props} fill={props.fill} valueLabel={label} />;
};

export const InMemoryPaginationTreemap = () => {
    const statsData: IDashboardPersistenceStatsData[] = [
        {
            label: "Endpoints",
            value: "Placeholder",
            color: "#6366f1",
        },
        {
            label: "Records loaded",
            value: "Placeholder",
            color: "#8b5cf6",
        },
        {
            label: "Largest loader",
            value: "Placeholder",
            color: "#7c3aed",
        },
    ];

    return (
        <>
            <DashboardCard title="In-Memory Pagination" subtitle="Persistence · memory pressure">
                <div className={sharedStyles.PersistenceCardContentWrapper}>
                    <DashboardPersistenceStats data={statsData} />

                    <div className={styles.ChartWrapper}>
                        <ResponsiveContainer width="100%" height="100%">
                            <Treemap
                                data={dataWithColors}
                                dataKey="size"
                                aspectRatio={4 / 3}
                                content={renderPaginationCell}
                            >
                                <Tooltip content={<InMemoryPaginationTreemapTooltip />} />
                            </Treemap>
                        </ResponsiveContainer>
                    </div>

                    <DashboardPersistenceLegend data={mockData} colors={paginationColors} />
                </div>
            </DashboardCard>
        </>
    );
};
