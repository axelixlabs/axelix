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

import java.net.URI;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;

import com.axelixlabs.axelix.master.domain.Instance;

/**
 * Represents a self-registered service instance for {@link SelfRegisteredDiscoveryClient}.
 *
 * @param instanceId    unique identifier (uid) of the service
 * @param serviceId     id of the Service that managed this {@link Instance}.
 * @param serviceName   name of the Service.
 * @param host          container IP address.
 * @param port          service port.
 * @param deploymentAt  timestamp when the pod was created.
 *
 * @author Sergey Cherkasov
 */
public record SelfRegisteredServiceInstance(
        String instanceId, String serviceId, String serviceName, String host, int port, String deploymentAt)
        implements ServiceInstance {

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public URI getUri() {
        return URI.create("%s://%s:%d".formatted(isSecure() ? "https" : "http", host, port));
    }

    @Override
    public Map<String, String> getMetadata() {
        return Map.of();
    }

    public String getDeploymentAt() {
        return deploymentAt;
    }
}
