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

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceAlreadyRegisteredException;
import com.axelixlabs.axelix.master.service.MemoryUsageCache;
import com.axelixlabs.axelix.master.service.discovery.InstancesDiscoverer;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * Job that performs periodic discovery and refresh of self-registered service instances in the registry.
 *
 * @author Sergey Cherkasov
 */
public class SelfRegisteredShortPollingInstanceScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SelfRegisteredShortPollingInstanceScheduler.class);

    private final InstancesDiscoverer instancesDiscoverer;
    private final InstanceRegistry instanceRegistry;
    private final MemoryUsageCache memoryUsageCache;

    public SelfRegisteredShortPollingInstanceScheduler(
            InstancesDiscoverer instancesDiscoverer,
            InstanceRegistry instanceRegistry,
            MemoryUsageCache memoryUsageCache) {
        this.instancesDiscoverer = instancesDiscoverer;
        this.instanceRegistry = instanceRegistry;
        this.memoryUsageCache = memoryUsageCache;
    }

    @Scheduled(fixedDelayString = "${axelix.master.discovery.polling.fixed-delay:60000}")
    public void performDiscovery() {

        Set<Instance> discoveredInstances = instancesDiscoverer.discoverSafely();

        if (discoveredInstances.isEmpty()) {
            logger.error(
                    """
                Despite the auto-discovery was enabled, the {} did not found any result.
                That is almost certainly not the intended behavior. Please, revisit your configuration.
                """,
                    this.getClass().getSimpleName());
        }

        Set<InstanceId> currentlyRegisteredIds = getCurrentlyRegisteredIds();

        registerNewInstances(discoveredInstances, currentlyRegisteredIds);

        logger.debug("Registered instances: {}", instanceRegistry.getAll().size());
    }

    private Set<InstanceId> getCurrentlyRegisteredIds() {
        return instanceRegistry.getAll().stream().map(Instance::id).collect(Collectors.toSet());
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
                            "The Instance '{}' expected to be new, but found in registry. That is not expected and should be reported to maintainers.",
                            instance.id());
                }
            }
            memoryUsageCache.putHeapSize(instance.id(), instance.memoryUsage().heap());
        }
    }
}
