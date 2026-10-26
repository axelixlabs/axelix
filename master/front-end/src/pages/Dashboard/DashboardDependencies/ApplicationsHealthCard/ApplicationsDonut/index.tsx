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
import type { IApplicationsRing } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    rings: IApplicationsRing[];
}

const OUTER_RADIUS = 62;
const RING_STEP = 14;

export const ApplicationsDonut = ({ rings }: IProps) => {
    return (
        <>
            <svg width="150" height="150" viewBox="0 0 140 140" className={`${styles.SVG} DonutSpinSettle`}>
                {rings.map(({ key, color, percentage }, index) => {
                    const radius = OUTER_RADIUS - index * RING_STEP;
                    const circumference = 2 * Math.PI * radius;
                    const length = (percentage / 100) * circumference;

                    return (
                        <g key={key}>
                            <circle cx="70" cy="70" r={radius} className={styles.Track} />
                            {length > 0 && (
                                <circle
                                    cx="70"
                                    cy="70"
                                    r={radius}
                                    className={styles.Arc}
                                    stroke={color}
                                    strokeDasharray={`${length} ${circumference}`}
                                />
                            )}
                        </g>
                    );
                })}
            </svg>
        </>
    );
};
