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
import { getMaxAdoptionInfo } from "helpers";
import type { IJavaFeatureAdoption } from "models";

import { DashboardChartTooltip } from "../DashboardChartTooltip";
import { DashboardDonutCentre } from "../DashboardDonutCentre";
import { DashboardLegendItem } from "../DashboardLegendItem";

import styles from "./styles.module.css";

interface IProps {
    data: IJavaFeatureAdoption[];
    title: string;
    subtitle: string;
    showRest?: boolean;
}

// TODO: Fix colors in future
const DEFAULT_COLORS = ["#4B9EFF", "#2DD4BF", "#A78BFA", "#F59E0B", "#FB7185"];

export const DashboardDonutChart = ({ data, title, subtitle, showRest = true }: IProps) => {
    if (!data.length) {
        return (
            <DashboardCard title={title} subtitle={subtitle}>
                No data
            </DashboardCard>
        );
    }

    const totalAdoption = data.reduce((sum, { adoptionPercentage }) => sum + adoptionPercentage, 0);
    const restPercent = Math.round(100 - totalAdoption);
    const { maxPercent, featureNameWithMaxPercent } = getMaxAdoptionInfo(data);

    const chartData = data.map(({ featureId, adoptionPercentage }, index) => ({
        name: featureId,
        value: adoptionPercentage,
        fill: DEFAULT_COLORS[index % DEFAULT_COLORS.length],
    }));

    if (showRest && restPercent > 0) {
        chartData.push({
            name: "Rest",
            value: restPercent,
            fill: "#D1D5DB",
        });
    }

    return (
        <DashboardCard title={title} subtitle={subtitle}>
            <div className={styles.ContentWrapper}>
                <div className={styles.ChartWrapper}>
                    <ResponsiveContainer width="100%" height="100%">
                        <PieChart>
                            <Pie
                                data={chartData}
                                innerRadius={55}
                                outerRadius={80}
                                paddingAngle={3}
                                dataKey="value"
                                stroke="none"
                            />
                            <Tooltip content={<DashboardChartTooltip />} wrapperStyle={{ zIndex: 10 }} />
                        </PieChart>
                    </ResponsiveContainer>

                    <DashboardDonutCentre
                        topRowData={`${Math.round(maxPercent)}%`}
                        bottomRowData={featureNameWithMaxPercent}
                    />
                </div>

                <div className={styles.LegendWrapper}>
                    {chartData.map(({ name, fill, value }) => (
                        <DashboardLegendItem key={name} circleColor={fill} label={name} value={`${value}%`} />
                    ))}
                </div>
            </div>
        </DashboardCard>
    );
};
