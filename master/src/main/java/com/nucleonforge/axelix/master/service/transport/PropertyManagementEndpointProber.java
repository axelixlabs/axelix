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

import org.springframework.stereotype.Service;

import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;

/**
 * {@link DiscardingAbstractEndpointProber} that specifically works with {@link ActuatorEndpoints#PROPERTY_MANAGEMENT /property-management} endpoint.
 *
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
@Service
public class PropertyManagementEndpointProber extends DiscardingAbstractEndpointProber {

    public PropertyManagementEndpointProber(InstanceRegistry instanceRegistry) {
        super(instanceRegistry, ActuatorEndpoints.PROPERTY_MANAGEMENT);
    }
}
