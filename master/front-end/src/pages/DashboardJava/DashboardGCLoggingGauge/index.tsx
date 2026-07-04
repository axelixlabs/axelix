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
import { DashboardJavaCard } from "../DashboardJavaCard";

import { ArcSegment } from "./ArcSegment";
import { StatCards } from "./StatCards";
import styles from "./styles.module.css";

const CX = 110;
const CY = 110;
const RADIUS = 80;
const THICKNESS = 20;

const GAP_DEG = 2;
const SLATE = "#e5e7eb";

const gcLogEnabled = 200;
const gcLogDisabled = 100;

// TODO: Fix colors in future
const GREEN = "#34D399";
const ROSE = "#FB7185";

export const DashboardGCLoggingGauge = () => {
    const total = gcLogEnabled + gcLogDisabled;
    const enabledPercent = total > 0 ? (gcLogEnabled / total) * 100 : 0;
    const split = (enabledPercent / 100) * 180;

    const roundedEnabledPercent = Math.round(enabledPercent);

    return (
        <DashboardJavaCard title="Garbage Collector Logging" subtitle="Log output coverage">
            <div className={styles.ContentWrapper}>
                <svg viewBox="0 0 220 120" width="220" height="120">
                    <ArcSegment
                        cx={CX}
                        cy={CY}
                        radius={RADIUS}
                        thickness={THICKNESS}
                        startDeg={180}
                        endDeg={360}
                        color={SLATE}
                    />
                    <ArcSegment
                        cx={CX}
                        cy={CY}
                        radius={RADIUS}
                        thickness={THICKNESS}
                        startDeg={180}
                        endDeg={180 + split - GAP_DEG}
                        color={GREEN}
                    />
                    <ArcSegment
                        cx={CX}
                        cy={CY}
                        radius={RADIUS}
                        thickness={THICKNESS}
                        startDeg={180 + split + GAP_DEG}
                        endDeg={360}
                        color={ROSE}
                    />
                    <text x={CX} y={CY - 20} textAnchor="middle" dominantBaseline="central" className="TextMedium">
                        {roundedEnabledPercent}%
                    </text>
                </svg>

                <StatCards enabledPercent={roundedEnabledPercent} />
            </div>
        </DashboardJavaCard>
    );
};
