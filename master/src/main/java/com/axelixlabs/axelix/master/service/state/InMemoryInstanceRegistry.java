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
package com.axelixlabs.axelix.master.service.state;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceAlreadyRegisteredException;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;

/**
 * Implementation of the {@link InstanceRegistry} that holds the data in the process memory.
 *
 * @author Mikhail Polivakha
 */
@NullMarked
@Component
public class InMemoryInstanceRegistry implements InstanceRegistry {

    private final Logger logger = LoggerFactory.getLogger(InMemoryInstanceRegistry.class);
    private final ConcurrentMap<InstanceId, Instance> source;

    public InMemoryInstanceRegistry() {
        this.source = new ConcurrentHashMap<>();
    }

    @Override
    public void register(Instance instance) throws InstanceAlreadyRegisteredException {
        Instance peer = this.source.putIfAbsent(instance.id(), instance);

        if (peer != null) {
            throw new InstanceAlreadyRegisteredException();
        }
    }

    @Override
    public void deRegister(InstanceId instanceId) throws InstanceNotFoundException {
        Instance oldValue = source.remove(instanceId);

        if (oldValue == null) {
            throw new InstanceNotFoundException();
        }
    }

    @Override
    public void replace(Instance instance) {
        source.compute(instance.id(), (id, existing) -> {
            if (existing == null) {
                logger.debug("Registered new instance: {}", instance.id());
            } else {
                logger.debug("Instance '{}' was replaced inside the registry", instance.id());
            }
            return instance;
        });
    }

    @Override
    public Optional<Instance> get(InstanceId instanceId) {
        return Optional.ofNullable(source.get(instanceId));
    }

    @Override
    public Set<Instance> getAll() {
        return Set.copyOf(source.values());
    }
}
