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
package com.axelixlabs.axelix.master.utils;

import java.util.List;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.domain.insights.FeatureId;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;

/**
 * Test fixture factory for {@link BasicDiscoveryMetadata}.
 *
 * @author Mikhail Polivakha
 */
public final class TestMetadataFactory {

    private static final String DEFAULT_VERSION = "1.0.0-SNAPSHOT";

    private static final String DEFAULT_SERVICE_VERSION = "3.5.0-SNAPSHOT";

    private static final String DEFAULT_COMMIT_SHORT_SHA = "a8b0929";

    private static final String DEFAULT_JDK_VENDOR = "BellSoft";

    private static final long DEFAULT_HEAP = 12_000;

    private TestMetadataFactory() {}

    public static BasicDiscoveryMetadata create(String groupId, String artifactId) {
        return create(groupId, artifactId, GarbageCollector.G1);
    }

    public static BasicDiscoveryMetadata create(String groupId, String artifactId, GarbageCollector garbageCollector) {
        return withFeatures(groupId, artifactId, true, false, false, false, true, garbageCollector);
    }

    public static BasicDiscoveryMetadata withFeatures(
            String groupId,
            String artifactId,
            boolean appCdsEnabled,
            boolean aotCacheEnabled,
            boolean gcLoggingEnabled,
            boolean compactObjectHeadersEnabled,
            boolean osivEnabled) {
        return withFeatures(
                groupId,
                artifactId,
                appCdsEnabled,
                aotCacheEnabled,
                gcLoggingEnabled,
                compactObjectHeadersEnabled,
                osivEnabled,
                GarbageCollector.G1);
    }

    public static BasicDiscoveryMetadata withFeatures(
            String groupId,
            String artifactId,
            boolean appCdsEnabled,
            boolean aotCacheEnabled,
            boolean gcLoggingEnabled,
            boolean compactObjectHeadersEnabled,
            boolean osivEnabled,
            GarbageCollector garbageCollector) {
        BasicDiscoveryMetadata.SoftwareVersions softwareVersions =
                new BasicDiscoveryMetadata.SoftwareVersions("25", "3.5.0", "6.1.2", null);

        return new BasicDiscoveryMetadata(
                DEFAULT_VERSION,
                DEFAULT_SERVICE_VERSION,
                groupId,
                artifactId,
                DEFAULT_COMMIT_SHORT_SHA,
                DEFAULT_JDK_VENDOR,
                garbageCollector,
                softwareVersions,
                BasicDiscoveryMetadata.HealthStatus.UP,
                new BasicDiscoveryMetadata.MemoryDetails(DEFAULT_HEAP),
                insights(appCdsEnabled, aotCacheEnabled, gcLoggingEnabled, compactObjectHeadersEnabled, osivEnabled));
    }

    private static BasicDiscoveryMetadata.Insights insights(
            boolean appCdsEnabled,
            boolean aotCacheEnabled,
            boolean gcLoggingEnabled,
            boolean compactObjectHeadersEnabled,
            boolean osivEnabled) {
        return new BasicDiscoveryMetadata.Insights(
                new BasicDiscoveryMetadata.HotSpot(
                        List.of(
                                feature(FeatureId.APP_CDS, appCdsEnabled),
                                feature(FeatureId.AOT_CACHE, aotCacheEnabled)),
                        List.of(feature(FeatureId.GC_LOGGING_ENABLED, gcLoggingEnabled)),
                        List.of(feature(FeatureId.COMPACT_OBJECT_HEADERS, compactObjectHeadersEnabled))),
                List.of(feature(FeatureId.OSIV, osivEnabled)));
    }

    private static BasicDiscoveryMetadata.InsightFeature feature(FeatureId featureId, boolean enabled) {
        return new BasicDiscoveryMetadata.InsightFeature(featureId.getId(), enabled);
    }
}
