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
package com.nucleonforge.axile.master.api.response.software;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.NonNull;

/**
 * Response that contains the summary of used versions for the
 * <strong>single software component</strong> used in the ecosystem.
 *
 * @author Mikhail Polivakha
 */
public class DistributionResponse {

    private final String softwareComponentName;

    private final Map<String, Long> versions;

    /**
     * @param name the name of the software component used in the ecosystem.
     */
    public DistributionResponse(@NonNull String name) {
        this.versions = new HashMap<>();
        this.softwareComponentName = name;
    }

    /**
     * Increase the versions counter for the given {@link #softwareComponentName}.
     */
    public void addVersion(String version) {
        versions.compute(version, (key, value) -> value == null ? 1L : value + 1);
    }

    public String getSoftwareComponentName() {
        return softwareComponentName;
    }

    public Map<String, Long> getVersions() {
        return versions;
    }
}
