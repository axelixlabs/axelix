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
interface IProps {
    cx: number;
    cy: number;
    radius: number;
    thickness: number;
    startDeg: number;
    endDeg: number;
    color: string;
}

const toRadians = (degrees: number): number => {
    return (degrees * Math.PI) / 180;
};

export const ArcSegment = ({ cx, cy, radius, thickness, startDeg, endDeg, color }: IProps) => {
    const outerRadius = radius;
    const innerRadius = radius - thickness;

    const startAngle = toRadians(startDeg);
    const endAngle = toRadians(endDeg);

    const largeArcFlag = Math.abs(endDeg - startDeg) > 180 ? 1 : 0;
    const sweepFlag = endDeg > startDeg ? 1 : 0;

    const outerStartX = cx + outerRadius * Math.cos(startAngle);
    const outerStartY = cy + outerRadius * Math.sin(startAngle);
    const outerEndX = cx + outerRadius * Math.cos(endAngle);
    const outerEndY = cy + outerRadius * Math.sin(endAngle);

    const innerEndX = cx + innerRadius * Math.cos(endAngle);
    const innerEndY = cy + innerRadius * Math.sin(endAngle);
    const innerStartX = cx + innerRadius * Math.cos(startAngle);
    const innerStartY = cy + innerRadius * Math.sin(startAngle);

    const d = [
        `M ${outerStartX} ${outerStartY}`,
        `A ${outerRadius} ${outerRadius} 0 ${largeArcFlag} ${sweepFlag} ${outerEndX} ${outerEndY}`,
        `L ${innerEndX} ${innerEndY}`,
        `A ${innerRadius} ${innerRadius} 0 ${largeArcFlag} ${1 - sweepFlag} ${innerStartX} ${innerStartY}`,
        "Z",
    ].join(" ");

    return <path d={d} fill={color} />;
};
