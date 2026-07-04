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
import java.util.Map;

import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;

/**
 * The aggregated info about the overall Java/JVM features usage in the ecosystem.
 *
 * @param projectLeyden the list of aggregated Project Leyden features.
 * @param gc the list of aggregated GC features.
 * @param garbageCollectorDistribution the distribution of garbage collectors used by services in the ecosystem.
 * @param projectLilliput the list of aggregated features inside Project Lilliput.
 *
 * @author Mikhail Polivakha
 */
public record JavaDashboardResponse(
        List<AggregatedFeature> projectLeyden,
        List<AggregatedFeature> gc,
        Map<GarbageCollector, Double> garbageCollectorDistribution,
        List<AggregatedFeature> projectLilliput) {}
