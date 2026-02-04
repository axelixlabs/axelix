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

import java.util.Set;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * Allows interaction with {@link Instance} and manages their registration in the {@link InstanceRegistry}.
 *
 * @author Sergey Cherkasov
 */
public interface ManagementInstance {

    /**
     * Register the given {@link Instance} inside the registry.
     *
     * @param instance the {@link Instance} to be registered
     */
    void registerInstances(Instance instance);

    /**
     * Clear the record of the service with the given {@link InstanceId}.
     *
     * @param instanceId the id of the {@link Instance} to deregister.
     */
    void deregisterMissingInstances(InstanceId instanceId);

    /**
     * Get all {@link InstanceId} registered in {@link InstanceRegistry}.
     *
     * @return all {@link InstanceId}
     */
    Set<InstanceId> getAllInstanceId();
}
