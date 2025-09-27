package com.nucleonforge.axile.master.service.discovery;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.nucleonforge.axile.common.domain.InstanceReference;

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

    @BeforeAll
    static void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutdownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldDiscoverManagedInstance() {
        String activeInstanceId = UUID.randomUUID().toString();

        // language=json
        String response = """
            {
              "version": "1.0.0-SNAPSHOT"
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert request.getPath() != null;

                if (path.equals("/" + activeInstanceId + "/actuator/axile-metadata")) {
                    return new MockResponse()
                            .setBody(response)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
        Mockito.when(serviceInstance.getInstanceId()).thenReturn(activeInstanceId);
        Mockito.when(serviceInstance.getUri())
                .thenReturn(URI.create(mockWebServer.url(activeInstanceId).toString()));

        Mockito.when(discoveryClient.getServices()).thenReturn(List.of(activeInstanceId));
        Mockito.when(discoveryClient.getInstances(activeInstanceId)).thenReturn(List.of(serviceInstance));

        Set<InstanceReference> instances = subject.discover();

        assertThat(instances).hasSize(1);
        InstanceReference instanceReference = instances.iterator().next();
        assertThat(instanceReference.id().instanceId()).isEqualTo(activeInstanceId);
        assertThat(instanceReference.actuatorUrl())
                .isEqualTo(mockWebServer.url(activeInstanceId + "/actuator").toString());
    }

    @Test
    void shouldRegisterOnlyCompatibleInstance() {
        String firstServiceId = UUID.randomUUID().toString();
        String firstServiceInstanceBadVersionId = UUID.randomUUID().toString();
        String firstServiceInstanceGoodVersionId = UUID.randomUUID().toString();

        String secondServiceId = UUID.randomUUID().toString();
        String secondServiceInstanceGoodVersionId = UUID.randomUUID().toString();

        // language=json
        String badVersionResponse =
                """
            {
              "version": "2.0.0-BAD-VERSION"
            }
            """;
        // language=json
        String goodVersionResponse =
                """
            {
              "version": "1.0.0-SNAPSHOT"
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + firstServiceInstanceBadVersionId + "/actuator/axile-metadata")) {
                    return new MockResponse()
                            .setBody(badVersionResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + firstServiceInstanceGoodVersionId + "/actuator/axile-metadata")
                        || path.equals("/" + secondServiceInstanceGoodVersionId + "/actuator/axile-metadata")) {
                    return new MockResponse()
                            .setBody(goodVersionResponse)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        // spotless:off
        // firstService -> bad application version instance
        ServiceInstance firstServiceBadVersion = Mockito.mock(ServiceInstance.class);
        Mockito.when(firstServiceBadVersion.getServiceId()).thenReturn(firstServiceId);
        Mockito.when(firstServiceBadVersion.getInstanceId()).thenReturn(firstServiceInstanceBadVersionId);
        Mockito.when(firstServiceBadVersion.getUri())
                .thenReturn(mockWebServer.url(firstServiceInstanceBadVersionId).uri());

        // firstService -> good application version instance
        ServiceInstance firstServiceGoodVersion = Mockito.mock(ServiceInstance.class);
        Mockito.when(firstServiceGoodVersion.getServiceId()).thenReturn(firstServiceId);
        Mockito.when(firstServiceGoodVersion.getInstanceId()).thenReturn(firstServiceInstanceGoodVersionId);
        Mockito.when(firstServiceGoodVersion.getUri())
            .thenReturn(mockWebServer.url(firstServiceInstanceGoodVersionId).uri());

        // secondService -> good application version instance
        ServiceInstance secondServiceGoodVersion = Mockito.mock(ServiceInstance.class);
        Mockito.when(secondServiceGoodVersion.getServiceId()).thenReturn(secondServiceId);
        Mockito.when(secondServiceGoodVersion.getInstanceId()).thenReturn(secondServiceInstanceGoodVersionId);
        Mockito.when(secondServiceGoodVersion.getUri())
            .thenReturn(mockWebServer.url(secondServiceInstanceGoodVersionId).uri());

        Mockito.when(discoveryClient.getServices()).thenReturn(List.of(firstServiceId, secondServiceId));
        Mockito.when(discoveryClient.getInstances(firstServiceId))
            .thenReturn(List.of(firstServiceBadVersion, firstServiceGoodVersion));
        Mockito.when(discoveryClient.getInstances(secondServiceId)).thenReturn(List.of(secondServiceGoodVersion));
        // spotless:on

        Set<InstanceReference> instances = subject.discover();

        assertThat(instances)
                .extracting(instance -> instance.id().instanceId())
                .containsOnly(firstServiceInstanceGoodVersionId, secondServiceInstanceGoodVersionId);
    }

    @Test
    void shouldIgnoreWhenDiscoveryClientReturnsEmpty() {
        Mockito.when(discoveryClient.getServices()).thenReturn(List.of());

        Set<InstanceReference> instances = subject.discover();

        assertThat(instances).isEmpty();
    }

    @Test
    void shouldIgnoreInstanceWhen404() {
        String okResponseInstanceId = UUID.randomUUID().toString();
        String notFoundInstanceId = UUID.randomUUID().toString();

        // language=json
        String response = """
        {
          "version": "1.0.0-SNAPSHOT"
        }
        """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + okResponseInstanceId + "/actuator/axile-metadata")) {
                    return new MockResponse()
                            .setBody(response)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else {
                    return new MockResponse().setResponseCode(404);
                }
            }
        });

        ServiceInstance okResponse = Mockito.mock(ServiceInstance.class);
        Mockito.when(okResponse.getInstanceId()).thenReturn(okResponseInstanceId);
        Mockito.when(okResponse.getUri())
                .thenReturn(mockWebServer.url(okResponseInstanceId).uri());

        ServiceInstance notFoundResponse = Mockito.mock(ServiceInstance.class);
        Mockito.when(notFoundResponse.getInstanceId()).thenReturn(notFoundInstanceId);
        Mockito.when(notFoundResponse.getUri())
                .thenReturn(mockWebServer.url(notFoundInstanceId).uri());

        Mockito.when(discoveryClient.getServices()).thenReturn(List.of("test-service"));
        Mockito.when(discoveryClient.getInstances("test-service")).thenReturn(List.of(okResponse, notFoundResponse));

        Set<InstanceReference> instances = subject.discover();

        assertThat(instances).extracting(instance -> instance.id().instanceId()).containsOnly(okResponseInstanceId);
    }

    @Test
    void shouldIgnoreInstanceWhenConnectionTimedOut() {
        String healthyInstanceId = UUID.randomUUID().toString();
        String timeoutInstanceId = UUID.randomUUID().toString();

        // language=json
        String response = """
        {
          "version": "1.0.0-SNAPSHOT"
        }
        """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + healthyInstanceId + "/actuator/axile-metadata")) {
                    return new MockResponse()
                            .setBody(response)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                } else if (path.equals("/" + timeoutInstanceId + "/actuator/axile-metadata")) {
                    try {
                        // Yeah, that sucks, but, we cannot just set timeout on the MockResponse, because
                        // of this OpenJDK HttpClient limitation https://bugs.openjdk.org/browse/JDK-8258397
                        Thread.sleep(6000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new MockResponse().setBody(response);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        ServiceInstance healthy = Mockito.mock(ServiceInstance.class);
        Mockito.when(healthy.getInstanceId()).thenReturn(healthyInstanceId);
        Mockito.when(healthy.getUri())
                .thenReturn(mockWebServer.url(healthyInstanceId).uri());

        ServiceInstance timedOut = Mockito.mock(ServiceInstance.class);
        Mockito.when(timedOut.getInstanceId()).thenReturn(timeoutInstanceId);
        Mockito.when(timedOut.getUri())
                .thenReturn(mockWebServer.url(timeoutInstanceId).uri());

        Mockito.when(discoveryClient.getServices()).thenReturn(List.of("test-service"));
        Mockito.when(discoveryClient.getInstances("test-service")).thenReturn(List.of(healthy, timedOut));

        Set<InstanceReference> instances = subject.discover();

        assertThat(instances).extracting(instance -> instance.id().instanceId()).containsOnly(healthyInstanceId);
    }

    @Test
    void shouldIgnoreInstanceWhenConnectionRefused() {
        String activeInstanceId = UUID.randomUUID().toString();
        String connectionRefusedInstanceId = UUID.randomUUID().toString();

        // language=json
        String response = """
        {
          "version": "1.0.0-SNAPSHOT"
        }
        """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert path != null;

                if (path.equals("/" + activeInstanceId + "/actuator/axile-metadata")) {
                    return new MockResponse()
                            .setBody(response)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        ServiceInstance healthy = Mockito.mock(ServiceInstance.class);
        Mockito.when(healthy.getInstanceId()).thenReturn(activeInstanceId);
        Mockito.when(healthy.getUri())
                .thenReturn(mockWebServer.url(activeInstanceId).uri());

        ServiceInstance connectionRefused = Mockito.mock(ServiceInstance.class);
        Mockito.when(connectionRefused.getInstanceId()).thenReturn(connectionRefusedInstanceId);
        // the assumption is that ports under 1024 cannot be allocated by mockwebserver (require root privileges)
        Mockito.when(connectionRefused.getUri()).thenReturn(URI.create("http://localhost:10"));

        Mockito.when(discoveryClient.getServices()).thenReturn(List.of("test-service"));
        Mockito.when(discoveryClient.getInstances("test-service")).thenReturn(List.of(healthy, connectionRefused));

        Set<InstanceReference> instances = subject.discover();

        assertThat(instances).extracting(instance -> instance.id().instanceId()).containsOnly(activeInstanceId);
    }
}
