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
import { ResultMetricCard } from "./ResultMetricCard";
import styles from "./styles.module.css";

const metrics = [
    {
        label: "Memory footprint · RSS",
        value: "−25%",
        unit: "reduction",
        was: { amount: "498 MB", widthPercent: "100%" },
        now: { amount: "374 MB", widthPercent: "75%" },
    },
    {
        label: "Throughput · under load",
        value: "+25%",
        unit: "increase",
        was: { amount: "1.4k rps", widthPercent: "80%" },
        now: { amount: "1.75k rps", widthPercent: "100%" },
    },
    {
        label: "Docker image size",
        value: "−40%",
        unit: "smaller",
        was: { amount: "327 MB", widthPercent: "100%" },
        now: { amount: "196 MB", widthPercent: "60%" },
    },
    {
        label: "Startup time",
        value: "−41%",
        unit: "faster",
        was: { amount: "6.4 s", widthPercent: "100%" },
        now: { amount: "3.8 s", widthPercent: "59%" },
    },
];

export const ResultMetrics = () => {
    return (
        <aside className={styles.MainWrapper}>
            {metrics.map(({ label, value, unit, was, now }) => (
                <ResultMetricCard key={label} label={label} value={value} unit={unit} was={was} now={now} />
            ))}
        </aside>
    );
};
