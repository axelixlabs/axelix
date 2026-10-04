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
import org.springframework.util.Assert;

import com.axelixlabs.axelix.common.domain.insights.FeatureId;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.master.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.master.contract.metadata.HotSpotInsights;
import com.axelixlabs.axelix.master.contract.metadata.InsightFeature;
import com.axelixlabs.axelix.master.contract.metadata.Insights;
import com.axelixlabs.axelix.master.contract.metadata.PersistenceInsights;
import com.axelixlabs.axelix.master.contract.metadata.SoftwareVersions;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.Versions;
import com.axelixlabs.axelix.master.domain.Insights.HotSpot;
import com.axelixlabs.axelix.master.domain.Insights.HotSpot.ProjectLeyden;
import com.axelixlabs.axelix.master.domain.Insights.HotSpot.ProjectLilliput;
import com.axelixlabs.axelix.master.domain.Insights.SpringFramework;
import com.axelixlabs.axelix.master.domain.JavaVersion;
import com.axelixlabs.axelix.master.domain.JdkVendor;

/**
 * Converter that is capable to conver the {@link BasicRegistrationMetadata} into {@link HistoricalApplicationSnapshot}.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@Component
public class HistoricalApplicationSnapshotConverter {

    public HistoricalApplicationSnapshot currentSnapshot(BasicRegistrationMetadata metadata) {

        return new HistoricalApplicationSnapshot(
                new SnapshotId(metadata.getGroupId(), metadata.getArtifactId(), LocalDate.now(ZoneOffset.UTC)),
                fromDto(metadata),
                fromVersions(metadata));
    }

    private Versions fromVersions(BasicRegistrationMetadata metadata) {
        SoftwareVersions softwareVersions = metadata.getSoftwareVersions();
        Assert.notNull(softwareVersions, "Cannot build a historical snapshot without the reported software versions");
        return new Versions(
                metadata.getVersion(),
                softwareVersions.getSpringBoot(),
                softwareVersions.getSpringFramework(),
                JavaVersion.parse(softwareVersions.getJava()),
                JdkVendor.fromVendorName(metadata.getJdkVendor()),
                softwareVersions.getKotlin());
    }

    private com.axelixlabs.axelix.master.domain.Insights fromDto(BasicRegistrationMetadata metadata) {
        GarbageCollector gcInUse = resolveGcInUse(metadata.getGcInUse());

        Insights insights = metadata.getInsights();
        if (insights == null) {
            return defaultInsights(gcInUse);
        }

        return new com.axelixlabs.axelix.master.domain.Insights(
                fromHotSpot(insights.getHotSpot(), gcInUse),
                fromSpringFramework(insights.getSpringFramework()),
                fromPersistenceInsights(insights.getPersistenceInsights()));
    }

    private GarbageCollector resolveGcInUse(com.axelixlabs.axelix.master.contract.metadata.GarbageCollector gcInUse) {
        return gcInUse == null ? GarbageCollector.UNKNOWN : GarbageCollector.valueOf(gcInUse.name());
    }

    private PersistenceInsights fromPersistenceInsights(PersistenceInsights persistenceInsights) {
        if (persistenceInsights == null || persistenceInsights.getTransactions() == null) {
            return new PersistenceInsights().transactions(List.of());
        }
        return persistenceInsights;
    }

    private HotSpot fromHotSpot(HotSpotInsights hotSpotInsights, GarbageCollector gcInUse) {
        if (hotSpotInsights == null) {
            return defaultHotSpot(gcInUse);
        }

        return new HotSpot(
                fromProjectLeyden(hotSpotInsights.getProjectLeyden()),
                fromGarbageCollector(hotSpotInsights.getGc(), gcInUse),
                fromProjectLilliput(hotSpotInsights.getProjectLilliputh()));
    }

    private ProjectLeyden fromProjectLeyden(List<InsightFeature> features) {
        return new ProjectLeyden(
                isFeatureEnabled(features, FeatureId.APP_CDS), isFeatureEnabled(features, FeatureId.AOT_CACHE));
    }

    private HotSpot.GarbageCollector fromGarbageCollector(List<InsightFeature> features, GarbageCollector gcInUse) {
        return new HotSpot.GarbageCollector(isFeatureEnabled(features, FeatureId.GC_LOGGING_ENABLED), gcInUse);
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
                .map(InsightFeature::getEnabled)
                .orElse(false);
    }

    private com.axelixlabs.axelix.master.domain.Insights defaultInsights(GarbageCollector gcInUse) {
        return new com.axelixlabs.axelix.master.domain.Insights(
                defaultHotSpot(gcInUse), new SpringFramework(false), new PersistenceInsights().transactions(List.of()));
    }

    private HotSpot defaultHotSpot(GarbageCollector gcInUse) {
        return new HotSpot(
                new ProjectLeyden(false, false),
                new HotSpot.GarbageCollector(false, gcInUse),
                new ProjectLilliput(false));
    }
}
