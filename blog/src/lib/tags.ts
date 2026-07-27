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

/** Sentinel used by the home filter for the "no filter" state. */
export const SHOW_ALL = "all";

/** Fallback when a tag doesn't start with an A–Z letter. */
const DEFAULT_TAG_COLOR = "#8FBD4B";

/** One color per 2-letter bucket: A-B, C-D, …, W-X, Y-Z (13 buckets). */
const PALETTE = [
    "#6db33f", // A-B  green
    "#c2487f", // C-D  magenta
    "#2aa39a", // E-F  teal
    "#d98e2b", // G-H  amber
    "#7c5cd6", // I-J  purple
    "#3f86c4", // K-L  blue
    "#c0563a", // M-N  terracotta
    "#c0392b", // O-P  red
    "#b14b8a", // Q-R  orchid
    "#2f8f9e", // S-T  cyan
    "#9c5cc2", // U-V  violet
    "#c98a2b", // W-X  ochre
    "#5f6b7a", // Y-Z  slate
];

/** Stable chip color for a tag, by its first letter's 2-letter bucket. */
export function colorForTag(name: string): string {
    const code = name.trim().toUpperCase().charCodeAt(0); // A=65 … Z=90
    if (code >= 65 && code <= 90) return PALETTE[Math.floor((code - 65) / 2)];
    return DEFAULT_TAG_COLOR;
}
