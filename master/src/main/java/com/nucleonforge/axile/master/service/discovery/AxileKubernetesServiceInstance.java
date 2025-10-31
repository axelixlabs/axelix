package com.nucleonforge.axile.master.service.discovery;

import java.net.URI;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;

/**
 * Represents a Kubernetes service instance for {@link AxileKubernetesDiscoveryClient}.
 *
 * @param instanceId unique identifier (uid) of the pod instance
 * @param serviceId name of the Kubernetes service
 * @param podName name of the pod
 * @param host pod IP address
 * @param port service port
 * @param isSecure indicates if the connection should use HTTPS
 * @param uri full URI to access the service instance
 * @param metadata additional metadata about the instance
 * @param deploymentAt timestamp when the pod was created
 *
 * @since 06.11.2025
 * @author Nikita Kirillov
 */
public record AxileKubernetesServiceInstance(
        String instanceId,
        String serviceId,
        String podName,
        String host,
        int port,
        boolean isSecure,
        URI uri,
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
        return isSecure;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public String getDeploymentAt() {
        return deploymentAt;
    }
}
