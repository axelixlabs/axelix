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
        statistic: measurement.statistic,
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
