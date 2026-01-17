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
import { EInstanceStatus } from "models";

export const HEALTH_STATUSES_COLORS: Record<EInstanceStatus, string> = {
    [EInstanceStatus.UP]: "#00ab55",
    [EInstanceStatus.DOWN]: "#ff000a",
    [EInstanceStatus.UNKNOWN]: "#838383",
};

/**
 * Constant that represents how many radians are in the single degree.
 */
export const RADIAN = Math.PI / 180;

const PIE_CHARTS_COLORS_PALETTE = ["#0366a6", "#9061aa", "#d68026", "#ffff00", "#006600"];

/**
 * Cache that stores the mapping between dashboard software component versions and their colors on the pie charts.
 */
const colorsCache: Map<string, Map<string, string>> = new Map();

const colorIndexesCache: Map<string, number> = new Map();

/**
 * Function that accepts the name of the software component version and returns the hex color for it on the pie chart.
 */
export const pickColor = (componentName: string, componentVersion: string) => {
    let componentColors = colorsCache.get(componentName);

    if (!componentColors) {
        componentColors = new Map();
        colorsCache.set(componentName, componentColors);
    }

    let colorForVersion = componentColors.get(componentVersion);

    if (!colorForVersion) {
        const colorIndex = colorIndexesCache.get(componentName) ?? 0;
        colorIndexesCache.set(componentName, colorIndex + 1);

        colorForVersion = PIE_CHARTS_COLORS_PALETTE[colorIndex];
        componentColors.set(componentVersion, colorForVersion);
    }

    return colorForVersion;
};
