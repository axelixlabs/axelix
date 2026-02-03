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

import java.util.List;
import java.util.Set;

import org.jspecify.annotations.NonNull;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.axelixlabs.axelix.common.api.selfregistered.SelfRegisteredServiceRequest;
import com.axelixlabs.axelix.master.service.SelfRegisteredServiceCache;

/**
 * Custom Axelix Discovery implementation of {@link DiscoveryClient}.
 *
 * @author Sergey Cherkasov
 */
public class SelfRegisteredDiscoveryClient implements DiscoveryClient {

    /**
     * Client description {@link String}.
     */
    public static final String DESCRIPTION = "Axelix Discovery Client";

    private final SelfRegisteredServiceCache cache;

    public SelfRegisteredDiscoveryClient(SelfRegisteredServiceCache cache) {
        this.cache = cache;
    }

    public String description() {
        return DESCRIPTION;
    }

    @Override
    public List<ServiceInstance> getInstances(@NonNull String serviceId) {
        Set<SelfRegisteredServiceRequest> services = cache.getAllServices();

        return services.stream()
                .filter(s -> s.name().equals(serviceId))
                .map(this::createServiceInstance)
                .toList();
    }

    @Override
    public List<String> getServices() {
        return cache.getAllServices().stream()
                .map(SelfRegisteredServiceRequest::name)
                .toList();
    }

    public ServiceInstance createServiceInstance(SelfRegisteredServiceRequest request) {
        return new SelfRegisteredServiceInstance(
                request.serviceId(),
                request.name(),
                request.name(),
                request.address(),
                request.port(),
                request.deploymentAt());
    }
}
