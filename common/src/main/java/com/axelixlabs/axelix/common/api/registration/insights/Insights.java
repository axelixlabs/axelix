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

import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;

/**
 * Insight information discovered for the given service instance.
 */
public final class Insights {

    private final HotSpotInsights hotSpotInsights;
    private final List<InsightFeature> springFramework;
    private final PersistenceInsights persistenceInsights;

    /**
     * Creates a new Insight.
     *
     * @param hotSpotInsights         the HotSpot-specific insight groups.
     * @param springFramework the Spring Framework insight features.
     */
    @JsonCreator
    public Insights(
            @JsonProperty("hotSpot") HotSpotInsights hotSpotInsights,
            @JsonProperty("springFramework") List<InsightFeature> springFramework,
            @JsonProperty("persistenceInsights") PersistenceInsights persistenceInsights) {
        this.hotSpotInsights = hotSpotInsights;
        this.springFramework = springFramework;
        this.persistenceInsights = persistenceInsights;
    }

    public HotSpotInsights getHotSpot() {
        return hotSpotInsights;
    }

    public List<InsightFeature> getSpringFramework() {
        return springFramework;
    }

    public PersistenceInsights getPersistenceInsights() {
        return persistenceInsights;
    }

    @Override
    public String toString() {
        return "Insights{" + "hotSpotInsights="
                + hotSpotInsights + ", springFramework="
                + springFramework + ", persistenceInsights="
                + persistenceInsights + '}';
    }
}
