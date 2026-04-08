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
import { Fragment, type JSX } from "react";
import {
    CartesianGrid,
    type DotItemDotProps,
    Line,
    LineChart,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from "recharts";

import { getMetricsChartTicks, reduceDisplayedNumber, toFormattedTime } from "helpers";
import type { IMeasurementsWithTimestamp } from "models";
import { METRIC_SLIDING_WINDOW_MS } from "utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Measurements for the metric
     */
    measurements: IMeasurementsWithTimestamp[];

    /**
     * Start of the chart time window (in milliseconds from epoch)
     */
    startTime: number;
}

const renderDot = ({ cx, cy, index, points }: DotItemDotProps): JSX.Element => {
    // we're rendering the head of the line - render the dot.
    if (index === points.length - 1) {
        return <circle cx={cx} cy={cy} r={3} className={styles.Dot} />;
    }

    return <Fragment key={index} />;
};

export const MetricChart = ({ measurements, startTime }: IProps) => {
    const endTime = startTime + METRIC_SLIDING_WINDOW_MS;

    return (
        <>
            <ResponsiveContainer className={styles.MainWrapper}>
                <LineChart data={measurements} margin={{ top: 10, right: 20 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis
                        dataKey="timestamp"
                        type="number"
                        scale="time"
                        domain={[startTime, endTime]}
                        tickFormatter={toFormattedTime}
                        ticks={getMetricsChartTicks(startTime, endTime)}
                    />

                    <YAxis tickFormatter={reduceDisplayedNumber} type="number" domain={["auto", "auto"]} />
                    <Tooltip
                        labelFormatter={(label) => {
                            if (typeof label !== "number") {
                                return label;
                            }

                            return toFormattedTime(label);
                        }}
                    />

                    <Line
                        type="monotone"
                        dataKey="value"
                        stroke="#00ab55"
                        strokeWidth={3}
                        activeDot={{ r: 5 }}
                        dot={renderDot}
                        isAnimationActive={false}
                    />
                </LineChart>
            </ResponsiveContainer>
        </>
    );
};
