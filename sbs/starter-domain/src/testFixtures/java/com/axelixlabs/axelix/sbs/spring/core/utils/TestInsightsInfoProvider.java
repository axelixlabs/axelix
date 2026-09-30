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
package com.axelixlabs.axelix.sbs.spring.core.utils;

import java.util.List;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.HotSpotInsights;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.InsightFeature;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.Insights;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.PersistenceInsights;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.InsightsInfoProvider;

/**
 * Test {@link InsightsInfoProvider} that returns a fixed insight payload.
 *
 * @author Mikhail Polivakha
 */
public final class TestInsightsInfoProvider implements InsightsInfoProvider {

    public static final Insights TEST_INSIGHTS = new Insights()
            .hotSpot(new HotSpotInsights()
                    .projectLeyden(List.of(
                            new InsightFeature().featureId("AppCDS").enabled(true),
                            new InsightFeature().featureId("AotCache").enabled(false)))
                    .gc(List.of(
                            new InsightFeature().featureId("GCLoggingEnabled").enabled(true),
                            new InsightFeature().featureId("GCLogFileSpecified").enabled(false)))
                    .projectLilliputh(List.of(new InsightFeature()
                            .featureId("CompactObjectHeaders")
                            .enabled(true))))
            .springFramework(List.of(new InsightFeature().featureId("OSIV").enabled(false)))
            .persistenceInsights(new PersistenceInsights().transactions(List.of()))
            .scheduledTaskExecutions(List.of());

    @Override
    public Insights getInsight() {
        return TEST_INSIGHTS;
    }
}
