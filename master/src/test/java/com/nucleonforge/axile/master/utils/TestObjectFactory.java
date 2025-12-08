/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.master.utils;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.instancio.Instancio;
import org.instancio.Select;

import com.nucleonforge.axile.common.domain.BuildInfo;
import com.nucleonforge.axile.common.domain.ClassPath;
import com.nucleonforge.axile.common.domain.ClassPathEntry;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.model.instance.InstanceId;

/**
 * Utility factory for creating test objects used in unit and integration tests.
 *
 * @since 29.08.2025
 * @author Nikita Kirillov
 */
public final class TestObjectFactory {

    private static final String DEFAULT_URL = "http://example.com";

    private static final Instance.InstanceStatus DEFAULT_STATUS = Instance.InstanceStatus.UP;

    private TestObjectFactory() {}

    public static Instance createInstance(String id) {
        return createInstanceWithUrl(id, DEFAULT_URL);
    }

    // TODO:
    public static Instance createInstanceWithUrl(String id, String url) {
        return createInstanceWithUrlAndStatus(id, url, DEFAULT_STATUS);
    }

    public static Instance createInstanceWithStatus(String id, Instance.InstanceStatus status) {
        return createInstanceWithUrlAndStatus(id, DEFAULT_URL, status);
    }

    public static Instance createInstanceWithUrlAndStatus(String id, String url, Instance.InstanceStatus status) {
        return new Instance(
                InstanceId.of(id),
                "test-object-factory-instance",
                "1.2.3-classifer-test",
                "17.0.14",
                "3.5.0",
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
