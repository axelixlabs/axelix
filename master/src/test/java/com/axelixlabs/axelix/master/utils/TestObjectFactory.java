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
import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.Instance.VMFeature;
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

    public static Instance withName(String id, String name) {
        return createInstance(id, DEFAULT_URL, name, null, "25", "3.5.2", "6.0.2", "BellSoft", null, List.of());
    }

    public static Instance withUrl(String id, String url) {
        return createInstance(id, url, DEFAULT_STATUS);
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
                List.of());
    }

    public static Instance createInstance(String id, String url, Instance.InstanceStatus status) {
        return createInstance(
                id, url, "test-object-factory-instance", status, "25", "3.5.2", "6.0.2", "BellSoft", null, List.of());
    }

    public static Instance createInstance(
            String id, String url, Instance.InstanceStatus status, VMFeature... vmFeatures) {
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
                Arrays.stream(vmFeatures).toList());
    }

    public static Instance createInstance(String id, String url, VMFeature... vmFeatures) {
        return createInstance(
                id,
                url,
                "test-object-factory-instance",
                DEFAULT_STATUS,
                "25",
                "3.5.2",
                "6.0.2",
                "BellSoft",
                null,
                Arrays.stream(vmFeatures).toList());
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
            List<VMFeature> vmFeatures) {
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
                status,
                new MemoryUsage(1000L),
                url,
                vmFeatures);
    }
}
