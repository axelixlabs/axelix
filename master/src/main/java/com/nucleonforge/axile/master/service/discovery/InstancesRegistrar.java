/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
