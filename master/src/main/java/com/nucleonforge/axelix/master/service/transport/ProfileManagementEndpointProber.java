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
package com.nucleonforge.axelix.master.service.transport;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axelix.common.api.ProfileMutationResult;
import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoint;
import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axelix.master.service.serde.MessageDeserializationStrategy;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;

/**
 * {@link AbstractEndpointProber} that specifically works with
 * {@link ActuatorEndpoints#PROFILE_MANAGEMENT /profile-management} endpoint.
 *
 * @since 24.09.2025
 * @author Nikita Kirillov
 */
@Service
public class ProfileManagementEndpointProber extends AbstractEndpointProber<ProfileMutationResult> {

    public ProfileManagementEndpointProber(
            InstanceRegistry instanceRegistry,
            MessageDeserializationStrategy<ProfileMutationResult> messageDeserializationStrategy) {
        super(instanceRegistry, messageDeserializationStrategy);
    }

    @Override
    public @NonNull ActuatorEndpoint supports() {
        return ActuatorEndpoints.PROFILE_MANAGEMENT;
    }
}
