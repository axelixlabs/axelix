package com.nucleonforge.axile.master.service.discovery;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * Axile Kubernetes implementation of {@link DiscoveryClient}.
 *
 * @since 05.11.2025
 * @author Nikita Kirillov
 */
public class AxileKubernetesDiscoveryClient implements DiscoveryClient {

    private static final Logger log = LoggerFactory.getLogger(KubernetesInstanceDiscoverer.class);

    private final KubernetesClient kubernetesClient;

    @Value("${spring.cloud.kubernetes.discovery.namespaces:}")
    private List<String> namespaces = new ArrayList<>();

    public AxileKubernetesDiscoveryClient(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @PostConstruct
    public void init() {
        if (namespaces == null || namespaces.isEmpty()) {
            String currentNamespace = kubernetesClient.getNamespace();

            if (currentNamespace == null || currentNamespace.trim().isEmpty()) {
                throw new IllegalStateException(
                        "Unable to determine current Kubernetes namespace. "
                                + "Either configure 'spring.cloud.kubernetes.discovery.namespaces' or run inside a Pod with serviceaccount.");
            }

            namespaces = Collections.singletonList(currentNamespace);
            log.info("DiscoveryClient using current namespace: {}", currentNamespace);
        } else {
            log.info("DiscoveryClient using configured namespaces: {}", namespaces);
        }
    }

    @Override
    public String description() {
        return "Axile Kubernetes Discovery Client";
    }

    @Override
    public List<ServiceInstance> getInstances(@NotNull String serviceId) {
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

    private List<ServiceInstance> buildInstances(
            String serviceId, String namespace, List<Pod> pods, List<ServicePort> ports) {

        return pods.stream()
                .filter(this::hasValidPodInfo)
                .flatMap(pod -> ports.stream()
                        .map(port -> createServiceInstance(serviceId, namespace, pod, port))
                        .filter(Objects::nonNull))
                .toList();
    }

    private boolean hasValidPodInfo(Pod pod) {
        return pod.getMetadata() != null
                && pod.getStatus() != null
                && pod.getStatus().getPodIP() != null
                && !pod.getStatus().getPodIP().isBlank();
    }

    @Nullable
    private ServiceInstance createServiceInstance(String serviceId, String namespace, Pod pod, ServicePort port) {

        boolean secure = "https".equalsIgnoreCase(port.getName());

        URI uri = createUri(pod.getStatus().getPodIP(), port, secure);
        if (uri == null) {
            return null;
        }

        Map<String, String> metadata = Map.of(
                "namespace", namespace,
                "servicePortName", port.getName(),
                "protocol", port.getProtocol());

        return new AxileKubernetesServiceInstance(
                pod.getMetadata().getUid(),
                serviceId,
                pod.getMetadata().getName(),
                pod.getStatus().getPodIP(),
                port.getPort(),
                secure,
                uri,
                metadata,
                pod.getMetadata().getCreationTimestamp());
    }

    @Nullable
    private URI createUri(String host, ServicePort sp, boolean cesure) {
        try {
            String protocol = cesure ? "https" : "http";
            return URI.create(protocol + "://" + host + ":" + sp.getPort());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid URI for pod IP '{}' and port '{}': {}", host, sp.getPort(), e.getMessage());
            return null;
        }
    }
}
