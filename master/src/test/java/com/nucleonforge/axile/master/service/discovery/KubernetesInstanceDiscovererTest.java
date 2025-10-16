package com.nucleonforge.axile.master.service.discovery;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

import com.nucleonforge.axile.master.instance.Instance;

import static com.nucleonforge.axile.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link KubernetesInstanceDiscovererTest}.
 *
 * @author Nikita Kirillov
 * @since 21.09.2025
 */
@SpringBootTest(properties = {"axile.master.discovery.auto=true", "axile.master.discovery.execution-environment=k8s"})
class KubernetesInstanceDiscovererTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private KubernetesInstanceDiscoverer subject;

    @MockBean
    private DiscoveryClient discoveryClient;

    @BeforeEach
    void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void shutdownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldDiscoverManagedInstance() {
        String activeInstanceId = UUID.randomUUID().toString();

        // language=json
        String response =
                """
            {
              "version": "1.0.0-SNAPSHOT",
              "serviceVersion" : "3.5.0-SNAPSHOT",
              "commitShortSha" : "a8b0929",
              "javaVersion" : "17.0.14u",
              "springBootVersion" : "3.5.0",
              "healthStatus" : "UP"
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

        HttpUrl url = mockWebServer.url(activeInstanceId);

        // TODO: that is not compile-time safe. Can we do better?
        ServiceInstance serviceInstance = Instancio.of(DefaultKubernetesServiceInstance.class)
                .set(Select.field("instanceId"), activeInstanceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), url.host())
                .set(Select.field("port"), url.port())
                .create();

        Mockito.when(discoveryClient.getServices()).thenReturn(List.of(activeInstanceId));
        Mockito.when(discoveryClient.getInstances(activeInstanceId)).thenReturn(List.of(serviceInstance));

        Set<Instance> instances = subject.discover();

        assertThat(instances).hasSize(1);
        Instance instance = instances.iterator().next();
        assertThat(instance).satisfies(it -> {
            assertThat(it.serviceVersion()).isEqualTo("3.5.0-SNAPSHOT");
            assertThat(it.commitShaShort()).isEqualTo("a8b0929");
            assertThat(it.javaVersion()).isEqualTo("17.0.14u");
            assertThat(it.springBootVersion()).isEqualTo("3.5.0");
            assertThat(it.status()).isEqualTo(Instance.InstanceStatus.UP);
            assertThat(it.actuatorUrl())
                    .isEqualTo(mockWebServer.url("/actuator").toString());
        });
    }

    @Test
    void shouldRegisterOnlyCompatibleInstance() {
        String firstServiceId = UUID.randomUUID().toString();
        String firstServiceInstanceBadVersionId = UUID.randomUUID().toString();

        String secondServiceId = UUID.randomUUID().toString();
        String secondServiceInstanceGoodVersionId = UUID.randomUUID().toString();

        // language=json
        String badVersionResponse =
                """
            {
              "version": "2.0.0-BAD-VERSION",
              "serviceVersion" : "3.3.1-SNAPSHOT",
              "commitShortSha" : "k29dql2",
              "javaVersion" : "21.0.2",
              "springBootVersion" : "3.1.3",
              "healthStatus" : "UP"
            }
            """;
        // language=json
        String goodVersionResponse =
                """
            {
              "version": "1.0.0-SNAPSHOT",
              "serviceVersion" : "1.2.0",
              "commitShortSha" : "ei2o11a",
              "javaVersion" : "19.0.1",
              "springBootVersion" : "3.0.7",
              "healthStatus" : "DOWN"
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(badVersionResponse)
                .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));
        mockWebServer.enqueue(new MockResponse()
                .setBody(goodVersionResponse)
                .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));

        // spotless:off
        // firstService -> bad application version instance
        HttpUrl firstUrl = mockWebServer.url(firstServiceInstanceBadVersionId);

        ServiceInstance firstServiceBadVersion = Instancio
            .of(DefaultKubernetesServiceInstance.class)
            .set(Select.field("instanceId"), firstServiceInstanceBadVersionId)
            .set(Select.field("serviceId"), firstServiceId)
            .set(Select.field("secure"), false)
            .set(Select.field("host"), firstUrl.host())
            .set(Select.field("port"), firstUrl.port())
            .create();

        HttpUrl secondUrl = mockWebServer.url(secondServiceInstanceGoodVersionId);
        // secondService -> good application version instance
        ServiceInstance secondServiceGoodVersion = Instancio
            .of(DefaultKubernetesServiceInstance.class)
            .set(Select.field("instanceId"), secondServiceInstanceGoodVersionId)
            .set(Select.field("serviceId"), secondServiceId)
            .set(Select.field("secure"), false)
            .set(Select.field("host"), secondUrl.host())
            .set(Select.field("port"), secondUrl.port())
            .create();

        Mockito.when(discoveryClient.getServices()).thenReturn(List.of(firstServiceId, secondServiceId));
        Mockito.when(discoveryClient.getInstances(firstServiceId)).thenReturn(List.of(firstServiceBadVersion));
        Mockito.when(discoveryClient.getInstances(secondServiceId)).thenReturn(List.of(secondServiceGoodVersion));
        // spotless:on

        Set<Instance> instances = subject.discover();

        assertThat(instances)
                .extracting(instance -> instance.id().instanceId())
                .containsOnly(secondServiceInstanceGoodVersionId);
    }

    @Test
    void shouldIgnoreWhenDiscoveryClientReturnsEmpty() {
        Mockito.when(discoveryClient.getServices()).thenReturn(List.of());

        Set<Instance> instances = subject.discover();

        assertThat(instances).isEmpty();
    }

    @Test
    void shouldIgnoreInstanceWhen404() {
        String testServiceId = "test-service";
        String testInstanceId = UUID.randomUUID().toString();

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                return new MockResponse().setResponseCode(404);
            }
        });

        // secondService -> good application version instance
        HttpUrl httpUrl = mockWebServer.url(testInstanceId);

        ServiceInstance k8sPod = Instancio.of(DefaultKubernetesServiceInstance.class)
                .set(Select.field("instanceId"), testInstanceId)
                .set(Select.field("serviceId"), testServiceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), httpUrl.host())
                .set(Select.field("port"), httpUrl.port())
                .create();

        Mockito.when(discoveryClient.getServices()).thenReturn(List.of(testServiceId));
        Mockito.when(discoveryClient.getInstances(testServiceId)).thenReturn(List.of(k8sPod));

        Set<Instance> instances = subject.discover();

        assertThat(instances).extracting(instance -> instance.id().instanceId()).isEmpty();
    }

    @Test
    void shouldIgnoreInstanceWhenConnectionTimedOut() {
        String testServiceId = "test-service";

        String healthyInstanceId = UUID.randomUUID().toString();
        String timeoutInstanceId = UUID.randomUUID().toString();

        // language=json
        String response =
                """
            {
              "version": "1.0.0-SNAPSHOT",
              "serviceVersion" : "1.2.0",
              "commitShortSha" : "ei2o11a",
              "javaVersion" : "19.0.1",
              "springBootVersion" : "3.0.7",
              "healthStatus" : "DOWN"
            }
        """;

        mockWebServer.setDispatcher(new Dispatcher() {

            int counter = 0;

            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                // Yes, we rely on the ordering of the requests here, which is not a good thing
                // but alternatively setting up the DefaultKubernetesServiceInstance is very cumbersome
                if (counter++ == 0) {
                    return new MockResponse()
                            .setBody(response)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    try {
                        // Yeah, that sucks, but, we cannot just set timeout on the MockResponse, because
                        // of this OpenJDK HttpClient limitation https://bugs.openjdk.org/browse/JDK-8258397
                        Thread.sleep(6000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new MockResponse().setBody(response);
                }
            }
        });

        HttpUrl healthyUrl = mockWebServer.url(healthyInstanceId);

        ServiceInstance healthyK8sPod = Instancio.of(DefaultKubernetesServiceInstance.class)
                .set(Select.field("instanceId"), healthyInstanceId)
                .set(Select.field("serviceId"), testServiceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), healthyUrl.host())
                .set(Select.field("port"), healthyUrl.port())
                .create();

        HttpUrl timeoutUrl = mockWebServer.url(timeoutInstanceId);
        ServiceInstance timeoutK8sPod = Instancio.of(DefaultKubernetesServiceInstance.class)
                .set(Select.field("instanceId"), timeoutInstanceId)
                .set(Select.field("serviceId"), testServiceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), timeoutUrl.host())
                .set(Select.field("port"), timeoutUrl.port())
                .create();

        Mockito.when(discoveryClient.getServices()).thenReturn(List.of(testServiceId));
        Mockito.when(discoveryClient.getInstances(testServiceId)).thenReturn(List.of(healthyK8sPod, timeoutK8sPod));

        Set<Instance> instances = subject.discover();

        assertThat(instances).extracting(instance -> instance.id().instanceId()).containsOnly(healthyInstanceId);
    }

    @Test
    void shouldIgnoreInstanceWhenConnectionRefused() {
        String healthyK8SInstanceId = UUID.randomUUID().toString();
        String connectionRefusedInstanceId = UUID.randomUUID().toString();
        String testServiceId = "test-service";

        // language=json
        String response =
                """
            {
              "version": "1.0.0-SNAPSHOT",
              "serviceVersion" : "1.2.0",
              "commitShortSha" : "ei2o11a",
              "javaVersion" : "19.0.1",
              "springBootVersion" : "3.0.7",
              "healthStatus" : "DOWN"
            }
        """;

        mockWebServer.enqueue(
                new MockResponse().setBody(response).addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));

        HttpUrl healthyUrl = mockWebServer.url(healthyK8SInstanceId);

        ServiceInstance healthyK8SPod = Instancio.of(DefaultKubernetesServiceInstance.class)
                .set(Select.field("instanceId"), healthyK8SInstanceId)
                .set(Select.field("serviceId"), testServiceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), healthyUrl.host())
                .set(Select.field("port"), healthyUrl.port())
                .create();

        ServiceInstance connectionRefusedPod = Instancio.of(DefaultKubernetesServiceInstance.class)
                .set(Select.field("instanceId"), connectionRefusedInstanceId)
                .set(Select.field("serviceId"), testServiceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), healthyUrl.host())
                // the assumption is that ports under 1024 cannot be allocated by mockwebserver (require root
                // privileges)
                .set(Select.field("port"), 10)
                .create();

        Mockito.when(discoveryClient.getServices()).thenReturn(List.of(testServiceId));
        Mockito.when(discoveryClient.getInstances(testServiceId))
                .thenReturn(List.of(healthyK8SPod, connectionRefusedPod));

        Set<Instance> instances = subject.discover();

        assertThat(instances).extracting(instance -> instance.id().instanceId()).containsOnly(healthyK8SInstanceId);
    }
}
