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

import com.axelixlabs.axelix.common.api.registration.SelfRegistrationMetadata;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.service.discovery.InstanceFactory;
import com.axelixlabs.axelix.master.service.discovery.ManagementInstance;

/**
 * Service responsible for creating and registering a new {@link Instance}
 *
 * @author Sergey Cherkasov
 */
public class SelfRegisteredServiceDiscoverer {

    private final ManagementInstance managementInstance;
    private final InstanceFactory instanceFactory;

    public SelfRegisteredServiceDiscoverer(ManagementInstance managementInstance, InstanceFactory instanceFactory) {
        this.managementInstance = managementInstance;
        this.instanceFactory = instanceFactory;
    }

    public void registerNewInstances(SelfRegistrationMetadata serviceMetadata) {
        Instance instance = instanceFactory.createInstance(
                serviceMetadata.getInstanceId(),
                serviceMetadata.getInstanceName(),
                serviceMetadata.getDeploymentAt(),
                serviceMetadata.getInstanceUrl(),
                serviceMetadata.getDefaultServiceMetadata());

        managementInstance.registerInstances(instance);
    }
}
