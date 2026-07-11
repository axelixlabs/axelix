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
 * Insight information discovered for the given service instance.
 */
public final class Insights {

    private final HotSpot hotSpot;
    private final List<InsightFeature> springFramework;

    /**
     * Creates a new Insight.
     *
     * @param hotSpot         the HotSpot-specific insight groups.
     * @param springFramework the Spring Framework insight features.
     */
    @JsonCreator
    public Insights(
            @JsonProperty("hotSpot") HotSpot hotSpot,
            @JsonProperty("springFramework") List<InsightFeature> springFramework) {
        this.hotSpot = hotSpot;
        this.springFramework = springFramework;
    }

    public HotSpot getHotSpot() {
        return hotSpot;
    }

    public List<InsightFeature> getSpringFramework() {
        return springFramework;
    }

    @Override
    public String toString() {
        return "Insight{" + "hotSpot=" + hotSpot + ", springFramework=" + springFramework + '}';
    }
}
