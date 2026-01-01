/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { EInstanceStatus } from "models";

export const HEALTH_STATUSES_COLORS: Record<EInstanceStatus, string> = {
    [EInstanceStatus.UP]: "#00ab55",
    [EInstanceStatus.DOWN]: "#ff000a",
    [EInstanceStatus.UNKNOWN]: "#838383",
};

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
