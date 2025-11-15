package com.nucleonforge.axile.master.service.discovery;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;

import com.nucleonforge.axile.master.exception.InstanceAlreadyRegisteredException;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

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

    public ShortPollingInstanceDiscoveryScheduler(
            InstancesDiscoverer instancesDiscoverer, InstanceRegistry instanceRegistry) {
        this.instancesDiscoverer = instancesDiscoverer;
        this.instanceRegistry = instanceRegistry;
    }

    @Scheduled(
            fixedDelayString = "${axile.master.discovery.polling.fixed-delay:60000}",
            initialDelayString = "${axile.master.discovery.polling.initial-delay:30000}")
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
                    logger.debug("Instance already registered: {}", instance.id());
                }
            }
        }
    }

    private void deregisterMissingInstances(Set<InstanceId> currentlyRegisteredIds, Set<InstanceId> discoveredIds) {
        for (InstanceId existingId : currentlyRegisteredIds) {
            if (!discoveredIds.contains(existingId)) {
                try {
                    instanceRegistry.deRegister(existingId);
                    logger.debug("Deregistered instance: {}", existingId);
                } catch (InstanceNotFoundException e) {
                    logger.debug("Instance not found during deregistration: {}", existingId);
                }
            }
        }
    }
}
