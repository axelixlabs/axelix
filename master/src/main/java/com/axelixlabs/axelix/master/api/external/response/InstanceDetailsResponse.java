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
package com.axelixlabs.axelix.master.api.external.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.domain.Instance.VMFeature;

/**
 * The profile of a given details.
 *
 * @param serviceName  The name of the service providing information.
 * @param git          The profile of the git component response.
 * @param runtime      The profile of the runtime component response.
 * @param spring       The profile of the spring component response.
 * @param build        The profile of the build component response.
 * @param os           The profile of the OS component response.
 *
 * @author Nikita Kirilov, Sergey Cherkasov
 */
public record InstanceDetailsResponse(
        @Nullable String serviceName,
        @Nullable GitProfile git,
        RuntimeProfile runtime,
        SpringProfile spring,
        @Nullable BuildProfile build,
        OSProfile os,
        List<VMFeature> vmFeatures) {

    /**
     * The profile of a given build.
     *
     * @param commitShaShort     The ID of the commit.
     * @param branch             The name of the Git branch.
     * @param authorName         The commit author name.
     * @param authorEmail        The commit author email.
     * @param commitTimestamp    The timestamp of the commit.
     */
    public record GitProfile(
            String commitShaShort, String branch, String authorName, String authorEmail, String commitTimestamp) {}

    /**
     * The profile of a given runtime.
     *
     * @param javaVersion       The version of the java.
     * @param kotlinVersion     The version of the kotlin, if any.
     * @param jdkVendor         The name of the vendor.
     * @param garbageCollector  The name of the garbage collector.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RuntimeProfile(
            String javaVersion, @Nullable String kotlinVersion, String jdkVendor, String garbageCollector) {}

    /**
     * The profile of a given spring.
     *
     * @param springBootVersion       The version of the Spring Boot.
     * @param springFrameworkVersion  The version of the Spring Framework.
     * @param springCloudVersion      The version of the Spring Cloud, if any.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SpringProfile(
            String springBootVersion,
            String springFrameworkVersion,
            @Nullable String springCloudVersion) {}

    /**
     * The profile of a given build.
     *
     * @param artifact     The artifact ID of the application.
     * @param version      The version of the application.
     * @param group        The group ID of the application.
     * @param time         The time the application was built.
     */
    public record BuildProfile(String artifact, String version, String group, String time) {}

    /**
     * The profile of a given OS.
     *
     * @param name     The name of the operating system.
     * @param version  The version of the operating system.
     * @param arch     The architecture of the CPU.
     */
    public record OSProfile(String name, String version, String arch) {}
}
