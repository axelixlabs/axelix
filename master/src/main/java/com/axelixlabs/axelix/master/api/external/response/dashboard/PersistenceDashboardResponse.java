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
package com.axelixlabs.axelix.master.api.external.response.dashboard;

import java.util.List;

/**
 * The aggregated info about the overall persistence problems in the ecosystem. Every entry corresponds to a single
 * service, so the front-end can render a per-service treemap of the problem magnitude.
 *
 * @param nPlusOne per-service number of N + 1 occasions across the ecosystem.
 * @param inMemoryPagination per-service number of in-memory pagination occasions across the ecosystem.
 *
 * @author Mikhail Polivakha
 */
public record PersistenceDashboardResponse(List<TreemapEntry> nPlusOne, List<TreemapEntry> inMemoryPagination) {

    /**
     * A single treemap cell: how many distinct occasions of a given problem a single service has.
     *
     * @param appName the displayable name of the application (its {@code artifactId}).
     * @param size the number of affected associations / queries for this service (each counts once, regardless of
     *     how many times it occurred).
     */
    public record TreemapEntry(String appName, int size) {}
}
