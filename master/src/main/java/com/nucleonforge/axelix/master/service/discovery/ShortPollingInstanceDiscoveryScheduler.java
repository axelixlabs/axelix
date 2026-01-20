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
package com.nucleonforge.axelix.master.service.discovery;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;

import com.nucleonforge.axelix.master.exception.InstanceAlreadyRegisteredException;
import com.nucleonforge.axelix.master.exception.InstanceNotFoundException;
import com.nucleonforge.axelix.master.model.instance.Instance;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.MemoryUsageCache;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;

/**
 * Job that performs periodical discovering and refresh of managed service instances in the registry.
 *
 * @since 29.10.2025
 * @author Nikita Kirillov
 */
public class ShortPollingInstanceDiscoveryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ShortPollingInstanceDiscoveryScheduler.class);

    private final InstancesDiscoverer instancesDiscoverer;
    private final InstanceRegistry instanceRegistry;
    private final MemoryUsageCache memoryUsageCache;

    public ShortPollingInstanceDiscoveryScheduler(
            InstancesDiscoverer instancesDiscoverer,
            InstanceRegistry instanceRegistry,
            MemoryUsageCache memoryUsageCache) {
        this.instancesDiscoverer = instancesDiscoverer;
        this.instanceRegistry = instanceRegistry;
        this.memoryUsageCache = memoryUsageCache;
    }

    @Scheduled(
            fixedDelayString = "${axelix.master.discovery.polling.fixed-delay:60000}",
            initialDelayString = "${axelix.master.discovery.polling.initial-delay:30000}")
    public void performDiscovery() {
        logger.debug("Starting instance discovery refresh cycle");

        Set<Instance> discoveredInstances = instancesDiscoverer.discover();
        Set<InstanceId> currentlyRegisteredIds = getCurrentlyRegisteredIds();
        Set<InstanceId> discoveredIds = getDiscoveredIds(discoveredInstances);

        registerNewInstances(discoveredInstances, currentlyRegisteredIds);
        deregisterMissingInstances(currentlyRegisteredIds, discoveredIds);

        logger.debug(
                "Instance discovery refresh completed. Registered instances: {}",
                instanceRegistry.getAll().size());
    }

    private Set<InstanceId> getCurrentlyRegisteredIds() {
        return instanceRegistry.getAll().stream().map(Instance::id).collect(Collectors.toSet());
    }

    private Set<InstanceId> getDiscoveredIds(Set<Instance> discoveredInstances) {
        return discoveredInstances.stream().map(Instance::id).collect(Collectors.toSet());
    }

    private void registerNewInstances(Set<Instance> discoveredInstances, Set<InstanceId> currentlyRegisteredIds) {
        for (Instance instance : discoveredInstances) {
            if (currentlyRegisteredIds.contains(instance.id())) {
                instanceRegistry.replace(instance);
            } else {
                try {
                    instanceRegistry.register(instance);
                    logger.debug("Registered new instance: {}", instance.id());
                } catch (InstanceAlreadyRegisteredException e) {
                    logger.warn(
                            "The Instance '{}' expected to be new, but found in registry. That is not expected and should be reported to maintainers",
                            instance.id());
                }
            }
            memoryUsageCache.putHeapSize(instance.id(), instance.memoryUsage().heap());
        }
    }

    private void deregisterMissingInstances(Set<InstanceId> currentlyRegisteredIds, Set<InstanceId> discoveredIds) {
        for (InstanceId existingId : currentlyRegisteredIds) {
            if (!discoveredIds.contains(existingId)) {
                try {
                    instanceRegistry.deRegister(existingId);
                    memoryUsageCache.clear(existingId);
                    logger.debug("Deregistered instance: {}", existingId);
                } catch (InstanceNotFoundException e) {
                    logger.debug("Instance not found during deregistration: {}", existingId);
                }
            }
        }
    }
}
