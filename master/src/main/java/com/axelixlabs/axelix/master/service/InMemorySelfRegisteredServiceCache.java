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
package com.axelixlabs.axelix.master.service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.NullMarked;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.selfregistered.SelfRegisteredServiceRequest;
import com.axelixlabs.axelix.master.domain.InstanceId;

/**
 * Implementation of the {@link SelfRegisteredServiceCache} that holds the data in the process memory.
 *
 * @author Sergey Cherkasov
 */
@NullMarked
@Service
public class InMemorySelfRegisteredServiceCache implements SelfRegisteredServiceCache {

    private final ConcurrentMap<InstanceId, SelfRegisteredServiceRequest> cache;

    public InMemorySelfRegisteredServiceCache() {
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<SelfRegisteredServiceRequest> getService(InstanceId instanceId) {
        return Optional.ofNullable(cache.get(instanceId));
    }

    @Override
    public void putService(SelfRegisteredServiceRequest service) {
        cache.put(InstanceId.of(service.serviceId()), service);
    }

    @Override
    public void clearService(InstanceId instanceId) {
        this.cache.remove(instanceId);
    }

    @Override
    public Set<SelfRegisteredServiceRequest> getAllServices() {
        return Set.copyOf(cache.values());
    }
}
