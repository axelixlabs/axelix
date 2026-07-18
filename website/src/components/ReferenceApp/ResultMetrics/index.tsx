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
        label: "Memory footprint · RSS Under Load",
        value: "−15%",
        unit: "reduction",
        was: { amount: "618 MB", widthPercent: "100%" },
        now: { amount: "533 MB", widthPercent: "86%" },
    },
    {
        label: "Throughput · under load",
        value: "+183%",
        unit: "increase",
        was: { amount: "29 rps", widthPercent: "35%" },
        now: { amount: "82 rps", widthPercent: "100%" },
    },
    {
        label: "GC Stop-The-World pauses",
        value: "−91%",
        unit: "less",
        was: { amount: "587", widthPercent: "100%" },
        now: { amount: "50", widthPercent: "9%" },
    },
    {
        label: "Startup time",
        value: "−30%",
        unit: "faster",
        was: { amount: "4.33 s", widthPercent: "100%" },
        now: { amount: "3.07 s", widthPercent: "71%" },
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
