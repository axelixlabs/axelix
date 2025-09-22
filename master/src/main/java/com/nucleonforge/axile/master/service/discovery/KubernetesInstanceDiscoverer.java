package com.nucleonforge.axile.master.service.discovery;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.nucleonforge.axile.common.api.ManagedServiceMetadata;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.InstanceReference;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.service.transport.EndpointInvocationException;
import com.nucleonforge.axile.master.service.transport.ManagedServiceMetadataEndpointProber;

/**
 * Kubernetes implementation of {@link InstancesDiscoverer}.
 * <p>This service discovers running instances of services registered
 * in a Kubernetes cluster.</p>
 *
 * @author Mikhail Polivakha
 */
@Service
@ConditionalOnProperty(prefix = "axile.master.discovery", name = "execution-environment", havingValue = "k8s")
public class KubernetesInstanceDiscoverer implements InstancesDiscoverer {

    private static final Logger log = LoggerFactory.getLogger(KubernetesInstanceDiscoverer.class);

    private final DiscoveryClient discoveryClient;
    private final ManagedServiceMetadataEndpointProber managedServiceProber;

    public KubernetesInstanceDiscoverer(
            DiscoveryClient discoveryClient,
            ManagedServiceMetadataEndpointProber managedServiceMetadataEndpointProber) {
        this.discoveryClient = discoveryClient;
        this.managedServiceProber = managedServiceMetadataEndpointProber;
    }

    @Override
    public @NonNull Set<@NonNull InstanceReference> discover() {
        List<String> serviceIds = discoveryClient.getServices();

        if (CollectionUtils.isEmpty(serviceIds)) {
            log.error(
                    """
                Despite the auto-discovery was enabled, the {} did not found any result.
                That is almost certainly not the intended behavior. Please, revisit your configuration
                """,
                    this.getClass().getSimpleName());
            return Set.of();
        }

        Set<InstanceReference> result = new HashSet<>();

        for (String serviceId : serviceIds) {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);

            if (!CollectionUtils.isEmpty(instances)) {
                result.addAll(instances.stream()
                        .filter(Objects::nonNull)
                        .filter(this::isManagedInstance)
                        .map(this::toDomainInstance)
                        .collect(Collectors.toSet()));
            }
        }

        return result;
    }

    private boolean isManagedInstance(ServiceInstance serviceInstance) {
        String actuatorUrl = serviceInstance.getUri().toString() + "/actuator";
        try {
            ManagedServiceMetadata metadata = managedServiceProber.invoke(actuatorUrl, NoHttpPayload.INSTANCE);
            return isCompatibleVersion(serviceInstance, metadata);
        } catch (EndpointInvocationException ignored) {
            return false;
        }
    }

    private boolean isCompatibleVersion(ServiceInstance serviceInstance, ManagedServiceMetadata metadata) {
        // TODO: currently version hardcoded - is ok, waiting for issue #88 to be implemented
        if (metadata.version().equals("1.0.0-SNAPSHOT")) {
            return true;
        } else {
            log.warn("Service: {} have not a valid version", serviceInstance.getServiceId());
            return false;
        }
    }

    public InstanceReference toDomainInstance(ServiceInstance instance) {
        return new InstanceReference(
                InstanceId.of(instance.getInstanceId()), instance.getUri().toString() + "/actuator");
    }
}
