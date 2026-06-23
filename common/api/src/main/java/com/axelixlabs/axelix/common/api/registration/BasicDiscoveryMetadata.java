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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * Represents the basic metadata of a service instance as exposed by the custom starter actuator endpoint.
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SuppressWarnings(
        "NullAway") // TODO: we need to think about nullability here. It is not obvious what the correct setup is in
// this case
public final class BasicDiscoveryMetadata {

    private final String version;
    private final String serviceVersion;
    private final String commitShortSha;
    private final String jdkVendor;
    private final SoftwareVersions softwareVersions;
    private final HealthStatus healthStatus;
    private final MemoryDetails memoryDetails;
    private final Insights insights;

    /**
     * Creates a new ServiceMetadata.
     *
     * @param version          the version of <strong>the axelix starter</strong> in the remote instance.
     *                         Might be {@code null} in case the instance is not supposed to be managed.
     * @param serviceVersion   the version of the <strong>managed service itself</strong>, i.e. the version
     *                         of the end-service artifact (the V inside GAV coordinate). The assumption is that
     *                         is never {@code null}, and it frankly should not be.
     * @param commitShortSha   the short commit hash (i.e. 'a622a54' or smth like that). Assuming it
     *                         to never be {@code null}.
     * @param jdkVendor        the JDK vendor name.
     * @param softwareVersions the software versions.
     * @param healthStatus     the health status of the given instance that is reported by started infrastructure.
     *                         Never {@code null}.
     * @param memoryDetails    the memory details.
     * @param insights          the insight information discovered for the given service instance.
     */
    @JsonCreator
    public BasicDiscoveryMetadata(
            @JsonProperty("version") String version,
            @JsonProperty("serviceVersion") String serviceVersion,
            @JsonProperty("commitShortSha") String commitShortSha,
            @JsonProperty("jdkVendor") String jdkVendor,
            @JsonProperty("softwareVersions") SoftwareVersions softwareVersions,
            @JsonProperty("healthStatus") HealthStatus healthStatus,
            @JsonProperty("memoryDetails") MemoryDetails memoryDetails,
            @JsonProperty("insights") Insights insights) {
        this.version = version;
        this.serviceVersion = serviceVersion;
        this.commitShortSha = commitShortSha;
        this.jdkVendor = jdkVendor;
        this.softwareVersions = softwareVersions;
        this.healthStatus = healthStatus;
        this.memoryDetails = memoryDetails;
        this.insights = insights;
    }

    public String getVersion() {
        return version;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public String getCommitShortSha() {
        return commitShortSha;
    }

    public String getJdkVendor() {
        return jdkVendor;
    }

    public SoftwareVersions getSoftwareVersions() {
        return softwareVersions;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public MemoryDetails getMemoryDetails() {
        return memoryDetails;
    }

    public Insights getInsights() {
        return insights;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicDiscoveryMetadata that = (BasicDiscoveryMetadata) o;
        return Objects.equals(version, that.version)
                && Objects.equals(serviceVersion, that.serviceVersion)
                && Objects.equals(commitShortSha, that.commitShortSha)
                && Objects.equals(jdkVendor, that.jdkVendor)
                && Objects.equals(softwareVersions, that.softwareVersions)
                && healthStatus == that.healthStatus
                && Objects.equals(memoryDetails, that.memoryDetails)
                && Objects.equals(insights, that.insights);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                version,
                serviceVersion,
                commitShortSha,
                jdkVendor,
                softwareVersions,
                healthStatus,
                memoryDetails,
                insights);
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
                + softwareVersions
                + ", healthStatus="
                + healthStatus
                + ", memoryDetails="
                + memoryDetails
                + '}';
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
        @JsonCreator
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

        public String getJava() {
            return java;
        }

        public String getSpringBoot() {
            return springBoot;
        }

        public String getSpringFramework() {
            return springFramework;
        }

        @Nullable
        public String getKotlin() {
            return kotlin;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
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
        @JsonCreator
        public MemoryDetails(@JsonProperty("heap") long heap) {
            this.heap = heap;
        }

        public long getHeap() {
            return heap;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
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

    /**
     * Insight information discovered for the given service instance.
     */
    public static final class Insights {

        private final HotSpot hotSpot;
        private final List<InsightFeature> springFramework;

        /**
         * Creates a new Insight.
         *
         * @param hotSpot         the HotSpot-specific insight groups.
         * @param springFramework the Spring Framework insight features.
         */
        @JsonCreator
        public Insights(
                @JsonProperty("hotSpot") HotSpot hotSpot,
                @JsonProperty("springFramework") List<InsightFeature> springFramework) {
            this.hotSpot = hotSpot;
            this.springFramework = springFramework;
        }

        public HotSpot getHotSpot() {
            return hotSpot;
        }

        public List<InsightFeature> getSpringFramework() {
            return springFramework;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Insights insights = (Insights) o;
            return Objects.equals(hotSpot, insights.hotSpot)
                    && Objects.equals(springFramework, insights.springFramework);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hotSpot, springFramework);
        }

        @Override
        public String toString() {
            return "Insight{" + "hotSpot=" + hotSpot + ", springFramework=" + springFramework + '}';
        }
    }

    /**
     * HotSpot-specific insight groups.
     */
    public static final class HotSpot {

        private final List<InsightFeature> projectLeyden;
        private final List<InsightFeature> gc;
        private final List<InsightFeature> projectLilliputh;

        /**
         * Creates a new HotSpot.
         *
         * @param projectLeyden   the Project Leyden insight features.
         * @param gc              the garbage collection insight features.
         * @param projectLilliputh the Project Lilliputh insight features.
         */
        @JsonCreator
        public HotSpot(
                @JsonProperty("projectLeyden") List<InsightFeature> projectLeyden,
                @JsonProperty("gc") List<InsightFeature> gc,
                @JsonProperty("projectLilliputh") List<InsightFeature> projectLilliputh) {
            this.projectLeyden = projectLeyden;
            this.gc = gc;
            this.projectLilliputh = projectLilliputh;
        }

        public List<InsightFeature> getProjectLeyden() {
            return projectLeyden;
        }

        public List<InsightFeature> getGc() {
            return gc;
        }

        public List<InsightFeature> getProjectLilliputh() {
            return projectLilliputh;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            HotSpot hotSpot = (HotSpot) o;
            return Objects.equals(projectLeyden, hotSpot.projectLeyden)
                    && Objects.equals(gc, hotSpot.gc)
                    && Objects.equals(projectLilliputh, hotSpot.projectLilliputh);
        }

        @Override
        public int hashCode() {
            return Objects.hash(projectLeyden, gc, projectLilliputh);
        }

        @Override
        public String toString() {
            return "HotSpot{"
                    + "projectLeyden="
                    + projectLeyden
                    + ", gc="
                    + gc
                    + ", projectLilliputh="
                    + projectLilliputh
                    + '}';
        }
    }

    /**
     * The specific insight feature discovered for the given service instance.
     */
    public static final class InsightFeature {

        private final String name;
        private final boolean enabled;

        /**
         * Creates a new InsightFeature.
         *
         * @param name    the insight feature name.
         * @param enabled the enabled state of the insight feature.
         */
        @JsonCreator
        public InsightFeature(@JsonProperty("name") String name, @JsonProperty("enabled") boolean enabled) {
            this.name = name;
            this.enabled = enabled;
        }

        public String getName() {
            return name;
        }

        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            InsightFeature that = (InsightFeature) o;
            return enabled == that.enabled && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, enabled);
        }

        @Override
        public String toString() {
            return "InsightFeature{" + "name='" + name + '\'' + ", enabled=" + enabled + '}';
        }
    }
}
