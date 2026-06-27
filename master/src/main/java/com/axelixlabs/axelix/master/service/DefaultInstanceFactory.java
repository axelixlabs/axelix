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
package com.axelixlabs.axelix.master.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.HealthStatus;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.Instance.InstanceStatus;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.domain.MemoryUsage;

/**
 * Default implementation {@link InstanceFactory}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@Service
public class DefaultInstanceFactory implements InstanceFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultInstanceFactory.class);

    public Instance createInstance(
            String instanceId,
            String instanceName,
            String deploymentAt,
            @Nullable Instant latestHeartBeat,
            String instanceActuatorUrl,
            BasicDiscoveryMetadata metadata)
            throws IllegalArgumentException {

        return new Instance(
                InstanceId.of(instanceId),
                resolveApplicationId(instanceId, metadata),
                instanceName,
                metadata.getServiceVersion(),
                metadata.getSoftwareVersions().getJava(),
                metadata.getSoftwareVersions().getSpringBoot(),
                metadata.getSoftwareVersions().getSpringFramework(),
                metadata.getSoftwareVersions().getKotlin(),
                metadata.getJdkVendor(),
                metadata.getCommitShortSha(),
                extractDeployTimestamp(deploymentAt, instanceId, instanceName),
                latestHeartBeat,
                convertServiceStatus(metadata.getHealthStatus()),
                new MemoryUsage(metadata.getMemoryDetails().getHeap()),
                instanceActuatorUrl);
    }

    private ApplicationId resolveApplicationId(String instanceId, BasicDiscoveryMetadata metadata) {
        if (!StringUtils.hasText(metadata.getGroupId()) || !StringUtils.hasText(metadata.getArtifactId())) {
            throw new IllegalArgumentException(
                    "Instance %s cannot be registered without a valid application id (both groupId and artifactId are mandatory)"
                            .formatted(instanceId));
        }

        return ApplicationId.of(metadata.getGroupId(), metadata.getArtifactId());
    }

    private InstanceStatus convertServiceStatus(HealthStatus healthStatus) {
        return switch (healthStatus) {
            case UP -> InstanceStatus.UP;
            case DOWN -> InstanceStatus.DOWN;
            case UNKNOWN -> InstanceStatus.UNKNOWN;
        };
    }

    @Nullable
    private Instant extractDeployTimestamp(String deploymentAt, String instanceId, String instanceName) {
        try {
            return OffsetDateTime.parse(deploymentAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .toInstant();
        } catch (DateTimeParseException e) {
            logger.warn("""
            Unable to parse the deployment timestamp of the Service : {} with name {}.
            That will affect the corresponding service on the wallboard UI
            """, instanceId, instanceName, e);
            return null;
        }
    }
}
