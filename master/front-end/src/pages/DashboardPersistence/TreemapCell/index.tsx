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
import styles from "./styles.module.css";

interface IProps {
    x?: number;
    y?: number;
    width?: number;
    height?: number;
    fill: string;
    valueLabel: string;
}

export const TreemapCell = ({ x = 0, y = 0, width = 0, height = 0, fill, valueLabel }: IProps) => {
    if (width < 4 || height < 4) {
        return null;
    }

    const showCount = width > 64 && height > 44;

    return (
        <>
            <g>
                <rect
                    x={x + 1}
                    y={y + 1}
                    width={width - 2}
                    height={height - 2}
                    fill={fill}
                    stroke="#fff"
                    strokeWidth={2}
                />

                {showCount && (
                    <text
                        x={x + width / 2}
                        y={y + height / 2 + 10}
                        textAnchor="middle"
                        dominantBaseline="middle"
                        fill="#fff"
                        className={`TextUltraSmall ${styles.Label}`}
                    >
                        {valueLabel}
                    </text>
                )}
            </g>
        </>
    );
};
