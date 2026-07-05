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
import type { JSX } from "react";
import { ResponsiveContainer, Tooltip, Treemap } from "recharts";

import { DashboardCard } from "components";
import type { IDashboardPersistenceStatsData, IDashboardTreemapEntity, IDashboardTreemapEntityWithColor } from "models";

import { DashboardPersistenceLegend } from "../DashboardPersistenceLegend";
import { DashboardPersistenceStats } from "../DashboardPersistenceStats";
import { TreemapCell } from "../TreemapCell";
import sharedStyles from "../shared.module.css";

import { NPlusOneTreemapTooltip } from "./NPlusOneTreemapTooltip";
import styles from "./styles.module.css";

// TODO: Fix colors
const nPlusOneColors = ["#ef4444", "#f97316", "#f59e0b", "#eab308", "#fb7185", "#fca5a5", "#fdba74", "#fcd34d"];

const mockData: IDashboardTreemapEntity[] = [
    { name: "UserRepository", size: 312, entity: "User" },
    { name: "OrderService", size: 248, entity: "Order" },
    { name: "ProductCatalog", size: 195, entity: "Product" },
    { name: "InvoiceLoader", size: 167, entity: "Invoice" },
    { name: "CommentFeed", size: 134, entity: "Comment" },
    { name: "TagResolver", size: 98, entity: "Tag" },
    { name: "AddressMapper", size: 87, entity: "Address" },
    { name: "PaymentGateway", size: 76, entity: "Payment" },
    { name: "NotificationSvc", size: 54, entity: "Notification" },
    { name: "AuditLog", size: 43, entity: "AuditEntry" },
];

const dataWithColors: IDashboardTreemapEntityWithColor[] = mockData.map((data, index) => ({
    ...data,
    fill: nPlusOneColors[index % nPlusOneColors.length],
}));

// TODO: Fix type
const renderTreemapCell = (props: any): JSX.Element => {
    return <TreemapCell {...props} fill={props.fill} valueLabel={`${props.size} queries`} />;
};

export const NPlusOneTreemap = () => {
    const statsData: IDashboardPersistenceStatsData[] = [
        {
            label: "Total Occurrences",
            value: "Placeholder",
            color: "#f97316",
        },
        {
            label: "Worst offender",
            value: "Placeholder",
            color: "#f59e0b",
        },
    ];

    return (
        <>
            <DashboardCard title="N + 1" subtitle="Persistence · query anti-patterns">
                <div className={sharedStyles.PersistenceCardContentWrapper}>
                    <DashboardPersistenceStats data={statsData} />

                    <div className={styles.TreemapWrapper}>
                        <ResponsiveContainer width="100%" height="100%">
                            <Treemap
                                data={dataWithColors}
                                dataKey="size"
                                aspectRatio={4 / 3}
                                content={renderTreemapCell}
                            >
                                <Tooltip content={<NPlusOneTreemapTooltip />} />
                            </Treemap>
                        </ResponsiveContainer>
                    </div>

                    <DashboardPersistenceLegend data={mockData} colors={nPlusOneColors} />
                </div>
            </DashboardCard>
        </>
    );
};
