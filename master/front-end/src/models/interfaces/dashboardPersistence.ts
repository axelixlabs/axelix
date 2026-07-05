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
// TODO: Maybe we can improve these types
export interface IDashboardTreemapEntity {
    /**
     * Displayable name of the application.
     */
    appName: string;

    /**
     * The size/count of the problem (e.g. count of N + 1).
     */
    size: number;
}

export interface IDashboardPersistenceStatsData {
    label: string;
    value: string;
    color: string;
}

export interface IDashboardPersistenceResponse {
    /**
     * The state of the N + 1 Problem in the ecosystem.
     */
    nPlusOne: IDashboardTreemapEntity[];

    /**
     * The state of the In memory pagination in the ecosystem.
     */
    inMemoryPagination: IDashboardTreemapEntity[];
}
