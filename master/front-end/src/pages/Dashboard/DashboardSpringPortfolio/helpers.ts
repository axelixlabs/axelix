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
import type { IPlatformDistribution, IPlatformName } from "@/models";

/**
 * Categorical palette used to keep each release line identifiable across the donut
 * arc and its legend row. Colours are assigned per card in line order.
 */
export const LINE_COLORS = [
    "#2EC4B6",
    "#60A5FA",
    "#A78BFA",
    "#F5A623",
    "#F472B6",
    "#34D399",
    "#FB923C",
    "#818CF8",
    "#E879F9",
    "#22D3EE",
];

export const platformFullName = (platform: IPlatformName): string => {
    return platform === "SPRING_BOOT" ? "Spring Boot" : "Spring Framework";
};

export const platformShortName = (platform: IPlatformName): string => {
    return platform === "SPRING_BOOT" ? "Boot" : "Framework";
};

/**
 * Builds a stable {@code line -> colour} map for a distribution, walking majors and
 * their lines in the order the backend returns them (newest first).
 */
export const buildLineColorMap = (distribution: IPlatformDistribution): Map<string, string> => {
    const colors = new Map<string, string>();
    let index = 0;

    for (const major of distribution.majors) {
        for (const line of major.lines) {
            colors.set(line.line, LINE_COLORS[index % LINE_COLORS.length]);
            index += 1;
        }
    }

    return colors;
};

/**
 * Parses an ISO {@code YYYY-MM-DD} date coming from the Master backend. Falls back to
 * an array form ({@code [year, month, day]}) in case Jackson is configured to emit dates
 * as timestamps.
 */
export const parseIsoDate = (value: string | number[]): Date => {
    if (Array.isArray(value)) {
        const [year, month, day] = value;
        return new Date(year, month - 1, day);
    }

    return new Date(`${value}T00:00:00`);
};

/**
 * Formats a maintenance-window date the way the design renders it, e.g. {@code "Jun 2027"}.
 */
export const formatMonthYear = (value: string | number[]): string => {
    return parseIsoDate(value).toLocaleDateString("en-US", { month: "short", year: "numeric" });
};
