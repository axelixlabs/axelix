package com.nucleonforge.axile.master.service.discovery;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.kubernetes.commons.discovery.DefaultKubernetesServiceInstance;

import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ShortPollingInstanceDiscoveryScheduler}.
 *
 * @since 29.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(
        properties = {
            "axile.master.discovery.auto=true",
            "axile.master.discovery.execution-environment=k8s",
            "axile.master.discovery.polling.fixed-delay=1000",
            "axile.master.discovery.polling.initial-delay=0"
        })
class ShortPollingInstanceDiscoverySchedulerTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private ShortPollingInstanceDiscoveryScheduler subject;

    @Autowired
    private InstanceRegistry instanceRegistry;

    @Autowired
    private InstancesDiscoverer instancesDiscoverer;

    @MockBean
    private DiscoveryClient discoveryClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        instanceRegistry.getAll().forEach(instance -> {
            try {
                instanceRegistry.deRegister(instance.id());
            } catch (InstanceNotFoundException ignored) {
            }
        });
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldRegisterNewK8sInstancesWhenDiscovered() {
        String service1 = "service-1";
        String service2 = "service-2";
        String instance1Id = UUID.randomUUID().toString();
        String instance2Id = UUID.randomUUID().toString();

        // language=json
        String response =
                """
        {
          "version": "1.0.0-SNAPSHOT",
          "serviceVersion": "1.0.0",
          "commitShortSha": "a8b0929",
          "javaVersion": "17.0.0",
          "springBootVersion": "3.0.0",
          "healthStatus": "UP"
        }
        """;

        mockWebServer.enqueue(
                new MockResponse().setBody(response).addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));
        mockWebServer.enqueue(
                new MockResponse().setBody(response).addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));

        HttpUrl url1 = mockWebServer.url(instance1Id);
        HttpUrl url2 = mockWebServer.url(instance2Id);

        ServiceInstance k8sInstance1 = Instancio.of(DefaultKubernetesServiceInstance.class)
                .set(Select.field("instanceId"), instance1Id)
                .set(Select.field("serviceId"), service1)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), url1.host())
                .set(Select.field("port"), url1.port())
                .create();

        ServiceInstance k8sInstance2 = Instancio.of(DefaultKubernetesServiceInstance.class)
                .set(Select.field("instanceId"), instance2Id)
                .set(Select.field("serviceId"), service2)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), url2.host())
                .set(Select.field("port"), url2.port())
                .create();

        Mockito.when(discoveryClient.getServices()).thenReturn(List.of(service1, service2));
        Mockito.when(discoveryClient.getInstances(service1)).thenReturn(List.of(k8sInstance1));
        Mockito.when(discoveryClient.getInstances(service2)).thenReturn(List.of(k8sInstance2));

        subject.performDiscovery();

        Set<Instance> registeredInstances = instanceRegistry.getAll();
        assertThat(registeredInstances).hasSize(2);

        Set<String> registeredInstanceIds = registeredInstances.stream()
                .map(instance -> instance.id().instanceId())
                .collect(Collectors.toSet());

        assertThat(registeredInstanceIds).containsOnly(instance1Id, instance2Id);
    }

    @Test
    void shouldReplaceInstancesWhenDiscovered() {
        String service = "service-3";
        String instanceId = UUID.randomUUID().toString();

        // language=json
        String firstResponse =
                """
            {
              "version": "1.0.0-SNAPSHOT",
              "serviceVersion": "1.0.0",
              "commitShortSha": "a8b0929",
              "javaVersion": "17.0.0",
              "springBootVersion": "3.0.0",
              "healthStatus": "UP"
            }
            """;

        // language=json
        String secondResponse =
                """
            {
              "version": "1.0.0-SNAPSHOT",
              "serviceVersion": "1.0.0",
              "commitShortSha": "910230",
              "javaVersion": "17.0.0",
              "springBootVersion": "3.4.1",
              "healthStatus": "DOWN"
            }
            """;

        mockWebServer.enqueue(
                new MockResponse().setBody(firstResponse).addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));
        mockWebServer.enqueue(
                new MockResponse().setBody(secondResponse).addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));

        HttpUrl url = mockWebServer.url(instanceId);

        ServiceInstance k8sInstance = Instancio.of(DefaultKubernetesServiceInstance.class)
                .set(Select.field("instanceId"), instanceId)
                .set(Select.field("serviceId"), service)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), url.host())
                .set(Select.field("port"), url.port())
                .create();

        Mockito.when(discoveryClient.getServices()).thenReturn(List.of(service));
        Mockito.when(discoveryClient.getInstances(service)).thenReturn(List.of(k8sInstance));
        subject.performDiscovery();

        // when.
        subject.performDiscovery();

        // then.
        assertThat(instanceRegistry.getAll()).hasSize(1).allSatisfy(instance -> {
            assertThat(instance.id().instanceId()).isEqualTo(instanceId);
            assertThat(instance.status()).isEqualTo(Instance.InstanceStatus.DOWN);
            assertThat(instance.commitShaShort()).isEqualTo("910230");
            assertThat(instance.springBootVersion()).isEqualTo("3.4.1");
        });
    }

    @Test
    void shouldDeregisterK8sInstanceWhenNoLongerInDiscovery() {
        String serviceId = "test-service";
        String instanceId = UUID.randomUUID().toString();

        // language=json
        String response =
                """
            {
              "version": "1.0.0-SNAPSHOT",
              "serviceVersion": "3.5.0-SNAPSHOT",
              "commitShortSha": "a8b0929",
              "javaVersion": "17.0.14u",
              "springBootVersion": "3.5.0",
              "healthStatus": "UP"
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert request.getPath() != null;

                if (path.equals("/actuator/axile-metadata")) {
                    return new MockResponse()
                            .setBody(response)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        HttpUrl url = mockWebServer.url(instanceId);

        ServiceInstance k8sServiceInstance = Instancio.of(DefaultKubernetesServiceInstance.class)
                .set(Select.field("instanceId"), instanceId)
                .set(Select.field("serviceId"), serviceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), url.host())
                .set(Select.field("port"), url.port())
                .create();

        Mockito.when(discoveryClient.getServices())
                .thenReturn(List.of(serviceId))
                .thenReturn(List.of());

        Mockito.when(discoveryClient.getInstances(serviceId)).thenReturn(List.of(k8sServiceInstance));

        subject.performDiscovery();

        assertThat(instanceRegistry.getAll()).hasSize(1);

        subject.performDiscovery();

        assertThat(instanceRegistry.getAll()).isEmpty();
    }

    @Test
    void shouldHandleEmptyDiscoveryResponse() {
        Mockito.when(discoveryClient.getServices()).thenReturn(List.of());

        subject.performDiscovery();

        assertThat(instanceRegistry.getAll()).isEmpty();
    }
}
