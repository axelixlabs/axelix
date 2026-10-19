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

import java.net.http.HttpClient;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoint;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.contract.metadatav2.MetadataV2;
import com.axelixlabs.axelix.master.service.serde.MessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * {@link AbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#METADATA_V2} endpoint.
 *
 * @author Nikita Kirillov
 */
@Service
public class MetadataV2EndpointProber extends AbstractEndpointProber<MetadataV2> {

    public MetadataV2EndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<MetadataV2> messageDeserializationStrategy,
            SecurityContextExecutor securityContextExecutor,
            HttpClient httpClient) {
        super(instanceRegistry, messageDeserializationStrategy, securityContextExecutor, httpClient);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.METADATA_V2;
    }
}
