package com.nucleonforge.axile.master.service.discovery;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * Class that serves as an entrypoint for the initial registration of all the managed services.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
public class InstancesRegistrar {

    private static final Logger log = LoggerFactory.getLogger(InstancesRegistrar.class);

    private final InstancesDiscoverer instancesDiscoverer;

    private final InstanceRegistry instanceRegistry;

    public InstancesRegistrar(InstancesDiscoverer instancesDiscoverer, InstanceRegistry instanceRegistry) {
        log.info(
                "Using {} as the primary instances auto-discovery mechanism",
                instancesDiscoverer.getClass().getName());

        this.instancesDiscoverer = instancesDiscoverer;
        this.instanceRegistry = instanceRegistry;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        Set<Instance> discovered = instancesDiscoverer.discoverSafely();

        if (discovered.isEmpty()) {
            log.error(
                    """
                    Despite the auto-discovery was enabled, the {} did not found any result.
                    That is almost certainly not the intended behavior. Please, revisit your configuration
                    """,
                    this.getClass().getSimpleName());
        } else {
            log.info("Discovered {} services. Their ids are : {}", discovered.size(), getServiceIds(discovered));
            for (Instance instance : discovered) {
                instanceRegistry.register(instance);
            }
        }
    }

    private static Set<String> getServiceIds(Set<Instance> discovered) {
        return discovered.stream().map(instance -> instance.id().instanceId()).collect(Collectors.toSet());
    }
}
