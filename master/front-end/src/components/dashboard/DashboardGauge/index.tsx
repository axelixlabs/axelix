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
import { DashboardCard } from "components";
import type { IFeatureAdoption } from "models";

import { ArcSegment } from "./ArcSegment";
import { StatCards } from "./StatCards";
import styles from "./styles.module.css";

const CX = 110;
const CY = 110;
const RADIUS = 80;
const THICKNESS = 20;
const GAP_DEG = 2;

// TODO: Fix colors in future
const SLATE = "#e5e7eb";
const GREEN = "#34D399";
const ROSE = "#FB7185";

interface IProps {
    data: IFeatureAdoption[];
    title: string;
    subtitle: string;
}

export const DashboardGauge = ({ data, title, subtitle }: IProps) => {
    const { adoptionPercentage } = data[0];
    const splitAngle = (adoptionPercentage / 100) * 180;
    const roundedPercent = Math.round(adoptionPercentage);

    const greenEnd = Math.max(180 + splitAngle - GAP_DEG, 180);
    const redStart = Math.min(180 + splitAngle + GAP_DEG, 360);

    return (
        <DashboardCard title={title} subtitle={subtitle} isEmpty={!data.length}>
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
                    {splitAngle > 0 && (
                        <ArcSegment
                            cx={CX}
                            cy={CY}
                            radius={RADIUS}
                            thickness={THICKNESS}
                            startDeg={180}
                            endDeg={greenEnd}
                            color={GREEN}
                        />
                    )}
                    {splitAngle < 180 && (
                        <ArcSegment
                            cx={CX}
                            cy={CY}
                            radius={RADIUS}
                            thickness={THICKNESS}
                            startDeg={redStart}
                            endDeg={360}
                            color={ROSE}
                        />
                    )}
                    <text x={CX} y={CY - 20} textAnchor="middle" dominantBaseline="central" className="TextMedium">
                        {roundedPercent}%
                    </text>
                </svg>

                <StatCards enabledPercent={roundedPercent} />
            </div>
        </DashboardCard>
    );
};
