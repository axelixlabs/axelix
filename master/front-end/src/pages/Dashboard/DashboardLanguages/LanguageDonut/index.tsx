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
import type { ILanguagesDonutSegment } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    segments: ILanguagesDonutSegment[];
    centreTop: string;
    centreBottom: string;
}

const RADIUS = 52;
const CIRCUMFERENCE = 2 * Math.PI * RADIUS;

export const LanguageDonut = ({ segments, centreTop, centreBottom }: IProps) => {
    let consumed = 0;

    return (
        <>
            <div className={styles.MainWrapper}>
                <svg width="150" height="150" viewBox="0 0 140 140" className={styles.SVG}>
                    {segments.map(({ value, color }, index) => {
                        const length = (value / 100) * CIRCUMFERENCE;
                        const offset = -((consumed / 100) * CIRCUMFERENCE);
                        consumed += value;

                        return (
                            <circle
                                key={index}
                                cx="70"
                                cy="70"
                                r={RADIUS}
                                fill="none"
                                stroke={color}
                                strokeWidth="22"
                                strokeDasharray={`${length} ${CIRCUMFERENCE - length}`}
                                strokeDashoffset={offset}
                            />
                        );
                    })}
                </svg>
                <div className={styles.Centre}>
                    <div className={`TextMedium ${styles.CentreTop}`}>{centreTop}</div>
                    <div className={`TextUltraSmall ${styles.CentreBottom}`}>{centreBottom}</div>
                </div>
            </div>
        </>
    );
};
