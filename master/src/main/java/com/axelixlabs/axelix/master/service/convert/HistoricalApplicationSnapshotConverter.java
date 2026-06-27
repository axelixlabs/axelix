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

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.InsightFeature;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.Insights;
import com.axelixlabs.axelix.master.domain.Insights.HotSpot;
import com.axelixlabs.axelix.master.domain.Insights.HotSpot.GarbageCollector;
import com.axelixlabs.axelix.master.domain.Insights.HotSpot.ProjectLeyden;
import com.axelixlabs.axelix.master.domain.Insights.HotSpot.ProjectLilliput;
import com.axelixlabs.axelix.master.domain.Insights.SpringFramework;

@Component
public class HistoricalApplicationSnapshotConverter {

    private static final String APP_CDS = "AppCDS";
    private static final String AOT_CACHE = "AotCache";
    private static final String COMPACT_OBJECT_HEADERS = "CompactObjectHeaders";
    private static final String OSIV = "OSIV";
    private static final String GC_LOGGING_ENABLED = "GCLoggingEnabled";

    public HistoricalApplicationSnapshot currentSnapshot(BasicDiscoveryMetadata metadata) {

        return new HistoricalApplicationSnapshot(
                new SnapshotId(metadata.getGroupId(), metadata.getArtifactId(), LocalDate.now(ZoneOffset.UTC)),
                fromDto(metadata.getInsights()));
    }

    // TODO: nullability checks here are performed solely because we have not yet covered BasicDiscoveryMetadata with
    // nullability annotations.
    private Insights fromDto(BasicDiscoveryMetadata.Insights insights) {
        if (insights == null) {
            return defaultInsights();
        }

        return new Insights(fromHotSpot(insights.getHotSpot()), fromSpringFramework(insights.getSpringFramework()));
    }

    private HotSpot fromHotSpot(BasicDiscoveryMetadata.HotSpot hotSpot) {
        if (hotSpot == null) {
            return defaultHotSpot();
        }

        return new HotSpot(
                fromProjectLeyden(hotSpot.getProjectLeyden()),
                fromGarbageCollector(hotSpot.getGc()),
                fromProjectLilliput(hotSpot.getProjectLilliputh()));
    }

    private ProjectLeyden fromProjectLeyden(List<InsightFeature> features) {
        return new ProjectLeyden(isFeatureEnabled(features, APP_CDS), isFeatureEnabled(features, AOT_CACHE));
    }

    private GarbageCollector fromGarbageCollector(List<InsightFeature> features) {
        return new GarbageCollector(isFeatureEnabled(features, GC_LOGGING_ENABLED), resolveGcInUse(features));
    }

    private ProjectLilliput fromProjectLilliput(List<InsightFeature> features) {
        return new ProjectLilliput(isFeatureEnabled(features, COMPACT_OBJECT_HEADERS));
    }

    private SpringFramework fromSpringFramework(List<InsightFeature> features) {
        return new SpringFramework(isFeatureEnabled(features, OSIV));
    }

    private boolean isFeatureEnabled(List<InsightFeature> features, String name) {
        if (features == null) {
            return false;
        }

        return features.stream()
                .filter(feature -> name.equals(feature.getName()))
                .findFirst()
                .map(InsightFeature::isEnabled)
                .orElse(false);
    }

    private String resolveGcInUse(List<InsightFeature> features) {
        return "TODO"; // TODO: we do not ship it in there
    }

    private Insights defaultInsights() {
        return new Insights(defaultHotSpot(), new SpringFramework(false));
    }

    private HotSpot defaultHotSpot() {
        return new HotSpot(
                new ProjectLeyden(false, false), new GarbageCollector(false, ""), new ProjectLilliput(false));
    }
}
