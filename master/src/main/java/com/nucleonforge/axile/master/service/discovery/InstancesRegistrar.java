package com.nucleonforge.axile.master.service.discovery;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.domain.InstanceReference;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

@Component
@ConditionalOnProperty(prefix = "axile.master.discovery", name = "auto", havingValue = "true")
public class InstancesRegistrar {

    private static final Logger log = LoggerFactory.getLogger(InstancesRegistrar.class);
    private final DiscoveryConfig discoveryConfig;
    private final InstancesDiscoverer instancesDiscoverer;
    private final InstanceRegistry instanceRegistry;

    public InstancesRegistrar(
            DiscoveryConfig discoveryConfig,
            InstancesDiscoverer instancesDiscoverer,
            InstanceRegistry instanceRegistry) {
        log.info(
                "Using {} as the primary instances auto-discovery mechanism",
                instancesDiscoverer.getClass().getName());

        this.discoveryConfig = discoveryConfig;
        this.instancesDiscoverer = instancesDiscoverer;
        this.instanceRegistry = instanceRegistry;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        if (discoveryConfig.auto()) {
            Set<InstanceReference> discovered = instancesDiscoverer.discoverSafely();

            if (discovered.isEmpty()) {
                log.error(
                        """
                Despite the auto-discovery was enabled, the {} did not found any result.
                That is almost certainly not the intended behavior. Please, revisit your configuration
                """,
                        this.getClass().getSimpleName());
            } else {
                log.info("Discovered {} services. Their ids are : {}", discovered.size(), getServiceIds(discovered));
                for (InstanceReference instanceReference : discovered) {
                    instanceRegistry.register(instanceReference);
                }
            }
        } /*else {  // TODO: intentionally commented out, waiting for issue #86 to be implemented
              log.info("Automatic discovery of services is not enabled, assuming the services will register themselves");
          }*/
    }

    private static Set<String> getServiceIds(Set<InstanceReference> discovered) {
        return discovered.stream().map(instance -> instance.id().instanceId()).collect(Collectors.toSet());
    }
}
