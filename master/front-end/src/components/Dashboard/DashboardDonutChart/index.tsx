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
import type { IChartData } from "models";

import { DashboardChartTooltip } from "./DashboardChartTooltip";
import { DashboardDonutCentre } from "./DashboardDonutCentre";
import { DashboardLegendItem } from "./DashboardLegendItem";
import styles from "./styles.module.css";

interface IProps {
    /**
     * The actual data to be displayed in donut chart.
     */
    data: IChartData[];

    /**
     * Chart heading titling info.
     */
    heading: ITitle;

    /**
     * Centre titling info.
     */
    centre: ITitle;

    /**
     * Rest category configuration.
     */
    rest: IRestCategory;

    /**
     * Called when a pie segment is clicked. If this function is not supplied, the
     * pie is deemed to be non-clickable.
     */
    onPieClick?: (categoryName: string, event: React.MouseEvent) => void;
}

export interface ITitle {
    title: string;
    subtitle: string;
}

export interface IRestCategory {
    /**
     * Show the 'rest' category?
     */
    show: boolean;

    /**
     * The title of the 'rest' category in the legend. Must be supplied if 'show = true'
     */
    title?: string;
}

// TODO: Fix colors in future
const DEFAULT_COLORS = ["#2DD4BF", "#A78BFA", "#F59E0B", "#FB7185", "#4B9EFF"];

export const DashboardDonutChart = ({ data, heading, rest, centre, onPieClick }: IProps) => {
    const chartData = data.map(({ categoryName, value }, index) => ({
        name: categoryName,
        value,
        fill: DEFAULT_COLORS[index % DEFAULT_COLORS.length],
    }));

    const totalValue = data.reduce((sum, { value }) => sum + value, 0);
    const restPercent = Math.round(100 - totalValue);

    if (rest.show && restPercent > 0) {
        chartData.push({
            name: rest.title!,
            value: restPercent,
            fill: "#D1D5DB",
        });
    }

    return (
        <DashboardCard title={heading.title} subtitle={heading.subtitle} isEmpty={!chartData.length}>
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
                                cursor={onPieClick ? "pointer" : "default"}
                                onClick={({ name }, _index, event) => {
                                    if (name && onPieClick) {
                                        onPieClick(name, event);
                                    }
                                }}
                            />
                            <Tooltip content={<DashboardChartTooltip />} wrapperStyle={{ zIndex: 10 }} />
                        </PieChart>
                    </ResponsiveContainer>

                    <DashboardDonutCentre topRowData={centre.title} bottomRowData={centre.subtitle} />
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
