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
import com.axelixlabs.axelix.common.domain.insights.FeatureId;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.Insights;
import com.axelixlabs.axelix.master.domain.Insights.HotSpot;
import com.axelixlabs.axelix.master.domain.Insights.HotSpot.ProjectLeyden;
import com.axelixlabs.axelix.master.domain.Insights.HotSpot.ProjectLilliput;
import com.axelixlabs.axelix.master.domain.Insights.SpringFramework;

/**
 * Converter that is capable to conver the {@link BasicDiscoveryMetadata} into {@link HistoricalApplicationSnapshot}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class HistoricalApplicationSnapshotConverter {

    public HistoricalApplicationSnapshot currentSnapshot(BasicDiscoveryMetadata metadata) {

        return new HistoricalApplicationSnapshot(
                new SnapshotId(metadata.getGroupId(), metadata.getArtifactId(), LocalDate.now(ZoneOffset.UTC)),
                fromDto(metadata));
    }

    // TODO: nullability checks here are performed solely because we have not yet covered BasicDiscoveryMetadata with
    // nullability annotations.
    private Insights fromDto(BasicDiscoveryMetadata metadata) {
        BasicDiscoveryMetadata.Insights insights = metadata.getInsights();
        if (insights == null) {
            return defaultInsights();
        }

        return new Insights(
                fromHotSpot(insights.getHotSpot(), metadata.getGcInUse()),
                fromSpringFramework(insights.getSpringFramework()));
    }

    private HotSpot fromHotSpot(BasicDiscoveryMetadata.HotSpot hotSpot, GarbageCollector gcInUse) {
        if (hotSpot == null) {
            return defaultHotSpot();
        }

        return new HotSpot(
                fromProjectLeyden(hotSpot.getProjectLeyden()),
                fromGarbageCollector(hotSpot.getGc(), gcInUse),
                fromProjectLilliput(hotSpot.getProjectLilliputh()));
    }

    private ProjectLeyden fromProjectLeyden(List<InsightFeature> features) {
        return new ProjectLeyden(
                isFeatureEnabled(features, FeatureId.APP_CDS), isFeatureEnabled(features, FeatureId.AOT_CACHE));
    }

    private HotSpot.GarbageCollector fromGarbageCollector(List<InsightFeature> features, GarbageCollector gcInUse) {
        return new HotSpot.GarbageCollector(
                isFeatureEnabled(features, FeatureId.GC_LOGGING_ENABLED), resolveGcInUse(gcInUse));
    }

    private ProjectLilliput fromProjectLilliput(List<InsightFeature> features) {
        return new ProjectLilliput(isFeatureEnabled(features, FeatureId.COMPACT_OBJECT_HEADERS));
    }

    private SpringFramework fromSpringFramework(List<InsightFeature> features) {
        return new SpringFramework(isFeatureEnabled(features, FeatureId.OSIV));
    }

    private boolean isFeatureEnabled(List<InsightFeature> features, FeatureId featureId) {
        if (features == null) {
            return false;
        }

        return features.stream()
                .filter(feature -> featureId.getId().equals(feature.getFeatureId()))
                .findFirst()
                .map(InsightFeature::isEnabled)
                .orElse(false);
    }

    private GarbageCollector resolveGcInUse(GarbageCollector gcInUse) {
        return gcInUse == null ? GarbageCollector.UNKNOWN : gcInUse;
    }

    private Insights defaultInsights() {
        return new Insights(defaultHotSpot(), new SpringFramework(false));
    }

    private HotSpot defaultHotSpot() {
        return new HotSpot(
                new ProjectLeyden(false, false),
                new HotSpot.GarbageCollector(false, GarbageCollector.UNKNOWN),
                new ProjectLilliput(false));
    }
}
