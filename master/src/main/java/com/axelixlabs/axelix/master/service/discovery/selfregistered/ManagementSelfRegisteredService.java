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
package com.axelixlabs.axelix.master.service.discovery.selfregistered;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelixlabs.axelix.common.api.registration.ServiceMetadata;
import com.axelixlabs.axelix.common.api.registration.ServiceMetadata.HealthStatus;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.Instance.InstanceStatus;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.MemoryUsage;
import com.axelixlabs.axelix.master.service.MemoryUsageCache;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * Service responsible for creating and registering a new {@link Instance}
 *
 * @author Sergey Cherkasov
 */
public class ManagementSelfRegisteredService {

    private static final Logger logger = LoggerFactory.getLogger(ManagementSelfRegisteredService.class);

    /**
     * The string key that represent the self-registered service creation timestamp.
     */
    public static final String SERVICE_CREATION_TIMESTAMP = "creationTimestamp";

    private static final String SERVICE_NAME = "serviceName";

    private static final String ACTUATOR_ENDPOINT_POSTFIX = "/actuator";

    private final InstanceRegistry instanceRegistry;
    private final MemoryUsageCache memoryUsageCache;

    public ManagementSelfRegisteredService(InstanceRegistry instanceRegistry, MemoryUsageCache memoryUsageCache) {
        this.instanceRegistry = instanceRegistry;
        this.memoryUsageCache = memoryUsageCache;
    }

    public void registerNewInstances(ServiceMetadata serviceMetadata) {
        Instance instance = createInstance(serviceMetadata);

        instanceRegistry.replace(instance);
        memoryUsageCache.putHeapSize(instance.id(), instance.memoryUsage().heap());
    }

    private Instance createInstance(ServiceMetadata serviceMetadata) {
        try {
            return new Instance(
                    InstanceId.of(serviceMetadata.getMetadata().getServiceId()),
                    extractServiceName(serviceMetadata),
                    serviceMetadata.getServiceVersion(),
                    serviceMetadata.getSoftwareVersions().getJava(),
                    serviceMetadata.getSoftwareVersions().getSpringBoot(),
                    serviceMetadata.getSoftwareVersions().getSpringFramework(),
                    serviceMetadata.getSoftwareVersions().getKotlin(),
                    serviceMetadata.getJdkVendor(),
                    serviceMetadata.getCommitShortSha(),
                    extractServiceDeployTimestamp(serviceMetadata),
                    mapStatus(serviceMetadata.getHealthStatus()),
                    new MemoryUsage(serviceMetadata.getMemoryDetails().getHeap()),
                    serviceMetadata.getMetadata().getServiceURL() + ACTUATOR_ENDPOINT_POSTFIX,
                    mapVMFeatures(serviceMetadata.getVmFeatures()));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(buildErrorMessage(serviceMetadata));
        }
    }

    private static List<Instance.VMFeature> mapVMFeatures(List<ServiceMetadata.VMFeature> vmFeature) {
        return vmFeature.stream()
                .map(it -> new Instance.VMFeature(it.getName(), it.getDescription(), it.isEnabled()))
                .toList();
    }

    private static InstanceStatus mapStatus(HealthStatus healthStatus) {
        if (healthStatus == null) {
            return Instance.InstanceStatus.UNKNOWN;
        }

        return switch (healthStatus) {
            case UP -> Instance.InstanceStatus.UP;
            case DOWN -> Instance.InstanceStatus.DOWN;
            case UNKNOWN -> Instance.InstanceStatus.UNKNOWN;
        };
    }

    private static String extractServiceName(ServiceMetadata metadata) {
        String serviceName = metadata.getMetadata().getServiceName();

        if (serviceName == null) {
            logger.warn(
                    "The self-registered service {} in the metadata does not have a name",
                    metadata.getMetadata().getServiceId());
            return SERVICE_NAME;
        }

        return serviceName;
    }

    @Nullable
    private static Instant extractServiceDeployTimestamp(ServiceMetadata serviceMetadata) {
        String deployedAtAsString = serviceMetadata.getMetadata().getDeploymentAt();

        if (deployedAtAsString == null) {
            logger.warn(
                    "The self-registered service {} {} filed in metadata is null",
                    serviceMetadata.getMetadata().getServiceId(),
                    SERVICE_CREATION_TIMESTAMP);
            return null;
        }

        try {
            return OffsetDateTime.parse(deployedAtAsString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .toInstant();
        } catch (DateTimeParseException e) {
            logger.warn(
                    """
                Unable to parse the deployment timestamp of the self-registered service : {} with name {}.
                That will affect the corresponding service on the wallboard UI
                """,
                    serviceMetadata.getMetadata().getServiceId(),
                    serviceMetadata.getMetadata().getServiceName(),
                    e);
            return null;
        }
    }

    private static String buildErrorMessage(ServiceMetadata serviceMetadata) {
        return "Unable to register the self-registered service '%s' with name '%s' as a managed instance"
                .formatted(
                        serviceMetadata.getMetadata().getServiceId(),
                        serviceMetadata.getMetadata().getServiceName());
    }
}
