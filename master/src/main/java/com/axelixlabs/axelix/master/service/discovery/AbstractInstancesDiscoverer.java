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
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.CollectionUtils;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.domain.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.service.transport.EndpointInvocationException;
import com.axelixlabs.axelix.master.service.transport.ManagedServiceMetadataEndpointProber;

/**
 * Abstract implementation of {@link InstancesDiscoverer} that performs common tasks like checking
 * the {{@link ActuatorEndpoints#METADATA} metadata endpoint} for compatibility etc.
 *
 * @author Mikhail Polivakha
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
    private final AxelixVersionDiscoverer axelixVersionDiscoverer;

    public AbstractInstancesDiscoverer(
            Logger logger,
            DiscoveryClient discoveryClient,
            ManagedServiceMetadataEndpointProber managedServiceMetadataEndpointProber,
            AxelixVersionDiscoverer axelixVersionDiscoverer) {
        this.discoveryClient = discoveryClient;
        this.managedServiceProber = managedServiceMetadataEndpointProber;
        this.logger = logger;
        this.axelixVersionDiscoverer = axelixVersionDiscoverer;
    }

    @Override
    public @NonNull Set<@NonNull Instance> discover() {
        List<String> serviceIds = discoveryClient.getServices();

        if (CollectionUtils.isEmpty(serviceIds)) {
            return Set.of();
        }

        Set<Instance> result = new HashSet<>();

        for (String serviceId : serviceIds) {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);

            if (!CollectionUtils.isEmpty(instances)) {
                result.addAll(instances.stream()
                        .filter(Objects::nonNull)
                        .map(this::getManagedServiceMetadata)
                        .filter(Objects::nonNull)
                        .filter(this::isCompatibleVersion)
                        .map(this::toDomainInstanceSafe)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()));
            }
        }

        return result;
    }

    private @Nullable InstanceIntermediateProfile getManagedServiceMetadata(@NonNull ServiceInstance serviceInstance) {
        String actuatorUrl = serviceInstance.getUri().toString() + ACTUATOR_ENDPOINT_POSTFIX;

        try {
            BasicDiscoveryMetadata metadata = managedServiceProber.invoke(actuatorUrl, NoHttpPayload.INSTANCE);

            return new InstanceIntermediateProfile(serviceInstance, metadata);
        } catch (EndpointInvocationException error) {
            logger.warn(
                    "Unable to reach the managed service with id : {}. Skipping instance registration",
                    serviceInstance.getInstanceId(),
                    error);
            return null;
        }
    }

    private boolean isCompatibleVersion(@NonNull InstanceIntermediateProfile profile) {
        if (profile.metadata().getVersion().equals(axelixVersionDiscoverer.getVersion())) {
            return true;
        } else {
            logger.warn(
                    "Service: {} have not a valid version",
                    profile.serviceInstance().getServiceId());
            return false;
        }
    }

    /**
     * It is a simple data carrying class that sole purpose is to carry the state that we have
     * assembled about the instance we're about register.
     *
     * @author Mikhail Polivakha
     */
    protected record InstanceIntermediateProfile(ServiceInstance serviceInstance, BasicDiscoveryMetadata metadata) {}

    private @Nullable Instance toDomainInstanceSafe(InstanceIntermediateProfile intermediateProfile) {
        try {
            return toDomainInstance(intermediateProfile);
        } catch (IllegalArgumentException e) {
            logger.warn(
                    "Unable to convert the discovered service : {} to its internal representation to make it manageable. Skipping registration",
                    intermediateProfile.serviceInstance().getServiceId(),
                    e);
            return null;
        }
    }

    /**
     * Map the {@link InstanceIntermediateProfile} to the {@link Instance}. Each implementation may and will have its own
     * {@link ServiceInstance}, and therefore we delegate this mapping to subclasses.
     *
     * @param serviceInstance the dto object that carries information known to this point about the instance being registered
     * @return the domain {@link Instance}.
     * @throws IllegalArgumentException thrown in case of any conversion error, or in case there some unexpected
     *         condition happened during mapping process
     */
    protected abstract Instance toDomainInstance(InstanceIntermediateProfile serviceInstance)
            throws IllegalArgumentException;
}
