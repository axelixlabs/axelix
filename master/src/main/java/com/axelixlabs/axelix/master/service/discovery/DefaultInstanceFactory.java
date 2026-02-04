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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.HealthStatus;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.Instance.InstanceStatus;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.MemoryUsage;

/**
 * Default implementation {@link InstanceFactory}.
 *
 * @author Sergey Cherkasov
 */
public class DefaultInstanceFactory implements InstanceFactory {
    private static final Logger logger = LoggerFactory.getLogger(DefaultInstanceFactory.class);

    private static final String ACTUATOR_ENDPOINT_POSTFIX = "/actuator";

    public Instance createInstance(
            String serviceId,
            String serviceName,
            String deploymentAt,
            String serviceUrl,
            BasicDiscoveryMetadata metadata) {
        return new Instance(
                InstanceId.of(serviceId),
                serviceName,
                metadata.getServiceVersion(),
                metadata.getSoftwareVersions().getJava(),
                metadata.getSoftwareVersions().getSpringBoot(),
                metadata.getSoftwareVersions().getSpringFramework(),
                metadata.getSoftwareVersions().getKotlin(),
                metadata.getJdkVendor(),
                metadata.getCommitShortSha(),
                extractDeployTimestamp(deploymentAt, serviceId, serviceName),
                convertServiceStatus(metadata.getHealthStatus()),
                new MemoryUsage(metadata.getMemoryDetails().getHeap()),
                serviceUrl + ACTUATOR_ENDPOINT_POSTFIX,
                convertMapVMFeatures(metadata.getVmFeatures()));
    }

    private InstanceStatus convertServiceStatus(HealthStatus healthStatus) {
        return switch (healthStatus) {
            case UP -> InstanceStatus.UP;
            case DOWN -> InstanceStatus.DOWN;
            case UNKNOWN -> InstanceStatus.UNKNOWN;
        };
    }

    private List<Instance.VMFeature> convertMapVMFeatures(List<BasicDiscoveryMetadata.VMFeature> vmFeature) {
        return vmFeature.stream()
                .map(it -> new Instance.VMFeature(it.getName(), it.getDescription(), it.isEnabled()))
                .toList();
    }

    @Nullable
    private Instant extractDeployTimestamp(String deploymentAt, String serviceId, String serviceName) {
        try {
            return OffsetDateTime.parse(deploymentAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .toInstant();
        } catch (DateTimeParseException e) {
            logger.warn(
                    """
            Unable to parse the deployment timestamp of the Service : {} with name {}.
            That will affect the corresponding service on the wallboard UI
            """,
                    serviceId,
                    serviceName,
                    e);
            return null;
        }
    }
}
