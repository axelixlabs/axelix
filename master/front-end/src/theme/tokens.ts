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

/**
 * The single source of the console's colors and typography. The antd theme reads these values
 * directly, and {@link applyDesignTokens} exposes them to CSS as `--axelix-*` custom properties,
 * e.g. `palette.green[600]` becomes `--axelix-green-600`, `palette.white` becomes `--axelix-white`
 * and `typography.fontSize.body` becomes `--axelix-font-size-body`.
 */
export const palette = {
    white: "#fff",
    // Text on white: 900 primary, 600 secondary, 500 tertiary (all WCAG AA); 400 placeholders/disabled only
    grey: {
        25: "#fcfcfd",
        50: "#f9fafb",
        100: "#f2f4f7",
        200: "#eaecf0",
        300: "#d0d5dd",
        400: "#98a2b3",
        500: "#667085",
        600: "#475467",
        700: "#344054",
        800: "#1d2939",
        900: "#101828",
    },
    // Brand / success. 500 is the brand green for decoration only; 600 is the AA-compliant shade for actions, links and text
    green: {
        50: "#e6f7ee",
        100: "#ccefdd",
        200: "#99dfbb",
        500: "#00ab55",
        600: "#008743",
        700: "#007038",
    },
    red: {
        50: "#fef3f2",
        100: "#fee4e2",
        200: "#fecdca",
        300: "#fda29b",
        500: "#f04438",
        600: "#d92d20",
        700: "#b42318",
    },
    orange: {
        50: "#fffaeb",
        100: "#fef0c7",
        200: "#fedf89",
        300: "#fec84b",
        400: "#fdb022",
        500: "#f79009",
        700: "#b54708",
    },
    // A lower-severity warning than orange, e.g. the "warn" level of the dependency analysis
    yellow: {
        50: "#fefbe8",
        200: "#fde68a",
        300: "#fde047",
        800: "#854a0e",
    },
    blue: {
        50: "#eff8ff",
        100: "#d1e9ff",
        200: "#b2ddff",
        400: "#53b1fd",
        500: "#2e90fa",
        600: "#1570ef",
        700: "#175cd3",
    },
} as const;

// The font sizes mirror the scale antd derives from the 14px base
export const typography = {
    fontFamily: "Golos, Helvetica, Arial, sans-serif",
    fontSize: {
        caption: 12,
        body: 14,
        subtitle: 16,
        title: 20,
        "page-title": 24,
        display: 30,
    },
    lineHeight: {
        tight: 1.25,
        normal: 1.5,
    },
    fontWeight: {
        regular: 400,
        medium: 500,
        semibold: 600,
    },
} as const;

export const applyDesignTokens = (style: Pick<CSSStyleDeclaration, "setProperty">): void => {
    for (const [hue, shades] of Object.entries(palette)) {
        if (typeof shades === "string") {
            style.setProperty(`--axelix-${hue}`, shades);
            continue;
        }

        for (const [shade, color] of Object.entries(shades)) {
            style.setProperty(`--axelix-${hue}-${shade}`, color);
        }
    }

    style.setProperty("--axelix-font-family", typography.fontFamily);

    for (const [role, size] of Object.entries(typography.fontSize)) {
        style.setProperty(`--axelix-font-size-${role}`, `${size}px`);
    }

    for (const [name, lineHeight] of Object.entries(typography.lineHeight)) {
        style.setProperty(`--axelix-line-height-${name}`, String(lineHeight));
    }

    for (const [name, weight] of Object.entries(typography.fontWeight)) {
        style.setProperty(`--axelix-font-weight-${name}`, String(weight));
    }
};
