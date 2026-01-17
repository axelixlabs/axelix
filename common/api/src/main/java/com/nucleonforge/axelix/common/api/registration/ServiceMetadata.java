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
package com.nucleonforge.axelix.common.api.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * Represents the metadata of a service instance as exposed by the custom starter actuator endpoint.
 *
 * @param version the version of <strong>the axelix starter</strong> in the remote instance.
 *                Might be {@code null} in case the instance is not supposed to be managed.
 *
 * @param serviceVersion the version of the <strong>managed service itself</strong>, i.e. the version
 *                of the end-service artifact (the V inside GAV coordinate). The assumption is that
 *                is never {@code null}, and it frankly should not be.
 *
 * @param commitShortSha the short commit hash (i.e. 'a622a54' or smth like that). Assuming it
 *                to never be {@code null}.
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
        @JsonProperty("jdkVendor") String jdkVendor,
        @JsonProperty("versions") SoftwareVersions versions,
        @JsonProperty("healthStatus") HealthStatus healthStatus,
        @JsonProperty("memory") MemoryDetails memoryDetails) {

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

    /**
     * @param java            the version of java platform that service is currently running on. Because the
     *                        assumption is that we're going to manage java/kotlin services, the java platform
     *                        is always going to be there. Therefore, it is never {@code null}.
     *
     * @param springBoot      the version of Spring Boot that service is currently running on. Because the
     *                        assumption is that we're managing the Spring Boot projects (at least as of now), the
     *                        Spring Boot version is also never {@code null}.
     *
     * @param springFramework the version of Spring Framework that service is currently running on. Because the
     *                        assumption is that we're managing the Spring Boot projects (at least as of now), the
     *                        Spring Framework version is also never {@code null}.
     *
     * @param kotlin          the version of Kotlin that service is currently using. Might be {@code null} in case
     *                        the service is not using kotlin.
     */
    public record SoftwareVersions(
            @JsonProperty("java") String java,
            @JsonProperty("springBoot") String springBoot,
            @JsonProperty("springFramework") String springFramework,
            @JsonProperty("kotlin") @Nullable String kotlin) {}

    /**
     * Memory details of the given Instance.
     *
     * @param heap the estimated heap size of the given instance.
     */
    public record MemoryDetails(@JsonProperty("heap") long heap) {}
}
