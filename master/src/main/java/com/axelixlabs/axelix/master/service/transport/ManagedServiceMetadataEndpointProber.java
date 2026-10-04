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
package com.axelixlabs.axelix.master.service.transport;

import java.time.Duration;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.registration.BasicRegistrationMetadata;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoint;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.service.discovery.probe.backoff.ProbeBackoffProperties;
import com.axelixlabs.axelix.master.service.serde.MessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#METADATA} endpoint.
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
@Service
public class ManagedServiceMetadataEndpointProber extends AbstractEndpointProber<BasicRegistrationMetadata> {

    private final ProbeBackoffProperties probeBackoffProperties;

    public ManagedServiceMetadataEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<BasicRegistrationMetadata> messageDeserializationStrategy,
            SecurityContextExecutor securityContextExecutor,
            ProbeBackoffProperties probeBackoffProperties) {
        super(instanceRegistry, messageDeserializationStrategy, securityContextExecutor);
        this.probeBackoffProperties = probeBackoffProperties;
    }

    /**
     * The metadata endpoint is probed during auto-discovery, and the probe lease is sized after this timeout.
     */
    @Override
    protected Duration requestTimeout() {
        return probeBackoffProperties.timeout();
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.METADATA;
    }
}
