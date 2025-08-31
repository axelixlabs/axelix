package com.nucleonforge.axile.master.service.discovery;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.domain.Instance;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

@Component
public class InstancesRegistrar {

    private static final Logger log = LoggerFactory.getLogger(InstancesRegistrar.class);
    private final DiscoveryConfig discoveryConfig;
    private final InstancesDiscoverer instancesDiscoverer;
    private final InstanceRegistry instanceRegistry;

    public InstancesRegistrar(
            DiscoveryConfig discoveryConfig,
            InstancesDiscoverer instancesDiscoverer,
            InstanceRegistry instanceRegistry) {
        this.discoveryConfig = discoveryConfig;
        this.instancesDiscoverer = instancesDiscoverer;
        this.instanceRegistry = instanceRegistry;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        if (discoveryConfig.auto()) {
            Set<Instance> discovered = instancesDiscoverer.discover();
            log.info("Discovered {} services. Their ids are : {}", discovered.size(), getServiceIds(discovered));
            for (Instance instance : discovered) {
                instanceRegistry.register(instance);
            }
        } else {
            log.info("Automatic discovery of services is not enabled, assuming the services will register themselves");
        }
    }

    private static Set<String> getServiceIds(Set<Instance> discovered) {
        return discovered.stream()
                .map(instance -> instance.getId().instanceId())
                .collect(Collectors.toSet());
    }
}
