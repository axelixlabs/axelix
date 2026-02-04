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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;

/**
 * Job that performs periodical discovering and refresh of managed service instances in the registry.
 *
 * @since 29.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public class ShortPollingInstanceDiscoveryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ShortPollingInstanceDiscoveryScheduler.class);

    private final InstancesDiscoverer instancesDiscoverer;
    private final ManagementInstance managementInstance;

    public ShortPollingInstanceDiscoveryScheduler(
            InstancesDiscoverer instancesDiscoverer, ManagementInstance managementInstance) {
        this.instancesDiscoverer = instancesDiscoverer;
        this.managementInstance = managementInstance;
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

        Set<InstanceId> currentlyRegisteredIds = managementInstance.getAllInstanceId();
        Set<InstanceId> discoveredIds = getDiscoveredIds(discoveredInstances);

        registerNewInstances(discoveredInstances);
        deregisterMissingInstances(currentlyRegisteredIds, discoveredIds);

        logger.debug("Registered instances: {}", currentlyRegisteredIds.size());
    }

    private Set<InstanceId> getDiscoveredIds(Set<Instance> discoveredInstances) {
        return discoveredInstances.stream().map(Instance::id).collect(Collectors.toSet());
    }

    private void registerNewInstances(Set<Instance> discoveredInstances) {
        for (Instance instance : discoveredInstances) {
            managementInstance.registerInstances(instance);
        }
    }

    private void deregisterMissingInstances(Set<InstanceId> currentlyRegisteredIds, Set<InstanceId> discoveredIds) {
        for (InstanceId existingId : currentlyRegisteredIds) {
            if (!discoveredIds.contains(existingId)) {
                try {
                    managementInstance.deregisterMissingInstances(existingId);
                    logger.debug("Deregistered instance: {}", existingId);
                } catch (InstanceNotFoundException e) {
                    logger.debug("Instance not found during deregistration: {}", existingId);
                }
            }
        }
    }
}
