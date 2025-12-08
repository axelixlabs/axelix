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
package com.nucleonforge.axile.common.api.registration;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the metadata of a service instance as exposed by the Axile SBS actuator endpoint.
 *
 * @param version the version of <strong>the axile starter</strong> in the remote instance.
 *                Might be {@code null} in case the instance is not supposed to be managed.
 *
 * @param serviceVersion the version of the <strong>managed service itself</strong>, i.e. the version
 *                of the end-service artifact (the V inside GAV coordinate). The assumption is that
 *                is never {@code null}, and it frankly should not be.
 *
 * @param commitShortSha the short commit hash (i.e. 'a622a54' or smth like that). Assuming it
 *                to never be {@code null}.
 *
 * @param javaVersion the version of java platform that service is currently running on. Because the
 *                    assumption is that we're going to manage java/kotlin services, the java platform
 *                    is always going to be there. Therefore, it is never {@code null}.
 *
 * @param springBootVersion the version of Spring Boot that service is currently running on. Because the
 *                    assumption is that we're managing the Spring Boot projects (at least as of now), the
 *                    Spring Boot version is also neven {@code null}.
 *
 * @param healthStatus the health status of the given instance that is reported by started infrastructure.
 *                    Never {@code null}.
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
@SuppressWarnings(
        "NullAway") // TODO: we need to think about nullability here. It is not obvious what the correct setup is in
// this case
public record ServiceMetadata(
        @JsonProperty("version") String version,
        @JsonProperty("serviceVersion") String serviceVersion,
        @JsonProperty("commitShortSha") String commitShortSha,
        @JsonProperty("javaVersion") String javaVersion,
        @JsonProperty("springBootVersion") String springBootVersion,
        @JsonProperty("healthStatus") HealthStatus healthStatus) {

    /**
     * The health status of the given instance during registration.
     *
     * @author Mikhail Polivakha
     */
    public enum HealthStatus {
        UP,
        DOWN,
        UNKNOWN
    }
}
