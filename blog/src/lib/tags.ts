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

export const SHOW_ALL = "all";

const DEFAULT_TAG_COLOR = "#8FBD4B";

const PALETTE = [
    "#6db33f",
    "#c2487f",
    "#2aa39a",
    "#d98e2b",
    "#7c5cd6",
    "#3f86c4",
    "#c0563a",
    "#c0392b",
    "#b14b8a",
    "#2f8f9e",
    "#9c5cc2",
    "#c98a2b",
    "#5f6b7a",
];

export function getColorForTag(name: string): string {
    const code = name.trim().toUpperCase().charCodeAt(0);
    if (code >= 65 && code <= 90) return PALETTE[Math.floor((code - 65) / 2)];
    return DEFAULT_TAG_COLOR;
}
