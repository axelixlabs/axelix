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
    private KubernetesInstanceDiscoverer discoverer;

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

        Set<InstanceReference> instances = discoverer.discover();

        assertThat(instances).hasSize(1);
        InstanceReference instanceReference = instances.iterator().next();
        assertThat(instanceReference.id().instanceId()).isEqualTo(activeInstanceId);
        assertThat(instanceReference.actuatorUrl())
                .isEqualTo(mockWebServer.url(activeInstanceId + "/actuator").toString());
    }

    @Test
    void shouldRegisterInstanceAfterInitialIncompatibleVersion() {
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

        Set<InstanceReference> instances = discoverer.discover();

        assertThat(instances).hasSize(2);
        assertThat(instances).anySatisfy(instance -> assertThat(instance.id().instanceId())
                .isEqualTo(firstServiceInstanceGoodVersionId));
        assertThat(instances).anySatisfy(instance -> assertThat(instance.id().instanceId())
                .isEqualTo(secondServiceInstanceGoodVersionId));
        assertThat(instances).noneSatisfy(instance -> assertThat(instance.id().instanceId())
                .isEqualTo(firstServiceInstanceBadVersionId));
    }

    @Test
    void shouldIgnoreWhenDiscoveryClientReturnsEmpty() {
        Mockito.when(discoveryClient.getServices()).thenReturn(List.of());

        Set<InstanceReference> instances = discoverer.discover();

        assertThat(instances).isEmpty();
    }
}
