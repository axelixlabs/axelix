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
import { ESupportStatus, type TProblemSupportStatus } from "@/models";
import { palette } from "@/theme/tokens";

/**
 * The colors of the support statuses in the dependency health charts.
 */
export const dependencyHealthColor: Record<TProblemSupportStatus, string> = {
    [ESupportStatus.SUNSET]: palette.red[500],
    [ESupportStatus.MAINTENANCE]: palette.orange[400],
    [ESupportStatus.DORMANT]: palette.purple[400],
};

/**
 * The color of the applications that use none of the problematic libraries.
 */
export const DEPENDENCY_HEALTH_NONE_COLOR = "#2DD4BF";

export const DEPENDENCY_HEALTH_PAGE_SIZE = 10;

/**
 * The colors of the version bars of a library, the newest version first. The versions past the end of the ramp
 * reuse its last color.
 */
export const DEPENDENCY_VERSION_COLORS = [
    palette.green[500],
    palette.grey[600],
    palette.grey[500],
    palette.grey[400],
    palette.grey[300],
    palette.grey[200],
];

/**
 * How many of the newest versions of a library are shown before the rest is folded.
 */
export const DEPENDENCY_VERSIONS_SHOWN = 4;

/**
 * @returns the status of a library when it is a risky one, {@code null} for an active or an uncurated library
 */
export const riskyStatusOf = (status: ESupportStatus | null): TProblemSupportStatus | null => {
    return status !== null && status !== ESupportStatus.ACTIVE ? status : null;
};
