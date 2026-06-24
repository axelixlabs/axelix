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
package com.axelixlabs.axelix.sbs.spring.core.master.insights;

import java.util.List;

import com.axelixlabs.axelix.common.api.gclog.GcLogStatus;
import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.HotSpot;
import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.InsightFeature;
import com.axelixlabs.axelix.sbs.spring.core.gclog.GcLogService;
import com.axelixlabs.axelix.sbs.spring.core.master.OpenSessionInViewStateProvider;

import static com.axelixlabs.axelix.sbs.spring.core.master.insights.WellKnownVmOptions.AOT_CACHE_OPTION;
import static com.axelixlabs.axelix.sbs.spring.core.master.insights.WellKnownVmOptions.SHARED_ARCHIVE_FILE;
import static com.axelixlabs.axelix.sbs.spring.core.master.insights.WellKnownVmOptions.USE_COMPACT_OBJECT_HEADERS;

/**
 * Default {@link InsightsInfoProvider} based on the current service runtime configuration.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public class DefaultInsightsInfoProvider implements InsightsInfoProvider {

    public static final String APP_CDS = "AppCDS";
    public static final String AOT_CACHE = "AotCache";
    public static final String COMPACT_OBJECT_HEADERS = "CompactObjectHeaders";
    public static final String OSIV = "OSIV";
    public static final String GC_LOGGING_ENABLED = "GCLoggingEnabled";
    public static final String GC_LOG_FILE_SPECIFIED = "GCLogFileSpecified";

    private final OpenSessionInViewStateProvider openSessionInViewStateProvider;

    private final GcLogService gcLogService;
    private final VmOptionsAccessor vmOptionsAccessor;

    /**
     * Creates a new DefaultInsightsInfoProvider.
     *
     * @param openSessionInViewStateProvider provider of the Spring Open Session in View state.
     */
    public DefaultInsightsInfoProvider(
            OpenSessionInViewStateProvider openSessionInViewStateProvider,
            GcLogService gcLogService,
            VmOptionsAccessor vmOptionsAccessor) {
        this.openSessionInViewStateProvider = openSessionInViewStateProvider;
        this.gcLogService = gcLogService;
        this.vmOptionsAccessor = vmOptionsAccessor;
    }

    @Override
    public BasicDiscoveryMetadata.Insights getInsight() {
        GcLogStatus gcLogStatus = gcLogService.getStatus();

        return new BasicDiscoveryMetadata.Insights(
                new HotSpot(
                        List.of(getAppCdsFeature(), getAotCacheFeature()),
                        List.of(getGcLoggingFeature(gcLogStatus), getGcLogFileSpecifiedFeature()),
                        List.of(getCompressedObjectHeadersFeature())),
                List.of(new InsightFeature(OSIV, openSessionInViewStateProvider.isOpenSessionInViewEnabled())));
    }

    private InsightFeature getAppCdsFeature() {
        return new InsightFeature(APP_CDS, vmOptionsAccessor.isAdvancedFeatureSpecified(SHARED_ARCHIVE_FILE));
    }

    private InsightFeature getAotCacheFeature() {
        return new InsightFeature(AOT_CACHE, vmOptionsAccessor.isAdvancedFeatureSpecified(AOT_CACHE_OPTION));
    }

    private InsightFeature getCompressedObjectHeadersFeature() {
        return new InsightFeature(
                COMPACT_OBJECT_HEADERS, vmOptionsAccessor.isAdvancedFeatureEnabled(USE_COMPACT_OBJECT_HEADERS));
    }

    private InsightFeature getGcLoggingFeature(GcLogStatus gcLogStatus) {
        return new InsightFeature(GC_LOGGING_ENABLED, gcLogStatus.isEnabled());
    }

    private InsightFeature getGcLogFileSpecifiedFeature() {
        return new InsightFeature(GC_LOG_FILE_SPECIFIED, gcLogService.isGcLogFileSpecified());
    }
}
