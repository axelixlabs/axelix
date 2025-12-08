/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
