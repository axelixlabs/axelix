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
import { Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";

import { DashboardCard } from "components";

import { DashboardChartTooltip } from "../DashboardChartTooltip";
import { DashboardDonutCentre } from "../DashboardDonutCentre";
import { DashboardLegendItem } from "../DashboardLegendItem";

import styles from "./styles.module.css";

// TODO: Fix colors in future
const TEAL = "#2DD4BF";
const BLUE = "#4B9EFF";
const INDIGO = "#818CF8";
const PURPLE = "#A78BFA";

const mockData = [
    { name: "AOT Compilation", value: 38, fill: BLUE },
    { name: "CDS Enabled", value: 27, fill: TEAL },
    { name: "No Leyden", value: 24, fill: PURPLE },
    { name: "Partial Adoption", value: 11, fill: INDIGO },
];

export const DashboardLeydenChart = () => {
    return (
        <>
            <DashboardCard title="Project Leyden Adoption" subtitle="JVM optimisation">
                <div className={styles.ContentWrapper}>
                    <div className={styles.ChartWrapper}>
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={mockData}
                                    innerRadius={55}
                                    outerRadius={80}
                                    paddingAngle={3}
                                    dataKey="value"
                                    stroke="none"
                                />
                                <Tooltip content={<DashboardChartTooltip />} wrapperStyle={{ zIndex: 10 }} />
                            </PieChart>
                        </ResponsiveContainer>

                        {/* TODO: Fix after using real data */}
                        <DashboardDonutCentre topRowData="4" bottomRowData="methods" />
                    </div>

                    <div className={styles.LegendWrapper}>
                        {mockData.map(({ name, fill, value }) => (
                            <DashboardLegendItem key={name} circleColor={fill} label={name} value={`${value}%`} />
                        ))}
                    </div>
                </div>
            </DashboardCard>
        </>
    );
};
