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
import { CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

import { reduceDisplayedNumber } from "helpers";
import type { IMeasurement } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Measurements for the metric
     */
    measurements: IMeasurement[];
}

export default function MetricChart({ measurements }: IProps) {
    const data = measurements.map((measurement) => ({
        statistic: "value",
        value: measurement.value,
    }));

    return (
        <div className={styles.MainWrapper}>
            <ResponsiveContainer>
                <LineChart data={data} margin={{ top: 10, right: 20, left: 0, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="statistic" />
                    <YAxis tickFormatter={reduceDisplayedNumber} />
                    <Tooltip />
                    <Legend />
                    <Line type="monotone" dataKey="value" stroke="#00ab55" dot />
                </LineChart>
            </ResponsiveContainer>
        </div>
    );
}
