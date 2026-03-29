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
package com.axelixlabs.axelix.master.domain;

import java.time.Instant;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @param id                      The id of the instance. This id must be unique among all the other instances that are
 *                                managed by this Axelix Master.
 * @param name                    Displayable name of the instance
 * @param serviceVersion          Displayable version of the instance itself (not version of our starter inside Instance)
 * @param javaVersion             Version of the Java Platform used inside the service
 * @param springBootVersion       Version of the Spring Boot used inside the service
 * @param springFrameworkVersion  Version of the Spring Framework used inside the service
 * @param kotlinVersion           Version of the Kotlin used inside the service. Might be {@code null}.
 * @param jdkVendor               Vendor of JDK distribution used inside the service.
 * @param commitShaShort          Short git commit hash from which this instance's {@link #serviceVersion version} was build
 * @param deployedAt              Timestamp when the service was deployed
 * @param status                  The status of the given instance from the Master standpoint.
 * @param actuatorUrl             The URL of the actuator root, e.g. {@code https://my-app:6061/actuator}
 */
@Table("instances")
public record Instance(
        @Id InstanceId id,
        String name,
        String serviceVersion,
        String javaVersion,
        String springBootVersion,
        String springFrameworkVersion,
        @Nullable String kotlinVersion,
        String jdkVendor,
        String commitShaShort,
        @Nullable Instant deployedAt,
        InstanceStatus status,
        @Embedded.Empty MemoryUsage memoryUsage,
        String actuatorUrl,
        @Column("vm_features") VmFeatures vmFeatures) {

    /**
     * Status of various useful JVM features for this service, like AOT Cache, AppCDS etc.
     *
     * @param name feature name
     * @param description feature description
     * @param enabled enabled or not
     */
    public record VMFeature(String name, String description, boolean enabled) {}

    /**
     * Wraps persisted VM features JSON. A dedicated type (not {@code Set}) is required so Spring Data JDBC maps a
     * single column instead of a separate {@code vm_feature} collection table.
     */
    public record VmFeatures(Set<VMFeature> features) {

        public static VmFeatures of(Set<VMFeature> features) {
            return new VmFeatures(features);
        }

        public static VmFeatures empty() {
            return new VmFeatures(Set.of());
        }
    }

    public Instance copy(InstanceStatus instanceStatus) {
        return new Instance(
                this.id,
                this.name,
                this.serviceVersion,
                this.javaVersion,
                this.springBootVersion,
                this.springFrameworkVersion,
                this.kotlinVersion,
                this.jdkVendor,
                this.commitShaShort,
                this.deployedAt,
                instanceStatus,
                this.memoryUsage,
                this.actuatorUrl,
                this.vmFeatures);
    }

    public enum InstanceStatus {
        UP,
        DOWN,
        UNKNOWN,
        RELOAD
    }
}
