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
package com.axelixlabs.axelix.master.service.convert;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.master.domain.HotSpot;
import com.axelixlabs.axelix.master.domain.InsightFeature;
import com.axelixlabs.axelix.master.domain.Insights;

/**
 * Converts {@link BasicDiscoveryMetadata.Insights} API DTOs into the master domain model.
 *
 * @author Mikhail Polivakha
 */
public final class InsightsConverter {

    private InsightsConverter() {}

    public static Insights fromApi(BasicDiscoveryMetadata.@Nullable Insights insights) {
        if (insights == null) {
            return Insights.empty();
        }

        return new Insights(fromHotSpot(insights.getHotSpot()), fromFeatures(insights.getSpringFramework()));
    }

    private static HotSpot fromHotSpot(BasicDiscoveryMetadata.@Nullable HotSpot hotSpot) {
        if (hotSpot == null) {
            return HotSpot.empty();
        }

        return new HotSpot(
                fromFeatures(hotSpot.getProjectLeyden()),
                fromFeatures(hotSpot.getGc()),
                fromFeatures(hotSpot.getProjectLilliputh()));
    }

    private static List<InsightFeature> fromFeatures(@Nullable List<BasicDiscoveryMetadata.InsightFeature> features) {
        if (features == null) {
            return List.of();
        }

        return features.stream()
                .map(feature -> new InsightFeature(feature.getName(), feature.isEnabled()))
                .toList();
    }
}
