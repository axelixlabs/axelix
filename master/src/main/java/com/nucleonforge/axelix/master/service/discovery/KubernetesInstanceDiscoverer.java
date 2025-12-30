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
package com.nucleonforge.axelix.master.service.discovery;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.nucleonforge.axelix.common.api.registration.ServiceMetadata;
import com.nucleonforge.axelix.master.model.instance.Instance;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.transport.ManagedServiceMetadataEndpointProber;

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
            ManagedServiceMetadataEndpointProber managedServiceMetadataEndpointProber) {
        super(log, discoveryClient, managedServiceMetadataEndpointProber);
    }

    @Override
    protected Instance toDomainInstance(InstanceIntermediateProfile profile) throws IllegalArgumentException {
        ServiceInstance serviceInstance = profile.serviceInstance();

        if (serviceInstance instanceof KubernetesServiceInstance k8sInstance) {

            Instant deployedAt = extractPodDeployTimestamp(k8sInstance);

            return new Instance(
                    InstanceId.of(k8sInstance.getInstanceId()),
                    k8sInstance.podName(),
                    profile.metadata().serviceVersion(),
                    profile.metadata().versions().java(),
                    profile.metadata().versions().springBoot(),
                    profile.metadata().versions().springFramework(),
                    profile.metadata().versions().kotlin(),
                    profile.metadata().jdkVendor(),
                    profile.metadata().commitShortSha(),
                    deployedAt,
                    mapStatus(profile),
                    serviceInstance.getUri().toString() + "/actuator");
        } else {
            throw new IllegalArgumentException(buildErrorMessage(serviceInstance));
        }
    }

    private static Instance.InstanceStatus mapStatus(InstanceIntermediateProfile profile) {
        ServiceMetadata.HealthStatus healthStatus = profile.metadata().healthStatus();

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
