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
package com.nucleonforge.axile.master.api.response;

import java.util.Collection;

import org.jspecify.annotations.Nullable;

/**
 * The grid of {@link InstanceShortProfile instances}.
 *
 * @author Mikhail Polivakha
 */
public record InstancesGridResponse(Collection<InstanceShortProfile> instances) {

    /**
     * The short profile of the particular Instance, managed by this master deployment.
     *
     * @param deployedFor a String representation for how long the service has been already deployed for.
     *
     * @author Mikhail Polivakha
     */
    public record InstanceShortProfile(
            String instanceId,
            String name,
            String serviceVersion,
            String commitShaShort,
            InstanceStatus status,
            @Nullable String deployedFor,
            String javaVersion,
            String springBootVersion) {}

    /**
     * The state of the given instance.
     *
     * @author Mikhail Polivakha
     */
    public enum InstanceStatus {
        UP,
        DOWN,
        UNKNOWN,
        RELOAD
    }
}
