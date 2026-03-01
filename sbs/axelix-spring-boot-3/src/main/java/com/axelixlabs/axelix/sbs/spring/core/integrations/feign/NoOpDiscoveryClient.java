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
package com.axelixlabs.axelix.sbs.spring.core.integrations.feign;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * Implementation of {@link DiscoveryClient} used as a fallback
 * when no real discovery integration is configured.
 *
 * @author Sergey Cherkasov
 */
public final class NoOpDiscoveryClient implements DiscoveryClient {
    @Override
    public String description() {
        return "No discovery client configured";
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        return List.of();
    }

    @Override
    public List<String> getServices() {
        return List.of();
    }
}
