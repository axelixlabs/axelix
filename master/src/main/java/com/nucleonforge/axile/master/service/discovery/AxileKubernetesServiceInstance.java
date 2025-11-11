package com.nucleonforge.axile.master.service.discovery;

import java.net.URI;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;

import com.nucleonforge.axile.master.model.instance.Instance;

/**
 * Represents a Kubernetes service instance for {@link AxileKubernetesDiscoveryClient}.
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
public record AxileKubernetesServiceInstance(
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
