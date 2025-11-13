package com.nucleonforge.axile.master.service.discovery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.nucleonforge.axile.common.utils.CollectionUtils;

/**
 * Axile Kubernetes implementation of {@link DiscoveryClient}.
 *
 * @since 05.11.2025
 * @author Nikita Kirillov
 */
public class AxileKubernetesDiscoveryClient implements DiscoveryClient {

    private static final Logger log = LoggerFactory.getLogger(AxileKubernetesDiscoveryClient.class);

    private final KubernetesClient kubernetesClient;
    private final Set<String> namespaces;

    public AxileKubernetesDiscoveryClient(KubernetesClient kubernetesClient, Set<String> namespaces) {
        this.namespaces = CollectionUtils.defaultIfEmpty(namespaces, kubernetesClient.getNamespace());
        this.kubernetesClient = kubernetesClient;
    }

    @Override
    public String description() {
        return "Axile Kubernetes Discovery Client";
    }

    @Override
    public List<ServiceInstance> getInstances(@NonNull String serviceId) {
        List<ServiceInstance> instances = new ArrayList<>();

        for (String namespace : namespaces) {
            Service service = getService(namespace, serviceId);
            if (service == null) {
                continue;
            }

            List<Pod> pods = getPodsForService(service, namespace);
            if (pods.isEmpty()) {
                continue;
            }

            List<ServicePort> ports = service.getSpec().getPorts();
            instances.addAll(buildInstances(serviceId, namespace, pods, ports));
        }

        return instances;
    }

    @Override
    public List<String> getServices() {
        Set<String> serviceNames = new HashSet<>();

        for (String namespace : namespaces) {

            // TODO: Can we query multiple namespaces at one time?
            ServiceList serviceList =
                    kubernetesClient.services().inNamespace(namespace).list();

            serviceNames.addAll(serviceList.getItems().stream()
                    .map(Service::getMetadata)
                    .filter(Objects::nonNull)
                    .map(ObjectMeta::getUid)
                    .filter(Objects::nonNull)
                    .toList());
        }

        return new ArrayList<>(serviceNames);
    }

    @Nullable
    private Service getService(String namespace, String serviceId) {

        // Yeah, that sucks, but we have to query all services and filter in memory since K8S API
        // does not allow to query by the resource UID for some reason.
        return kubernetesClient.services().inNamespace(namespace).list().getItems().stream()
                .filter(service ->
                        serviceId.equalsIgnoreCase(service.getMetadata().getUid()))
                .findFirst()
                .orElse(null);
    }

    private List<Pod> getPodsForService(Service service, String namespace) {
        Map<String, String> selectors = Optional.ofNullable(service.getSpec())
                .map(ServiceSpec::getSelector)
                .orElse(null);

        if (selectors == null || selectors.isEmpty()) {
            return List.of();
        }

        return kubernetesClient
                .pods()
                .inNamespace(namespace)
                .withLabels(selectors)
                .list()
                .getItems();
    }

    /**
     * Important: new {@link ServiceInstance} is created for every single {@link ServicePort}. The assumption is that
     * the client, that requested the API would filter out those services that are actually important.
     */
    private List<ServiceInstance> buildInstances(
            String serviceId, String namespace, List<Pod> pods, List<ServicePort> ports) {

        return pods.stream()
                .filter(this::hasValidPodInfo)
                .flatMap(pod -> ports.stream()
                        .map(port -> tryToCreateServiceInstance(serviceId, namespace, pod, port))
                        .filter(Objects::nonNull))
                .toList();
    }

    @Nullable
    private ServiceInstance tryToCreateServiceInstance(String serviceId, String namespace, Pod pod, ServicePort port) {
        try {
            return createServiceInstance(serviceId, namespace, pod, port);
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return null;
        }
    }

    private boolean hasValidPodInfo(Pod pod) {
        return pod.getMetadata() != null
                && pod.getStatus() != null
                && pod.getStatus().getPodIP() != null
                && !pod.getStatus().getPodIP().isBlank();
    }

    private ServiceInstance createServiceInstance(String serviceId, String namespace, Pod pod, ServicePort port)
            throws IllegalArgumentException {

        validateServicePort(serviceId, port);

        Map<String, String> metadata = Map.of(
                "namespace", namespace,
                "servicePortName", port.getName(),
                "protocol", port.getProtocol());

        return new AxileKubernetesServiceInstance(
                pod.getMetadata().getUid(),
                serviceId,
                pod.getMetadata().getName(),
                pod.getStatus().getPodIP(),
                port.getTargetPort().getIntVal(),
                "https".equalsIgnoreCase(port.getName()),
                metadata,
                pod.getMetadata().getCreationTimestamp());
    }

    private void validateServicePort(String serviceId, ServicePort servicePort) {
        Integer targetPort = servicePort.getTargetPort().getIntVal();

        if (targetPort == null) {
            throw new IllegalArgumentException(
                    """
                As of now, we do not support named K8S ports. \s
                The targetPort of the K8S '%s' is supposed to be an integer, \s
                but it is not. So, as of now, the service will not get registered. \s
                """
                            .formatted(serviceId));
        }
    }
}
