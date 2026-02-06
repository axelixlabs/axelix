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
package com.axelixlabs.axelix.sbs.spring.core.master;

import java.time.Instant;
import java.util.UUID;

import com.axelixlabs.axelix.common.api.registration.SelfRegistrationMetadata;
import com.axelixlabs.axelix.sbs.spring.core.config.SelfRegistrationConfigurationProperties;

/**
 * Default implementation of {@link SelfRegistrationMetadataAssembler}.
 *
 * @since 05.02.2026
 * @author Nikita Kirillov
 */
public class DefaultSelfRegistrationMetadataAssembler implements SelfRegistrationMetadataAssembler {

    private final SelfRegistrationConfigurationProperties selfRegistrationConfigurationProperties;

    private final ServiceMetadataAssembler serviceMetadataAssembler;

    private final String instanceId;

    private final String deploymentAt;

    private final String fullActuatorUrl;

    public DefaultSelfRegistrationMetadataAssembler(
            ServiceMetadataAssembler serviceMetadataAssembler,
            SelfRegistrationConfigurationProperties selfRegistrationConfigurationProperties,
            String baseActuatorPath) {
        this.selfRegistrationConfigurationProperties = selfRegistrationConfigurationProperties;
        this.serviceMetadataAssembler = serviceMetadataAssembler;
        this.instanceId = UUID.randomUUID().toString();
        this.deploymentAt = Instant.now().toString();
        this.fullActuatorUrl = joinPath(selfRegistrationConfigurationProperties.getInstanceUrl(), baseActuatorPath);
    }

    @Override
    public SelfRegistrationMetadata assemble() {
        return new SelfRegistrationMetadata(
                serviceMetadataAssembler.assemble(),
                instanceId,
                selfRegistrationConfigurationProperties.getInstanceName(),
                fullActuatorUrl,
                deploymentAt);
    }

    private String joinPath(String instanceUrl, String baseActuatorPath) {
        String cleanHost = instanceUrl.endsWith("/") ? instanceUrl.substring(0, instanceUrl.length() - 1) : instanceUrl;
        return cleanHost + baseActuatorPath;
    }
}
