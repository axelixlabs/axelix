package com.nucleonforge.axile.master.service.discovery;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
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
                    .map(service -> service.getMetadata().getName())
                    .toList());
        }

        return new ArrayList<>(serviceNames);
    }

    @Nullable
    private Service getService(String namespace, String serviceId) {
        try {
            return kubernetesClient
                    .services()
                    .inNamespace(namespace)
                    .withName(serviceId)
                    .get();
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<Pod> getPodsForService(Service service, String namespace) {
        if (service.getSpec() == null) {
            return List.of();
        }

        Map<String, String> selector = service.getSpec().getSelector();
        if (selector == null || selector.isEmpty()) {
            return List.of();
        }

        try {
            PodList podList = kubernetesClient
                    .pods()
                    .inNamespace(namespace)
                    .withLabels(selector)
                    .list();

            return podList.getItems() != null ? podList.getItems() : List.of();
        } catch (Exception e) {
            log.warn(
                    "Failed to list pods for service '{}' in namespace '{}': {}",
                    service.getMetadata().getName(),
                    namespace,
                    e.getMessage());
            return List.of();
        }
    }

    private List<ServiceInstance> buildInstances(
            String serviceId, String namespace, List<Pod> pods, List<ServicePort> ports) {
        List<ServiceInstance> instances = new ArrayList<>();

        for (Pod pod : pods) {
            if (pod.getMetadata() == null || pod.getStatus() == null) {
                continue;
            }

            String podIp = pod.getStatus().getPodIP();
            if (podIp == null || podIp.isBlank()) {
                continue;
            }

            String podName = pod.getMetadata().getName();
            String deployTime = pod.getMetadata().getCreationTimestamp();

            for (ServicePort sp : ports) {
                boolean isSecure = "https".equalsIgnoreCase(sp.getName());
                URI uri = createUri(podIp, sp, isSecure);

                if (uri == null) {
                    continue;
                }

                Map<String, String> metadata = Map.of(
                        "namespace", namespace,
                        "servicePortName", sp.getName(),
                        "protocol", sp.getProtocol());

                AxileKubernetesServiceInstance instance = new AxileKubernetesServiceInstance(
                        pod.getMetadata().getUid(),
                        serviceId,
                        podName,
                        podIp,
                        sp.getPort(),
                        isSecure,
                        uri,
                        metadata,
                        deployTime);

                instances.add(instance);
            }
        }

        return instances;
    }

    @Nullable
    private URI createUri(String podIp, ServicePort sp, boolean isSecure) {
        try {
            return URI.create((isSecure ? "https" : "http") + "://" + podIp + ":" + sp.getPort());
        } catch (Exception e) {
            log.warn("Invalid URI for pod IP '{}' and port '{}': {}", podIp, sp.getPort(), e.getMessage());
            return null;
        }
    }
}
