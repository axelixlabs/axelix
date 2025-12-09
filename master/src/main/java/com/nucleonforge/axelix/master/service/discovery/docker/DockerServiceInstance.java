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
package com.nucleonforge.axelix.master.service.discovery.docker;

import java.net.URI;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;

import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.service.discovery.AxileKubernetesDiscoveryClient;

/**
 * Represents a Kubernetes service instance for {@link AxileKubernetesDiscoveryClient}.
 *
 * @param instanceId unique identifier (uid) of the pod instance.
 * @param serviceId id of the Docker Compose Service that managed this {@link Instance}.
 * @param containerName name of the pod.
 * @param host container IP address.
 * @param port service port.
 * @param secure indicates if the connection should use HTTPS.
 * @param metadata additional metadata about the instance.
 * @param deploymentAt timestamp when the pod was created.
 *
 * @author Sergey Cherkasov
 */
public record DockerServiceInstance(
        String instanceId,
        String serviceId,
        String containerName,
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
        return Map.of();
    }

    public String getDeploymentAt() {
        return deploymentAt;
    }
}
