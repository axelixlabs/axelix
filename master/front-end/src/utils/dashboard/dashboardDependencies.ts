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
 * The color of the services that use none of the problematic projects.
 */
export const DEPENDENCY_HEALTH_NONE_COLOR = "#2DD4BF";

export const DEPENDENCY_HEALTH_PAGE_SIZE = 10;
