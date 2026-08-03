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

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.MemoryUsage;

/**
 * Test fixture factory for {@link Instance}.
 *
 * @since 29.08.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public final class TestInstanceFactory {

    private static final String DEFAULT_URL = "http://example.com";

    private static final ApplicationId DEFAULT_APPLICATION_ID =
            ApplicationId.of("com.axelixlabs", "test-object-factory-app");

    private static final Instance.InstanceStatus DEFAULT_STATUS = Instance.InstanceStatus.UP;

    private TestInstanceFactory() {}

    public static Instance create(String id) {
        return create(id, DEFAULT_URL);
    }

    public static Instance create(String id, String groupId, String artifactId) {
        return withApplicationId(id, ApplicationId.of(groupId, artifactId));
    }

    public static Instance withApplicationId(String id, ApplicationId applicationId) {
        return new Instance(
                InstanceId.of(id),
                applicationId,
                "test-object-factory-instance",
                "1.2.3-classifer-test",
                "25",
                "3.5.2",
                "6.0.2",
                null,
                "BellSoft",
                "df027cf",
                Instant.now(),
                Instant.now(),
                DEFAULT_STATUS,
                new MemoryUsage(1000L),
                DEFAULT_URL);
    }

    public static Instance create(String id, @Nullable Instant instant) {
        return createInstanceWithHeartbeat(id, instant);
    }

    public static Instance withName(String id, String name) {
        return create(id, DEFAULT_URL, name, DEFAULT_STATUS, "25", "3.5.2", "6.0.2", "BellSoft", null);
    }

    public static Instance create(String id, String url) {
        return create(id, url, DEFAULT_STATUS);
    }

    public static Instance withStatus(String id, Instance.InstanceStatus status) {
        return create(id, DEFAULT_URL, status);
    }

    public static Instance create(
            String id,
            String java,
            String springBoot,
            String springFramework,
            String jdkVendor,
            @Nullable String kotlin) {
        return create(
                id,
                DEFAULT_URL,
                "test-object-factory-instance",
                DEFAULT_STATUS,
                java,
                springBoot,
                springFramework,
                jdkVendor,
                kotlin);
    }

    public static Instance create(
            String id,
            String java,
            String springBoot,
            String springFramework,
            String jdkVendor,
            @Nullable String kotlin,
            double memoryUsage) {
        return new Instance(
                InstanceId.of(id),
                DEFAULT_APPLICATION_ID,
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
                "url");
    }

    public static Instance create(String id, String url, Instance.InstanceStatus status) {
        return create(id, url, "test-object-factory-instance", status, "25", "3.5.2", "6.0.2", "BellSoft", null);
    }

    public static Instance createInstanceWithHeartbeat(String id, @Nullable Instant instant) {
        return new Instance(
                InstanceId.of(id),
                DEFAULT_APPLICATION_ID,
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
                DEFAULT_URL);
    }

    public static Instance create(
            String id,
            String url,
            String name,
            Instance.InstanceStatus status,
            String java,
            String springBoot,
            String springFramework,
            String jdkVendor,
            @Nullable String kotlin) {
        return new Instance(
                InstanceId.of(id),
                DEFAULT_APPLICATION_ID,
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
                url);
    }
}
