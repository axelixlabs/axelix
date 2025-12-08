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
package com.nucleonforge.axile.master.model.instance;

import java.time.Instant;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * @param id                 The id of the instance. This id must be unique among all the other instances that are
 *                           managed by this Axile Master.
 * @param name               Displayable name of the instance
 * @param serviceVersion     Displayable version of the instance itself (not version of our starter inside Instance)
 * @param javaVersion        Version of the Java Platform used inside the service
 * @param springBootVersion  Version of the Spring Boot used inside the service
 * @param commitShaShort     Short git commit hash from which this instance's {@link #serviceVersion version} was build
 * @param deployedAt         Timestamp when the service was deployed
 * @param status             The status of the given instance from the Master standpoint.
 * @param actuatorUrl        The URL of the actuator root, e.g. {@code https://my-app:6061/actuator}
 */
public record Instance(
        InstanceId id,
        String name,
        String serviceVersion,
        String javaVersion,
        String springBootVersion,
        String commitShaShort,
        @Nullable Instant deployedAt,
        InstanceStatus status,
        @NonNull String actuatorUrl) {

    public Instance copy(InstanceStatus instanceStatus) {
        return new Instance(
                this.id,
                this.name,
                this.serviceVersion,
                this.javaVersion,
                this.springBootVersion,
                this.commitShaShort,
                this.deployedAt,
                instanceStatus,
                this.actuatorUrl);
    }

    public enum InstanceStatus {
        UP,
        DOWN,
        UNKNOWN,
        RELOAD
    }
}
