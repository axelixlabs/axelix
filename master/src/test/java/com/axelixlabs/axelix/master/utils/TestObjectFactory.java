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

import java.time.Instant;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.domain.HotSpot;
import com.axelixlabs.axelix.master.domain.InsightFeature;
import com.axelixlabs.axelix.master.domain.Insights;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.MemoryUsage;

/**
 * Utility factory for creating test objects used in unit and integration tests.
 *
 * @since 29.08.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public final class TestObjectFactory {

    private static final String DEFAULT_URL = "http://example.com";

    private static final Instance.InstanceStatus DEFAULT_STATUS = Instance.InstanceStatus.UP;

    private TestObjectFactory() {}

    public static Instance createInstance(String id) {
        return withUrl(id, DEFAULT_URL);
    }

    public static Instance createInstance(String id, @Nullable Instant instant) {
        return createInstanceWithHeartbeat(id, instant);
    }

    public static Instance withName(String id, String name) {
        return createInstance(
                id, DEFAULT_URL, name, DEFAULT_STATUS, "25", "3.5.2", "6.0.2", "BellSoft", null, Insights.empty());
    }

    public static Instance withUrl(String id, String url) {
        return createInstance(id, url, DEFAULT_STATUS);
    }

    public static Instance createInstance(String id, String url) {
        return withUrl(id, url);
    }

    public static Instance withStatus(String id, Instance.InstanceStatus status) {
        return createInstance(id, DEFAULT_URL, status);
    }

    public static Instance createInstance(
            String id,
            String java,
            String springBoot,
            String springFramework,
            String jdkVendor,
            @Nullable String kotlin) {
        return createInstance(
                id,
                DEFAULT_URL,
                "test-object-factory-instance",
                DEFAULT_STATUS,
                java,
                springBoot,
                springFramework,
                jdkVendor,
                kotlin,
                Insights.empty());
    }

    public static Instance createInstance(
            String id,
            String java,
            String springBoot,
            String springFramework,
            String jdkVendor,
            @Nullable String kotlin,
            double memoryUsage) {
        return new Instance(
                InstanceId.of(id),
                "test-object-factory-instance",
                "1.2.3-classifer-test",
                java,
                springBoot,
                springFramework,
                kotlin,
                jdkVendor,
                "df027cf",
                Instant.now(),
                Instant.now(),
                Instance.InstanceStatus.UP,
                new MemoryUsage(memoryUsage),
                "url",
                Insights.empty());
    }

    public static Instance createInstance(String id, String url, Instance.InstanceStatus status) {
        return createInstance(
                id,
                url,
                "test-object-factory-instance",
                status,
                "25",
                "3.5.2",
                "6.0.2",
                "BellSoft",
                null,
                Insights.empty());
    }

    public static Instance createInstance(String id, String url, Instance.InstanceStatus status, Insights insights) {
        return createInstance(
                id, url, "test-object-factory-instance", status, "25", "3.5.2", "6.0.2", "BellSoft", null, insights);
    }

    public static Instance createInstance(String id, String url, Insights insights) {
        return createInstance(id, url, DEFAULT_STATUS, insights);
    }

    public static Insights sampleInsights() {
        return new Insights(
                new HotSpot(
                        List.of(new InsightFeature("AppCDS", false)),
                        List.of(),
                        List.of(new InsightFeature("CompactObjectHeaders", true))),
                List.of());
    }

    public static Instance createInstanceWithHeartbeat(String id, @Nullable Instant instant) {
        return new Instance(
                InstanceId.of(id),
                "test-object-factory-instance",
                "1.2.3-classifer-test",
                "25",
                "3.5.2",
                "6.0.2",
                null,
                "BellSoft",
                "df027cf",
                Instant.now(),
                instant,
                DEFAULT_STATUS,
                new MemoryUsage(1000L),
                DEFAULT_URL,
                Insights.empty());
    }

    public static Instance createInstance(
            String id,
            String url,
            String name,
            Instance.InstanceStatus status,
            String java,
            String springBoot,
            String springFramework,
            String jdkVendor,
            @Nullable String kotlin,
            Insights insights) {
        return new Instance(
                InstanceId.of(id),
                name,
                "1.2.3-classifer-test",
                java,
                springBoot,
                springFramework,
                kotlin,
                jdkVendor,
                "df027cf",
                Instant.now(),
                Instant.now().minusSeconds(60),
                status,
                new MemoryUsage(1000L),
                url,
                insights);
    }
}
