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
package com.axelixlabs.axelix.master.service.discovery.k8s;

import java.net.URI;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;

import com.axelixlabs.axelix.master.domain.Instance;

/**
 * Represents a Kubernetes service instance for {@link KubernetesDiscoveryClient}.
 *
 * @param instanceId unique identifier (uid) of the pod instance.
 * @param serviceId id of the Kubernetes Service that managed this {@link Instance}.
 * @param podName name of the pod.
 * @param host pod IP address.
 * @param port service port.
 * @param secure indicates if the connection should use HTTPS.
 * @param metadata additional metadata about the instance.
 * @param deploymentAt timestamp when the pod was created.
 *
 * @since 06.11.2025
 * @author Nikita Kirillov
 */
public record KubernetesServiceInstance(
        String instanceId,
        String serviceId,
        String podName,
        String host,
        int port,
        boolean secure,
        Map<String, String> metadata,
        String deploymentAt)
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
        return secure;
    }

    @Override
    public URI getUri() {
        return URI.create("%s://%s:%d".formatted(secure ? "https" : "http", host, port));
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public String getDeploymentAt() {
        return deploymentAt;
    }
}
