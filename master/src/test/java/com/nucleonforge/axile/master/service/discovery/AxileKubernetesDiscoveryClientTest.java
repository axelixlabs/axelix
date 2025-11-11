package com.nucleonforge.axile.master.service.discovery;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link AxileKubernetesDiscoveryClient}
 *
 * @since 10.11.2025
 * @author Nikita Kirillov
 */
@SpringBootTest
class AxileKubernetesDiscoveryClientTest {

    @Configuration
    static class TestConfig {
        @Bean
        @Primary
        public AxileKubernetesDiscoveryClient axileDiscoveryClient(KubernetesClient kubernetesClient) {
            return new AxileKubernetesDiscoveryClient(kubernetesClient, Set.of("prod", "staging"));
        }
    }

    @Autowired
    private AxileKubernetesDiscoveryClient discoveryClient;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private KubernetesClient kubernetesClient;

    private MixedOperation<Service, ServiceList, ServiceResource<Service>> servicesOp =
            mock(MixedOperation.class, RETURNS_DEEP_STUBS);

    private MixedOperation<Pod, PodList, PodResource> podsOp = mock(MixedOperation.class, RETURNS_DEEP_STUBS);

    @Test
    void shouldReturnServicesByUidFromMultipleNamespaces() {
        String namespace1 = "prod";
        String namespace2 = "staging";

        Service service1 = createMockService("service-1", "uid-1", namespace1);
        Service service2 = createMockService("service-2", "uid-2", namespace2);
        Service service3 = createMockService("service-3", "uid-3", namespace1);

        ServiceList serviceList1 = createServiceList(service1, service3);
        ServiceList serviceList2 = createServiceList(service2);

        when(kubernetesClient.services()).thenReturn(servicesOp);

        when(servicesOp.inNamespace(namespace1).list()).thenReturn(serviceList1);
        when(servicesOp.inNamespace(namespace2).list()).thenReturn(serviceList2);

        List<String> services = discoveryClient.getServices();

        assertThat(services).containsExactlyInAnyOrder("uid-1", "uid-2", "uid-3");
    }

    @Test
    void shouldReturnEmptyListWhenNoServicesFound() {
        when(kubernetesClient.services()).thenReturn(servicesOp);

        ServiceList emptyServiceList = createServiceList();

        when(servicesOp.inNamespace("prod").list()).thenReturn(emptyServiceList);
        when(servicesOp.inNamespace("staging").list()).thenReturn(emptyServiceList);

        List<String> services = discoveryClient.getServices();

        assertThat(services).isEmpty();
    }

    @Test
    void shouldFindServiceInstancesByServiceId() {
        String serviceUid = UUID.randomUUID().toString();

        String prodNamespace = "prod";
        Service prodService = createMockService("prod", serviceUid, prodNamespace, Map.of("app", "test-app"));
        Pod prodPod1 = createMockPod("prod-pod-1", "10.0.0.1", prodNamespace, Map.of("app", "test-app"));
        Pod prodPod2 = createMockPod("prod-pod-2", "10.0.0.2", prodNamespace, Map.of("app", "test-app"));

        String stagingNamespace = "staging";
        Service stagingService = createMockService("staging", serviceUid, stagingNamespace, Map.of("app", "test-app"));
        Pod stagingPod1 = createMockPod("staging-pod-1", "10.0.1.1", stagingNamespace, Map.of("app", "test-app"));
        Pod stagingPod2 = createMockPod("staging-pod-2", "10.0.1.2", stagingNamespace, Map.of("app", "test-app"));

        ServiceList prodServiceList = createServiceList(prodService);
        PodList prodPodList = createPodList(prodPod1, prodPod2);

        ServiceList stagingServiceList = createServiceList(stagingService);
        PodList stagingPodList = createPodList(stagingPod1, stagingPod2);

        when(kubernetesClient.services()).thenReturn(servicesOp);
        when(kubernetesClient.pods()).thenReturn(podsOp);

        // prod namespace
        when(servicesOp.inNamespace(prodNamespace).list()).thenReturn(prodServiceList);
        when(podsOp.inNamespace(prodNamespace).withLabels(anyMap()).list()).thenReturn(prodPodList);

        // staging namespace
        when(servicesOp.inNamespace(stagingNamespace).list()).thenReturn(stagingServiceList);
        when(podsOp.inNamespace(stagingNamespace).withLabels(anyMap()).list()).thenReturn(stagingPodList);

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceUid);

        assertThat(instances).hasSize(4);

        Map<String, List<Integer>> ipToPorts = instances.stream()
                .collect(Collectors.groupingBy(
                        ServiceInstance::getHost, Collectors.mapping(ServiceInstance::getPort, Collectors.toList())));

        assertThat(ipToPorts).containsKeys("10.0.0.1", "10.0.0.2", "10.0.1.1", "10.0.1.2");

        ipToPorts.values().forEach(ports -> assertThat(ports).containsExactly(8080));
    }

    @Test
    void shouldReturnEmptyInstancesWhenServiceNotFound() {
        ServiceList emptyServiceList = createServiceList();

        when(kubernetesClient.services()).thenReturn(servicesOp);

        when(servicesOp.inNamespace("prod").list()).thenReturn(emptyServiceList);
        when(servicesOp.inNamespace("staging").list()).thenReturn(emptyServiceList);

        List<ServiceInstance> instances = discoveryClient.getInstances("non-existent-uid");

        assertThat(instances).isEmpty();
    }

    @Test
    void shouldReturnEmptyInstancesWhenServiceHasNoSelector() {
        String serviceUid = "service-no-selector";
        String namespace = "prod";

        Service service = createMockService("service-no-selector", serviceUid, namespace, null);
        ServiceList serviceList = createServiceList(service);

        when(kubernetesClient.services()).thenReturn(servicesOp);
        when(servicesOp.inNamespace("prod").list()).thenReturn(serviceList);
        when(servicesOp.inNamespace("staging").list()).thenReturn(new ServiceList());

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceUid);

        assertThat(instances).isEmpty();
    }

    @Test
    void shouldReturnEmptyInstancesWhenNoPodsMatchSelector() {
        String serviceUid = "service-no-pods";
        String namespace = "prod";

        Service service = createMockService("staging", serviceUid, namespace, Map.of("app", "test-app"));
        ServiceList serviceList = createServiceList(service);
        PodList emptyPodList = createPodList();

        when(kubernetesClient.services()).thenReturn(servicesOp);
        when(kubernetesClient.pods()).thenReturn(podsOp);

        when(servicesOp.inNamespace("prod").list()).thenReturn(serviceList);
        when(servicesOp.inNamespace("staging").list()).thenReturn(new ServiceList());

        when(podsOp.inNamespace("prod").withLabels(anyMap()).list()).thenReturn(emptyPodList);

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceUid);

        assertThat(instances).isEmpty();
    }

    @Test
    void shouldSkipPodsWithoutIp() {
        String serviceUid = UUID.randomUUID().toString();
        String namespace = "prod";

        Service service = createMockService("prod-service", serviceUid, namespace, Map.of("app", "test-app"));
        Pod podWithIp = createMockPod("pod-with-ip", "10.0.0.1", namespace, Map.of("app", "test-app"));
        Pod podWithoutIp = createMockPod("pod-without-ip", null, namespace, Map.of("app", "test-app"));

        ServiceList serviceList = createServiceList(service);
        PodList podList = createPodList(podWithIp, podWithoutIp);

        when(kubernetesClient.services()).thenReturn(servicesOp);
        when(kubernetesClient.pods()).thenReturn(podsOp);

        when(servicesOp.inNamespace("prod").list()).thenReturn(serviceList);
        when(servicesOp.inNamespace("staging").list()).thenReturn(new ServiceList());

        when(podsOp.inNamespace("prod").withLabels(anyMap()).list()).thenReturn(podList);

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceUid);

        assertThat(instances).hasSize(1);
        assertThat(instances.get(0).getHost()).isEqualTo("10.0.0.1");
    }

    @Test
    void shouldCreateMultipleInstancesForMultiplePorts() {
        String serviceUid = UUID.randomUUID().toString();

        String prodNamespace = "prod";
        Service prodService = createMockServiceWithMultiplePorts(serviceUid, prodNamespace);
        Pod prodPod = createMockPod("prod-pod", "10.0.0.1", prodNamespace, Map.of("app", "multi-port-service"));

        String stagingNamespace = "staging";
        Service stagingService = createMockServiceWithMultiplePorts(serviceUid, stagingNamespace);
        Pod stagingPod =
                createMockPod("staging-pod", "10.0.1.1", stagingNamespace, Map.of("app", "multi-port-service"));

        ServiceList prodServiceList = createServiceList(prodService);
        PodList prodPodList = createPodList(prodPod);

        ServiceList stagingServiceList = createServiceList(stagingService);
        PodList stagingPodList = createPodList(stagingPod);

        when(kubernetesClient.services()).thenReturn(servicesOp);
        when(kubernetesClient.pods()).thenReturn(podsOp);

        // prod namespace
        when(servicesOp.inNamespace(prodNamespace).list()).thenReturn(prodServiceList);
        when(podsOp.inNamespace(prodNamespace).withLabels(anyMap()).list()).thenReturn(prodPodList);

        // staging namespace
        when(servicesOp.inNamespace(stagingNamespace).list()).thenReturn(stagingServiceList);
        when(podsOp.inNamespace(stagingNamespace).withLabels(anyMap()).list()).thenReturn(stagingPodList);

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceUid);

        assertThat(instances).hasSize(4);

        Map<String, List<Integer>> ipToPorts = instances.stream()
                .collect(Collectors.groupingBy(
                        ServiceInstance::getHost, Collectors.mapping(ServiceInstance::getPort, Collectors.toList())));

        assertThat(ipToPorts).containsKeys("10.0.0.1", "10.0.1.1");
        ipToPorts.values().forEach(ports -> assertThat(ports).containsExactlyInAnyOrder(8080, 8443));
    }

    @Test
    void shouldHandleServiceWithEmptySelector() {
        String serviceUid = UUID.randomUUID().toString();
        String namespace = "prod";

        Service service = createMockService("empty-selector-service", serviceUid, namespace, Collections.emptyMap());
        ServiceList serviceList = createServiceList(service);

        when(kubernetesClient.services()).thenReturn(servicesOp);

        when(servicesOp.inNamespace("prod").list()).thenReturn(serviceList);
        when(servicesOp.inNamespace("staging").list()).thenReturn(new ServiceList());

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceUid);

        assertThat(instances).isEmpty();
    }

    private Service createMockService(String name, String uid, String namespace) {
        return createMockService(name, uid, namespace, Map.of("app", name));
    }

    private Service createMockService(String name, String uid, String namespace, Map<String, String> selector) {
        Service service = mock(Service.class);
        ObjectMeta metadata = mock(ObjectMeta.class);
        ServiceSpec spec = mock(ServiceSpec.class);

        when(service.getMetadata()).thenReturn(metadata);
        when(service.getSpec()).thenReturn(spec);

        when(metadata.getName()).thenReturn(name);
        when(metadata.getUid()).thenReturn(uid);
        when(metadata.getNamespace()).thenReturn(namespace);

        when(spec.getSelector()).thenReturn(selector);

        ServicePort port = mock(ServicePort.class);
        when(port.getName()).thenReturn("http");
        when(port.getPort()).thenReturn(8080);
        when(port.getTargetPort()).thenReturn(new IntOrString(8080));
        when(port.getProtocol()).thenReturn("TCP");
        when(spec.getPorts()).thenReturn(List.of(port));

        return service;
    }

    private Service createMockServiceWithMultiplePorts(String uid, String namespace) {
        Service service = mock(Service.class);
        ObjectMeta metadata = mock(ObjectMeta.class);
        ServiceSpec spec = mock(ServiceSpec.class);

        when(service.getMetadata()).thenReturn(metadata);
        when(service.getSpec()).thenReturn(spec);

        when(metadata.getName()).thenReturn("multi-port-service");
        when(metadata.getUid()).thenReturn(uid);
        when(metadata.getNamespace()).thenReturn(namespace);
        when(spec.getSelector()).thenReturn(Map.of("app", "multi-port-service"));

        ServicePort httpPort = mock(ServicePort.class);
        when(httpPort.getName()).thenReturn("http");
        when(httpPort.getPort()).thenReturn(8080);
        when(httpPort.getTargetPort()).thenReturn(new IntOrString(8080));
        when(httpPort.getProtocol()).thenReturn("TCP");

        ServicePort httpsPort = mock(ServicePort.class);
        when(httpsPort.getName()).thenReturn("https");
        when(httpsPort.getPort()).thenReturn(8443);
        when(httpsPort.getTargetPort()).thenReturn(new IntOrString(8443));
        when(httpsPort.getProtocol()).thenReturn("TCP");

        when(spec.getPorts()).thenReturn(List.of(httpPort, httpsPort));

        return service;
    }

    private Pod createMockPod(String name, String ip, String namespace, Map<String, String> labels) {
        Pod pod = mock(Pod.class);
        ObjectMeta metadata = mock(ObjectMeta.class);
        PodStatus status = mock(PodStatus.class);

        when(pod.getMetadata()).thenReturn(metadata);
        when(pod.getStatus()).thenReturn(status);

        when(metadata.getName()).thenReturn(name);
        when(metadata.getNamespace()).thenReturn(namespace);
        when(metadata.getUid()).thenReturn(UUID.randomUUID().toString());
        when(metadata.getLabels()).thenReturn(labels);
        when(metadata.getCreationTimestamp()).thenReturn("2025-10-10T00:00:00Z");

        when(status.getPodIP()).thenReturn(ip);

        return pod;
    }

    private ServiceList createServiceList(Service... services) {
        ServiceList serviceList = mock(ServiceList.class);
        when(serviceList.getItems()).thenReturn(List.of(services));
        return serviceList;
    }

    private PodList createPodList(Pod... pods) {
        PodList podList = mock(PodList.class);
        when(podList.getItems()).thenReturn(List.of(pods));
        return podList;
    }
}
