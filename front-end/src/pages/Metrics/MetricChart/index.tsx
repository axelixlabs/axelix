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
import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

import { formatXAxis, getMetricsChartTicks, reduceDisplayedNumber } from "helpers";
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

export const MetricChart = ({ measurements, startTime }: IProps) => {
    const endTime = startTime + METRIC_SLIDING_WINDOW_MS;

    return (
        <ResponsiveContainer className={styles.MainWrapper}>
            <LineChart data={measurements} margin={{ top: 10, right: 20 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis
                    dataKey="timestamp"
                    type="number"
                    scale="time"
                    domain={[startTime, endTime]}
                    tickFormatter={formatXAxis}
                    ticks={getMetricsChartTicks(startTime, endTime)}
                />
                <YAxis tickFormatter={reduceDisplayedNumber} type="number" domain={["auto", "auto"]} />

                {/* TODO: Consider the option of correct time display */}
                <Tooltip labelFormatter={(value) => new Date(value).toLocaleTimeString()} />
                <Line
                    type="monotone"
                    dataKey="value"
                    stroke="#00ab55"
                    strokeWidth={3}
                    activeDot={{ r: 5 }}
                    isAnimationActive={false}
                />
            </LineChart>
        </ResponsiveContainer>
    );
};
