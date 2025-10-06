package com.nucleonforge.axile.master.service.discovery;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.kubernetes.commons.discovery.KubernetesServiceInstance;
import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.registration.ServiceMetadata;
import com.nucleonforge.axile.common.domain.Instance;
import com.nucleonforge.axile.common.domain.InstanceId;
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
public class KubernetesInstanceDiscoverer extends AbstractInstancesDiscoverer {

    private static final Logger log = LoggerFactory.getLogger(KubernetesInstanceDiscoverer.class);

    /**
     * The string key in K8S pod's metadata that signifies the pod's name.
     */
    public static final String POD_NAME = "name";

    /**
     * The string key that represent the pod's creation timestamp.
     */
    public static final String POD_CREATION_TIMESTAMP = "creationTimestamp";

    public KubernetesInstanceDiscoverer(
            DiscoveryClient discoveryClient,
            ManagedServiceMetadataEndpointProber managedServiceMetadataEndpointProber) {
        super(log, discoveryClient, managedServiceMetadataEndpointProber);
    }

    @Override
    @SuppressWarnings("NullAway")
    protected Instance toDomainInstance(InstanceIntermediateProfile profile) throws IllegalArgumentException {
        ServiceInstance serviceInstance = profile.serviceInstance();

        if (serviceInstance instanceof KubernetesServiceInstance k8sInstance) {

            if (k8sInstance.getMetadata() == null) {
                throw new IllegalArgumentException(
                        "Unable to register K8S pod '%s' as a managed instance - no metadata present on the pod"
                                .formatted(serviceInstance.getInstanceId()));
            }

            String podName = k8sInstance.getMetadata().get("app.kubernetes.io/name");
            Instant deployedAt = extractPodDeployTimestamp(k8sInstance);

            return new Instance(
                    InstanceId.of(k8sInstance.getInstanceId()),
                    podName,
                    profile.metadata().serviceVersion(),
                    profile.metadata().javaVersion(),
                    profile.metadata().springBootVersion(),
                    profile.metadata().commitShortSha(),
                    deployedAt,
                    mapStatus(profile),
                    serviceInstance.getUri().toString() + "/actuator");
        } else {
            throw new IllegalArgumentException(buildErrorMessage(serviceInstance));
        }
    }

    private static Instance.InstanceStatus mapStatus(InstanceIntermediateProfile profile) {
        ServiceMetadata.HealthStatus healthStatus = profile.metadata().healthStatus();

        if (healthStatus == null) {
            return Instance.InstanceStatus.UNKNOWN;
        }

        return switch (healthStatus) {
            case UP -> Instance.InstanceStatus.UP;
            case DOWN -> Instance.InstanceStatus.DOWN;
            case UNKNOWN -> Instance.InstanceStatus.UNKNOWN;
        };
    }

    @SuppressWarnings("NullAway")
    private static Instant extractPodDeployTimestamp(KubernetesServiceInstance k8sInstance) {
        String deployedAtAsString = k8sInstance.getMetadata().get(POD_CREATION_TIMESTAMP);

        try {
            if (deployedAtAsString == null) {
                log.warn(
                        "The K8S pod's {} {} filed in metadata is null",
                        k8sInstance.getInstanceId(),
                        POD_CREATION_TIMESTAMP);
                return null;
            }
            TemporalAccessor temporal =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.Z").parse(deployedAtAsString);
            return Instant.from(temporal);
        } catch (DateTimeException e) {
            log.warn(
                    """
                Unable to parse the deployment timestamp of the pod : {}.
                That will affect the corresponding service on the wallboard UI
                """,
                    k8sInstance.getInstanceId(),
                    e);
            return null;
        }
    }

    private static String buildErrorMessage(ServiceInstance serviceInstance) {
        return "Unable to register K8S pod '%s' as a managed instance - expected %s to be an instance of %s, but actually is %s"
                .formatted(
                        serviceInstance.getInstanceId(),
                        ServiceInstance.class.getSimpleName(),
                        KubernetesServiceInstance.class.getName(),
                        serviceInstance.getClass().getName());
    }
}
