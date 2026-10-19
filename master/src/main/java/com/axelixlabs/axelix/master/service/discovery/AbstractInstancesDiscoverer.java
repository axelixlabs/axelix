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
package com.axelixlabs.axelix.master.service.discovery;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.common.utils.SemanticVersion;
import com.axelixlabs.axelix.master.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.master.contract.metadatav2.MetadataV2;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.Instance.InstanceStatus;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.MemoryUsage;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.BadRequestException;
import com.axelixlabs.axelix.master.service.transport.EndpointInvocationException;
import com.axelixlabs.axelix.master.service.transport.ManagedServiceMetadataEndpointProber;
import com.axelixlabs.axelix.master.service.transport.MetadataV2EndpointProber;

/**
 * Abstract implementation of {@link InstancesDiscoverer} that performs common tasks like checking
 * the {{@link ActuatorEndpoints#METADATA} metadata endpoint} for compatibility etc.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
public abstract class AbstractInstancesDiscoverer implements InstancesDiscoverer {

    // TODO:
    //  So, the problem is that the /actuator path is not guaranteed to be
    //  the path under which the actuator endpoints are going to be exposed.
    //  It is possible to override it via specific properties, see the doc
    //
    // https://docs.spring.io/spring-boot/docs/2.1.7.RELEASE/reference/html/production-ready-monitoring.html#production-ready-customizing-management-server-context-path
    //  So, we have to take this into account. It is however unclear how
    //  we can do that in case of automatic discovery.
    protected static final String ACTUATOR_ENDPOINT_POSTFIX = "/actuator";

    private final Logger logger;
    private final DiscoveryClient discoveryClient;
    private final ManagedServiceMetadataEndpointProber managedServiceProber;
    private final MetadataV2EndpointProber metadataV2Prober;
    private final CompatibilityDetectionStrategy compatibilityDetectionStrategy;
    private final InstanceRegistry instanceRegistry;

    public AbstractInstancesDiscoverer(
            Logger logger,
            DiscoveryClient discoveryClient,
            ManagedServiceMetadataEndpointProber managedServiceMetadataEndpointProber,
            MetadataV2EndpointProber metadataV2EndpointProber,
            CompatibilityDetectionStrategy compatibilityDetectionStrategy,
            InstanceRegistry instanceRegistry) {
        this.discoveryClient = discoveryClient;
        this.managedServiceProber = managedServiceMetadataEndpointProber;
        this.metadataV2Prober = metadataV2EndpointProber;
        this.logger = logger;
        this.compatibilityDetectionStrategy = compatibilityDetectionStrategy;
        this.instanceRegistry = instanceRegistry;
    }

    @Override
    public Set<DiscoveredInstanceProfile> discover() {
        List<String> serviceIds = discoveryClient.getServices();

        if (CollectionUtils.isEmpty(serviceIds)) {
            return Set.of();
        }

        Set<DiscoveredInstanceProfile> result = new HashSet<>();

        for (String serviceId : serviceIds) {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);

            if (!CollectionUtils.isEmpty(instances)) {
                result.addAll(instances.stream()
                        .map(this::discoverInstance)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()));
            }
        }

        return result;
    }

    private @Nullable DiscoveredInstanceProfile discoverInstance(ServiceInstance serviceInstance) {
        Optional<Instance> knownInstance = Optional.ofNullable(serviceInstance.getInstanceId())
                .flatMap(instanceId -> instanceRegistry.get(InstanceId.of(instanceId)));

        if (knownInstance.isPresent()
                && servesMetadataV2(knownInstance.get())
                && compatibilityDetectionStrategy.isCompatible(
                        knownInstance.get().starterVersion())) {
            DiscoveredInstanceProfile.Refresh refreshed = refresh(knownInstance.get());

            if (refreshed != null) {
                return refreshed;
            }
        }

        return Optional.ofNullable(getManagedServiceMetadata(serviceInstance))
                .filter(this::isCompatibleVersion)
                .filter(this::hasApplicationId)
                .map(this::toDomainInstanceSafe)
                .orElse(null);
    }

    // TODO: Remove in 1.6, once the starters before 1.3 (without the metadata v2 endpoint) fall out of the window
    private static boolean servesMetadataV2(Instance instance) {
        return SemanticVersion.tryParse(instance.starterVersion())
                .filter(version -> version.major() > 1 || (version.major() == 1 && version.minor() >= 3))
                .isPresent();
    }

    private DiscoveredInstanceProfile.@Nullable Refresh refresh(Instance instance) {
        try {
            MetadataV2 metadataV2 = metadataV2Prober.invoke(instance.actuatorUrl(), NoHttpPayload.INSTANCE);

            if (!hasRequiredFields(metadataV2)) {
                logger.debug(
                        "The managed service with id : {} returned an incomplete metadata v2. Falling back to the full metadata",
                        instance.id().instanceId());
                return null;
            }

            return new DiscoveredInstanceProfile.Refresh(
                    instance.copy(
                            toInstanceStatus(metadataV2),
                            new MemoryUsage(metadataV2.getMemoryDetails().getHeap())),
                    metadataV2);
        } catch (EndpointInvocationException | BadRequestException error) {
            logger.debug(
                    "Unable to fetch the metadata v2 of the managed service with id : {}. Falling back to the full metadata",
                    instance.id().instanceId(),
                    error);
            return null;
        }
    }

    private static boolean hasRequiredFields(MetadataV2 metadataV2) {
        return metadataV2.getHealthStatus() != null
                && metadataV2.getMemoryDetails() != null
                && metadataV2.getMemoryDetails().getHeap() != null
                && metadataV2.getTransactions() != null;
    }

    private static InstanceStatus toInstanceStatus(MetadataV2 metadataV2) {
        return switch (metadataV2.getHealthStatus()) {
            case UP -> InstanceStatus.UP;
            case DOWN -> InstanceStatus.DOWN;
            case UNKNOWN -> InstanceStatus.UNKNOWN;
        };
    }

    protected record IntermediateInstanceProfile(ServiceInstance serviceInstance, BasicRegistrationMetadata metadata) {}

    private @Nullable IntermediateInstanceProfile getManagedServiceMetadata(ServiceInstance serviceInstance) {
        String actuatorUrl = serviceInstance.getUri() + ACTUATOR_ENDPOINT_POSTFIX;

        try {
            BasicRegistrationMetadata metadata = managedServiceProber.invoke(actuatorUrl, NoHttpPayload.INSTANCE);

            return new IntermediateInstanceProfile(serviceInstance, metadata);
        } catch (EndpointInvocationException | BadRequestException error) {
            logger.debug(
                    "Unable to reach the managed service with id : {}. Skipping instance registration",
                    serviceInstance.getInstanceId(),
                    error);
            return null;
        }
    }

    private boolean isCompatibleVersion(IntermediateInstanceProfile profile) {
        if (compatibilityDetectionStrategy.isCompatible(profile.metadata().getVersion())) {
            return true;
        } else {
            logger.warn(
                    "Service: {} has a version of Axelix starter that current Axelix Master is not capable to work with",
                    profile.serviceInstance().getServiceId());
            return false;
        }
    }

    private boolean hasApplicationId(IntermediateInstanceProfile profile) {
        BasicRegistrationMetadata metadata = profile.metadata();
        if (StringUtils.hasText(metadata.getArtifactId())) {
            return true;
        } else {
            logger.warn(
                    "Service instance: {} does not expose a valid application id (artifactId is mandatory). Skipping registration",
                    profile.serviceInstance().getInstanceId());
            return false;
        }
    }

    private DiscoveredInstanceProfile.@Nullable Registration toDomainInstanceSafe(
            IntermediateInstanceProfile intermediateProfile) {
        try {
            return new DiscoveredInstanceProfile.Registration(
                    toDomainInstance(intermediateProfile), intermediateProfile.metadata());
        } catch (IllegalArgumentException e) {
            logger.warn(
                    "Unable to convert the discovered service : {} to its internal representation to make it manageable. Skipping registration",
                    intermediateProfile.serviceInstance().getServiceId(),
                    e);
            return null;
        }
    }

    /**
     * Map the {@link IntermediateInstanceProfile} to the {@link Instance}. Each implementation may and will have its own
     * {@link ServiceInstance}, and therefore we delegate this mapping to subclasses.
     *
     * @param serviceInstance the dto object that carries information known to this point about the instance being registered
     * @return the domain {@link Instance}.
     * @throws IllegalArgumentException thrown in case of any conversion error, or in case there some unexpected
     *         condition happened during mapping process
     */
    protected abstract Instance toDomainInstance(IntermediateInstanceProfile serviceInstance)
            throws IllegalArgumentException;
}
