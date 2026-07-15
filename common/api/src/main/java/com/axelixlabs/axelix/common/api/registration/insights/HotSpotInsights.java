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
package com.axelixlabs.axelix.common.api.registration.insights;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * HotSpot-specific insight groups.
 */
public final class HotSpotInsights {

    private final List<InsightFeature> projectLeyden;
    private final List<InsightFeature> gc;
    private final List<InsightFeature> projectLilliputh;

    /**
     * Creates a new HotSpot.
     *
     * @param projectLeyden    the Project Leyden insight features.
     * @param gc               the garbage collection insight features.
     * @param projectLilliputh the Project Lilliputh insight features.
     */
    @JsonCreator
    public HotSpotInsights(
            @JsonProperty("projectLeyden") List<InsightFeature> projectLeyden,
            @JsonProperty("gc") List<InsightFeature> gc,
            @JsonProperty("projectLilliputh") List<InsightFeature> projectLilliputh) {
        this.projectLeyden = projectLeyden;
        this.gc = gc;
        this.projectLilliputh = projectLilliputh;
    }

    public List<InsightFeature> getProjectLeyden() {
        return projectLeyden;
    }

    public List<InsightFeature> getGc() {
        return gc;
    }

    public List<InsightFeature> getProjectLilliputh() {
        return projectLilliputh;
    }

    @Override
    public String toString() {
        return "HotSpot{"
                + "projectLeyden="
                + projectLeyden
                + ", gc="
                + gc
                + ", projectLilliputh="
                + projectLilliputh
                + '}';
    }
}
