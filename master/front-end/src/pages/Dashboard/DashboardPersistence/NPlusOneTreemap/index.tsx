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
import type { IDashboardPersistenceStatsData, IDashboardTreemapEntity } from "models";

import { DashboardPersistenceLegend } from "../DashboardPersistenceLegend";
import { DashboardPersistenceStats } from "../DashboardPersistenceStats";
import { TreemapCell } from "../TreemapCell";
import sharedStyles from "../shared.module.css";

import { NPlusOneTreemapTooltip } from "./NPlusOneTreemapTooltip";
import styles from "./styles.module.css";

// TODO: Fix colors
const nPlusOneColors = ["#ef4444", "#f97316", "#f59e0b", "#eab308", "#fb7185", "#fca5a5", "#fdba74", "#fcd34d"];

// TODO: Fix type
const renderTreemapCell = (props: any): JSX.Element => {
    return <TreemapCell {...props} fill={props.fill} valueLabel={`${props.size} cases`} />;
};

interface IProps {
    nPlusOneEntries: IDashboardTreemapEntity[];
}

export const NPlusOneTreemap = ({ nPlusOneEntries }: IProps) => {
    const totalOccurrences = nPlusOneEntries.reduce((total, item) => total + item.size, 0);
    const maxOccurrences = nPlusOneEntries.reduce(
        (max, item) => (item.size > max.size ? item : max),
        nPlusOneEntries[0],
    );

    const coloredEntries = nPlusOneEntries.map((data, index) => ({
        ...data,
        name: data.appName,
        fill: nPlusOneColors[index % nPlusOneColors.length],
    }));

    const statsData: IDashboardPersistenceStatsData[] = [
        {
            label: "Total Occurrences",
            value: `${totalOccurrences}`,
            color: "#f97316",
        },
        {
            label: "Worst offender",
            value: `${maxOccurrences.appName}`,
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
                                data={coloredEntries}
                                dataKey="size"
                                aspectRatio={4 / 3}
                                content={renderTreemapCell}
                            >
                                <Tooltip content={<NPlusOneTreemapTooltip />} />
                            </Treemap>
                        </ResponsiveContainer>
                    </div>

                    <DashboardPersistenceLegend data={nPlusOneEntries} colors={nPlusOneColors} />
                </div>
            </DashboardCard>
        </>
    );
};
