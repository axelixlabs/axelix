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

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * Default implementation {@link InstanceFactory}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class DefaultInstanceManager implements InstanceManager {

    private final InstanceRegistry instanceRegistry;
    private final MemoryUsageCache memoryUsageCache;

    public DefaultInstanceManager(InstanceRegistry instanceRegistry, MemoryUsageCache memoryUsageCache) {
        this.instanceRegistry = instanceRegistry;
        this.memoryUsageCache = memoryUsageCache;
    }

    public void registerInstances(Instance instance) {
        instanceRegistry.replace(instance);
        memoryUsageCache.putHeapSize(instance.id(), instance.memoryUsage().heap());
    }

    public void deregisterMissingInstances(InstanceId instanceId) {
        instanceRegistry.deRegister(instanceId);
        memoryUsageCache.clear(instanceId);
    }

    public Set<InstanceId> getAllInstanceId() {
        return instanceRegistry.getAll().stream().map(Instance::id).collect(Collectors.toSet());
    }
}
