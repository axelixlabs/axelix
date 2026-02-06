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

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * An interface for creating a new {@link Instance}, to be subsequently registered in {@link InstanceRegistry}.
 *
 * @author Sergey Cherkasov
 */
public interface InstanceFactory {

    /**
     * The method for creating a new {@link Instance}.
     *
     * @param instanceId          unique identifier (uid) of the service
     * @param instanceName        name of the service
     * @param instanceActuatorUrl the URL of the service, including the postfix with actuator path, e.g. {@code https://my-app:6061/actuator}.
     *                            This postfix qualification is required, since actuator path is not guaranteed to always be {@code /actuator}
     * @param deploymentAt        timestamp when the service was created
     * @param metadata            the basic metadata of a service instance
     *
     * @return the created {@link Instance}.
     */
    Instance createInstance(
            String instanceId,
            String instanceName,
            String deploymentAt,
            String instanceActuatorUrl,
            BasicDiscoveryMetadata metadata);
}
