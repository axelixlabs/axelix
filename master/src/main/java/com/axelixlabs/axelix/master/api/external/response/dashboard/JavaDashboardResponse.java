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
 * The aggregated info about the overall features usage in the ecosystem.
 *
 * @author Mikhail Polivakha
 */
public record JavaDashboardResponse(HotSpot hotSpot, List<AggregatedFeature> springFramework) {

    /**
     * The aggregated HotSpot VM-specific features usage statistics.
     *
     * @param projectLeyden the list of aggregated project leyden features .
     * @param gc the list of aggregated GC features.
     * @param projectLilliput the list of aggregated features inside project Lilliput
     */
    public record HotSpot(
            List<AggregatedFeature> projectLeyden,
            List<AggregatedFeature> gc,
            List<AggregatedFeature> projectLilliput) {}

    /**
     * @param featureId the id of the feature in use.
     * @param adoptionPercentage percentage of the total services that use the feature with the given featureId.
     */
    public record AggregatedFeature(String featureId, double adoptionPercentage) {}
}
