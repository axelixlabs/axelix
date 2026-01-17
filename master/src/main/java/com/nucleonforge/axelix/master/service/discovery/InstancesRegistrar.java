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

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import com.nucleonforge.axelix.master.model.instance.Instance;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;

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

    @Async
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
