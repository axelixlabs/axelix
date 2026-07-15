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

import com.axelixlabs.axelix.common.api.registration.insights.HotSpotInsights;
import com.axelixlabs.axelix.common.api.registration.insights.InsightFeature;
import com.axelixlabs.axelix.common.api.registration.insights.Insights;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.InsightsInfoProvider;

/**
 * Test {@link InsightsInfoProvider} that returns a fixed insight payload.
 *
 * @author Mikhail Polivakha
 */
// TODO: This is the candidate for the future to-be-created common-test module
public final class TestInsightsInfoProvider implements InsightsInfoProvider {

    public static final Insights TEST_INSIGHTS = new Insights(
            new HotSpotInsights(
                    List.of(new InsightFeature("AppCDS", true), new InsightFeature("AotCache", false)),
                    List.of(
                            new InsightFeature("GCLoggingEnabled", true),
                            new InsightFeature("GCLogFileSpecified", false)),
                    List.of(new InsightFeature("CompactObjectHeaders", true))),
            List.of(new InsightFeature("OSIV", false)),
            new PersistenceInsights(List.of()));

    @Override
    public Insights getInsight() {
        return TEST_INSIGHTS;
    }
}
