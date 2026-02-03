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
package com.axelixlabs.axelix.master.service.discovery.k8s;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.axelixlabs.axelix.common.api.registration.ServiceMetadata;
import com.axelixlabs.axelix.common.domain.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.MemoryUsage;
import com.axelixlabs.axelix.master.service.discovery.AbstractInstancesDiscoverer;
import com.axelixlabs.axelix.master.service.discovery.InstancesDiscoverer;
import com.axelixlabs.axelix.master.service.transport.ManagedServiceMetadataEndpointProber;

/**
 * Kubernetes implementation of {@link InstancesDiscoverer}.
 * <p>This service discovers running instances of services registered
 * in a Kubernetes cluster.</p>
 *
 * @author Mikhail Polivakha
 */
public class KubernetesInstanceDiscoverer extends AbstractInstancesDiscoverer {

    private static final Logger log = LoggerFactory.getLogger(KubernetesInstanceDiscoverer.class);

    /**
     * The string key that represent the pod's creation timestamp.
     */
    public static final String POD_CREATION_TIMESTAMP = "creationTimestamp";

    public KubernetesInstanceDiscoverer(
            DiscoveryClient discoveryClient,
            ManagedServiceMetadataEndpointProber managedServiceMetadataEndpointProber,
            AxelixVersionDiscoverer axelixVersionDiscoverer) {
        super(log, discoveryClient, managedServiceMetadataEndpointProber, axelixVersionDiscoverer);
    }

    @Override
    protected Instance toDomainInstance(InstanceIntermediateProfile profile) throws IllegalArgumentException {
        ServiceInstance serviceInstance = profile.serviceInstance();

        if (serviceInstance instanceof KubernetesServiceInstance k8sInstance) {

            Instant deployedAt = extractPodDeployTimestamp(k8sInstance);

            return new Instance(
                    InstanceId.of(k8sInstance.getInstanceId()),
                    k8sInstance.podName(),
                    profile.metadata().getServiceVersion(),
                    profile.metadata().getSoftwareVersions().getJava(),
                    profile.metadata().getSoftwareVersions().getSpringBoot(),
                    profile.metadata().getSoftwareVersions().getSpringFramework(),
                    profile.metadata().getSoftwareVersions().getKotlin(),
                    profile.metadata().getJdkVendor(),
                    profile.metadata().getCommitShortSha(),
                    deployedAt,
                    mapStatus(profile),
                    new MemoryUsage(profile.metadata().getMemoryDetails().getHeap()),
                    serviceInstance.getUri().toString() + "/actuator",
                    mapVMFeatures(profile));
        } else {
            throw new IllegalArgumentException(buildErrorMessage(serviceInstance));
        }
    }

    private static List<Instance.VMFeature> mapVMFeatures(InstanceIntermediateProfile profile) {
        return profile.metadata().getVmFeatures().stream()
                .map(it -> new Instance.VMFeature(it.getName(), it.getDescription(), it.isEnabled()))
                .toList();
    }

    private static Instance.InstanceStatus mapStatus(InstanceIntermediateProfile profile) {
        ServiceMetadata.HealthStatus healthStatus = profile.metadata().getHealthStatus();

        if (healthStatus == null) {
            return Instance.InstanceStatus.UNKNOWN;
        }

        return switch (healthStatus) {
            case UP -> Instance.InstanceStatus.UP;
            case DOWN -> Instance.InstanceStatus.DOWN;
            case UNKNOWN -> Instance.InstanceStatus.UNKNOWN;
        };
    }

    @Nullable
    private static Instant extractPodDeployTimestamp(KubernetesServiceInstance k8sInstance) {
        String deployedAtAsString = k8sInstance.getDeploymentAt();

        if (deployedAtAsString == null) {
            log.warn(
                    "The K8S pod's {} {} filed in metadata is null",
                    k8sInstance.getInstanceId(),
                    POD_CREATION_TIMESTAMP);
            return null;
        }

        try {
            return OffsetDateTime.parse(deployedAtAsString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .toInstant();
        } catch (DateTimeParseException e) {
            log.warn(
                    """
                Unable to parse the deployment timestamp of the pod : {}.
                That will affect the corresponding service on the wallboard UI
                """,
                    k8sInstance.getInstanceId(),
                    e);
            return null;
        }
    }

    private static String buildErrorMessage(ServiceInstance serviceInstance) {
        return "Unable to register K8S pod '%s' as a managed instance - expected %s to be an instance of %s, but actually is %s"
                .formatted(
                        serviceInstance.getInstanceId(),
                        ServiceInstance.class.getSimpleName(),
                        org.springframework.cloud.kubernetes.commons.discovery.KubernetesServiceInstance.class
                                .getName(),
                        serviceInstance.getClass().getName());
    }
}
