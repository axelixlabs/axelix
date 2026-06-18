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

import { DashboardChartTooltip } from "../DashboardChartTooltip";
import { DashboardDonutCentre } from "../DashboardDonutCentre";
import { DashboardJavaCard } from "../DashboardJavaCard";
import { DashboardLegendItem } from "../DashboardLegendItem";

import styles from "./styles.module.css";

// TODO: Fix colors
const BLUE = "#4B9EFF";
const GREEN = "#34D399";
const PURPLE = "#A78BFA";
const ORANGE = "#F59E0B";
const ROSE = "#FB7185";

const mockData = [
    { name: "G1GC", value: 54, fill: GREEN },
    { name: "ZGC", value: 21, fill: BLUE },
    { name: "Shenandoah", value: 12, fill: ORANGE },
    { name: "ParallelGC", value: 8, fill: PURPLE },
    { name: "SerialGC", value: 5, fill: ROSE },
];

export const DashboardGCDistributionChart = () => {
    return (
        <DashboardJavaCard title="Garbage Collector Distribution" subtitle="Runtime profile">
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
                    <DashboardDonutCentre topRowData="54%" bottomRowData="G1GC" />
                </div>

                <div className={styles.LegendWrapper}>
                    {mockData.map(({ name, fill, value }) => (
                        <DashboardLegendItem key={name} circleColor={fill} label={name} value={`${value}%`} />
                    ))}
                </div>
            </div>
        </DashboardJavaCard>
    );
};
