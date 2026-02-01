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
package com.axelixlabs.axelix.common.api.registration;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * Represents the metadata of a service instance as exposed by the custom starter actuator endpoint.
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SuppressWarnings(
        "NullAway") // TODO: we need to think about nullability here. It is not obvious what the correct setup is in
// this case
public final class ServiceMetadata {

    private final String version;
    private final String serviceVersion;
    private final String commitShortSha;
    private final String jdkVendor;
    private final SoftwareVersions versions;
    private final HealthStatus healthStatus;
    private final MemoryDetails memoryDetails;
    private final List<VMFeature> vmFeatures;

    /**
     * Creates a new ServiceMetadata.
     *
     * @param version        the version of <strong>the axelix starter</strong> in the remote instance.
     *                       Might be {@code null} in case the instance is not supposed to be managed.
     * @param serviceVersion the version of the <strong>managed service itself</strong>, i.e. the version
     *                       of the end-service artifact (the V inside GAV coordinate). The assumption is that
     *                       is never {@code null}, and it frankly should not be.
     * @param commitShortSha the short commit hash (i.e. 'a622a54' or smth like that). Assuming it
     *                       to never be {@code null}.
     * @param jdkVendor      the JDK vendor name.
     * @param versions       the software versions.
     * @param healthStatus   the health status of the given instance that is reported by started infrastructure.
     *                       Never {@code null}.
     * @param memoryDetails  the memory details.
     * @param vmFeatures     the VM features.
     */
    public ServiceMetadata(
            @JsonProperty("version") String version,
            @JsonProperty("serviceVersion") String serviceVersion,
            @JsonProperty("commitShortSha") String commitShortSha,
            @JsonProperty("jdkVendor") String jdkVendor,
            @JsonProperty("versions") SoftwareVersions versions,
            @JsonProperty("healthStatus") HealthStatus healthStatus,
            @JsonProperty("memory") MemoryDetails memoryDetails,
            @JsonProperty("vmFeatures") List<VMFeature> vmFeatures) {
        this.version = version;
        this.serviceVersion = serviceVersion;
        this.commitShortSha = commitShortSha;
        this.jdkVendor = jdkVendor;
        this.versions = versions;
        this.healthStatus = healthStatus;
        this.memoryDetails = memoryDetails;
        this.vmFeatures = vmFeatures;
    }

    public String version() {
        return version;
    }

    public String serviceVersion() {
        return serviceVersion;
    }

    public String commitShortSha() {
        return commitShortSha;
    }

    public String jdkVendor() {
        return jdkVendor;
    }

    public SoftwareVersions versions() {
        return versions;
    }

    public HealthStatus healthStatus() {
        return healthStatus;
    }

    public MemoryDetails memoryDetails() {
        return memoryDetails;
    }

    public List<VMFeature> vmFeatures() {
        return vmFeatures;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceMetadata that = (ServiceMetadata) o;
        return Objects.equals(version, that.version)
                && Objects.equals(serviceVersion, that.serviceVersion)
                && Objects.equals(commitShortSha, that.commitShortSha)
                && Objects.equals(jdkVendor, that.jdkVendor)
                && Objects.equals(versions, that.versions)
                && healthStatus == that.healthStatus
                && Objects.equals(memoryDetails, that.memoryDetails)
                && Objects.equals(vmFeatures, that.vmFeatures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                version, serviceVersion, commitShortSha, jdkVendor, versions, healthStatus, memoryDetails, vmFeatures);
    }

    @Override
    public String toString() {
        return "ServiceMetadata{"
                + "version='"
                + version
                + '\''
                + ", serviceVersion='"
                + serviceVersion
                + '\''
                + ", commitShortSha='"
                + commitShortSha
                + '\''
                + ", jdkVendor='"
                + jdkVendor
                + '\''
                + ", versions="
                + versions
                + ", healthStatus="
                + healthStatus
                + ", memoryDetails="
                + memoryDetails
                + ", vmFeatures="
                + vmFeatures
                + '}';
    }

    /**
     * The specific feature of the current runtime.
     */
    public static final class VMFeature {

        private final String name;
        private final String description;
        private final boolean enabled;

        public VMFeature(String name, String description, boolean enabled) {
            this.name = name;
            this.description = description;
            this.enabled = enabled;
        }

        public String name() {
            return name;
        }

        public String description() {
            return description;
        }

        public boolean enabled() {
            return enabled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VMFeature vmFeature = (VMFeature) o;
            return enabled == vmFeature.enabled
                    && Objects.equals(name, vmFeature.name)
                    && Objects.equals(description, vmFeature.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, description, enabled);
        }

        @Override
        public String toString() {
            return "VMFeature{"
                    + "name='"
                    + name
                    + '\''
                    + ", description='"
                    + description
                    + '\''
                    + ", enabled="
                    + enabled
                    + '}';
        }
    }

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
     * Software versions information.
     */
    public static final class SoftwareVersions {

        private final String java;
        private final String springBoot;
        private final String springFramework;

        @Nullable
        private final String kotlin;

        /**
         * Creates a new SoftwareVersions.
         *
         * @param java            the version of java platform that service is currently running on. Because the
         *                        assumption is that we're going to manage java/kotlin services, the java platform
         *                        is always going to be there. Therefore, it is never {@code null}.
         * @param springBoot      the version of Spring Boot that service is currently running on. Because the
         *                        assumption is that we're managing the Spring Boot projects (at least as of now), the
         *                        Spring Boot version is also never {@code null}.
         * @param springFramework the version of Spring Framework that service is currently running on. Because the
         *                        assumption is that we're managing the Spring Boot projects (at least as of now), the
         *                        Spring Framework version is also never {@code null}.
         * @param kotlin          the version of Kotlin that service is currently using. Might be {@code null} in case
         *                        the service is not using kotlin.
         */
        public SoftwareVersions(
                @JsonProperty("java") String java,
                @JsonProperty("springBoot") String springBoot,
                @JsonProperty("springFramework") String springFramework,
                @JsonProperty("kotlin") @Nullable String kotlin) {
            this.java = java;
            this.springBoot = springBoot;
            this.springFramework = springFramework;
            this.kotlin = kotlin;
        }

        public String java() {
            return java;
        }

        public String springBoot() {
            return springBoot;
        }

        public String springFramework() {
            return springFramework;
        }

        @Nullable
        public String kotlin() {
            return kotlin;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SoftwareVersions that = (SoftwareVersions) o;
            return Objects.equals(java, that.java)
                    && Objects.equals(springBoot, that.springBoot)
                    && Objects.equals(springFramework, that.springFramework)
                    && Objects.equals(kotlin, that.kotlin);
        }

        @Override
        public int hashCode() {
            return Objects.hash(java, springBoot, springFramework, kotlin);
        }

        @Override
        public String toString() {
            return "SoftwareVersions{"
                    + "java='"
                    + java
                    + '\''
                    + ", springBoot='"
                    + springBoot
                    + '\''
                    + ", springFramework='"
                    + springFramework
                    + '\''
                    + ", kotlin='"
                    + kotlin
                    + '\''
                    + '}';
        }
    }

    /**
     * Memory details of the given Instance.
     */
    public static final class MemoryDetails {

        private final long heap;

        /**
         * Creates a new MemoryDetails.
         *
         * @param heap the estimated heap size of the given instance.
         */
        public MemoryDetails(@JsonProperty("heap") long heap) {
            this.heap = heap;
        }

        public long heap() {
            return heap;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MemoryDetails that = (MemoryDetails) o;
            return heap == that.heap;
        }

        @Override
        public int hashCode() {
            return Objects.hash(heap);
        }

        @Override
        public String toString() {
            return "MemoryDetails{" + "heap=" + heap + '}';
        }
    }
}
