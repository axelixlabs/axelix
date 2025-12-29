/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.master.utils;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.instancio.Instancio;
import org.instancio.Select;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axelix.common.domain.BuildInfo;
import com.nucleonforge.axelix.common.domain.ClassPath;
import com.nucleonforge.axelix.common.domain.ClassPathEntry;
import com.nucleonforge.axelix.master.model.instance.Instance;
import com.nucleonforge.axelix.master.model.instance.InstanceId;

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
        return createInstance(id, DEFAULT_URL);
    }

    public static Instance createInstance(String id, String url) {
        return createInstance(id, url, DEFAULT_STATUS);
    }

    public static Instance createInstance(String id, Instance.InstanceStatus status) {
        return createInstance(id, DEFAULT_URL, status);
    }

    public static Instance createInstance(
            String id,
            String java,
            String springBoot,
            String springFramework,
            String jdkVendor,
            @Nullable String kotlin) {
        return createInstance(id, DEFAULT_URL, DEFAULT_STATUS, java, springBoot, springFramework, jdkVendor, kotlin);
    }

    public static Instance createInstance(String id, String url, Instance.InstanceStatus status) {
        return createInstance(id, url, status, "25", "3.5.2", "6.0.2", "BellSoft", null);
    }

    public static Instance createInstance(
            String id,
            String url,
            Instance.InstanceStatus status,
            String java,
            String springBoot,
            String springFramework,
            String jdkVendor,
            @Nullable String kotlin) {
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
                status,
                url);
    }

    public static BuildInfo createBuildInfo(ClassPathEntry... classPathEntries) {
        return Instancio.of(BuildInfo.class)
                .set(
                        Select.fields().named("classPathEntries").declaredIn(ClassPath.class),
                        Arrays.stream(classPathEntries).collect(Collectors.toSet()))
                .create();
    }
}
