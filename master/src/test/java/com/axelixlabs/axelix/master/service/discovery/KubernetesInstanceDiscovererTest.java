/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.service.discovery;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.axelixlabs.axelix.common.domain.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.service.DefaultInstanceFactory;
import com.axelixlabs.axelix.master.service.InMemoryMemoryUsageCache;
import com.axelixlabs.axelix.master.service.MemoryUsageCache;
import com.axelixlabs.axelix.master.service.discovery.k8s.KubernetesInstanceDiscoverer;
import com.axelixlabs.axelix.master.service.discovery.k8s.KubernetesServiceInstance;
import com.axelixlabs.axelix.master.service.serde.MetadataJacksonMessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.state.InMemoryInstanceRegistry;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.ManagedServiceMetadataEndpointProber;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link KubernetesInstanceDiscoverer}.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @since 21.09.2025
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = KubernetesInstanceDiscovererTest.CurrentConfig.class)
class KubernetesInstanceDiscovererTest {

    private static MockWebServer mockWebServer;

    @MockitoBean
    private DiscoveryClient discoveryClient;

    @Autowired
    private ManagedServiceMetadataEndpointProber managedServiceMetadataEndpointProber;

    @Autowired
    private MemoryUsageCache memoryUsageCache;

    @Autowired
    private AxelixVersionDiscoverer axelixVersionDiscoverer;

    @Autowired
    private DefaultInstanceFactory instanceFactory;

    private URI uri;

    private KubernetesInstanceDiscoverer subject;

    @TestConfiguration
    static class CurrentConfig {

        @Bean
        public ManagedServiceMetadataEndpointProber managedServiceMetadataEndpointProber(
                InstanceRegistry instanceRegistry,
                MetadataJacksonMessageDeserializationStrategy deserializationStrategy) {
            return new ManagedServiceMetadataEndpointProber(instanceRegistry, deserializationStrategy);
        }

        @Bean
        public InstanceRegistry instanceRegistry() {
            return new InMemoryInstanceRegistry();
        }

        @Bean
        public MetadataJacksonMessageDeserializationStrategy deserializationStrategy() {
            return new MetadataJacksonMessageDeserializationStrategy(new ObjectMapper());
        }

        @Bean
        public MemoryUsageCache memoryUsageCache() {
            return new InMemoryMemoryUsageCache();
        }

        @Bean
        public AxelixVersionDiscoverer axelixVersionDiscoverer() {
            return () -> "1.0.0-SNAPSHOT";
        }

        @Bean
        public DefaultInstanceFactory instanceFactory() {
            return new DefaultInstanceFactory();
        }
    }

    @BeforeEach
    void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        uri = URI.create("http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort());

        subject = new KubernetesInstanceDiscoverer(
                discoveryClient, managedServiceMetadataEndpointProber, axelixVersionDiscoverer, instanceFactory);
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
              "jdkVendor" : "BellSoft",
              "softwareVersions" : {
                "springBoot" : "3.5.0",
                "java" : "25",
                "springFramework" : "6.1.2",
                "kotlin" : null
              },
              "healthStatus" : "UP",
              "memoryDetails" : {
                "heap" : 12000
              },
              "vmFeatures": [
                   {
                     "name" : "AppCDS",
                     "description" : "AppCDS Description",
                     "enabled" : false
                   }
              ]
            }
            """;

        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = request.getPath();
                assert request.getPath() != null;

                if (path.equals("/actuator/axelix-metadata")) {
                    return new MockResponse()
                            .setBody(response)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                }
                return new MockResponse().setResponseCode(404);
            }
        });

        // TODO: that is not compile-time safe. Can we do better?
        ServiceInstance serviceInstance = Instancio.of(KubernetesServiceInstance.class)
                .set(Select.field("instanceId"), activeInstanceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), uri.getHost())
                .set(Select.field("port"), uri.getPort())
                .create();

        Mockito.when(discoveryClient.getServices()).thenReturn(List.of(activeInstanceId));
        Mockito.when(discoveryClient.getInstances(activeInstanceId)).thenReturn(List.of(serviceInstance));

        Set<Instance> instances = subject.discover();

        assertThat(instances).hasSize(1);
        Instance instance = instances.iterator().next();
        assertThat(instance).satisfies(it -> {
            assertThat(it.serviceVersion()).isEqualTo("3.5.0-SNAPSHOT");
            assertThat(it.commitShaShort()).isEqualTo("a8b0929");
            assertThat(it.javaVersion()).isEqualTo("25");
            assertThat(it.springBootVersion()).isEqualTo("3.5.0");
            assertThat(it.springFrameworkVersion()).isEqualTo("6.1.2");
            assertThat(it.kotlinVersion()).isNull();
            assertThat(it.status()).isEqualTo(Instance.InstanceStatus.UP);
            assertThat(it.vmFeatures())
                    .hasSize(1)
                    .first()
                    .isEqualTo(new Instance.VMFeature("AppCDS", "AppCDS Description", false));
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
              "serviceVersion" : "3.5.0-SNAPSHOT",
              "commitShortSha" : "a8b0929",
              "jdkVendor" : "BellSoft",
              "softwareVersions" : {
                "springBoot" : "3.5.0",
                "java" : "25",
                "springFramework" : "6.1.2",
                "kotlin" : null
              },
              "healthStatus" : "UP",
              "memoryDetails" : {
                "heap" : 12000
              },
              "vmFeatures": [
                   {
                     "name" : "AppCDS",
                     "description" : "AppCDS Description",
                     "enabled" : false
                   }
              ]
            }
            """;
        // language=json
        String goodVersionResponse =
                """
            {
              "version": "1.0.0-SNAPSHOT",
              "serviceVersion" : "3.5.0-SNAPSHOT",
              "commitShortSha" : "a8b0929",
              "jdkVendor" : "BellSoft",
              "softwareVersions" : {
                "springBoot" : "3.5.0",
                "java" : "25",
                "springFramework" : "6.1.2",
                "kotlin" : null
              },
              "healthStatus" : "UP",
              "memoryDetails" : {
                "heap" : 12000
              },
              "vmFeatures": [
                   {
                     "name" : "AppCDS",
                     "description" : "AppCDS Description",
                     "enabled" : false
                   }
              ]
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(badVersionResponse)
                .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));
        mockWebServer.enqueue(new MockResponse()
                .setBody(goodVersionResponse)
                .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));

        // firstService -> bad application version instance
        ServiceInstance firstServiceBadVersion = Instancio.of(KubernetesServiceInstance.class)
                .set(Select.field("instanceId"), firstServiceInstanceBadVersionId)
                .set(Select.field("serviceId"), firstServiceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), uri.getHost())
                .set(Select.field("port"), uri.getPort())
                .create();

        // secondService -> good application version instance
        ServiceInstance secondServiceGoodVersion = Instancio.of(KubernetesServiceInstance.class)
                .set(Select.field("instanceId"), secondServiceInstanceGoodVersionId)
                .set(Select.field("serviceId"), secondServiceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), uri.getHost())
                .set(Select.field("port"), uri.getPort())
                .create();

        Mockito.when(discoveryClient.getServices()).thenReturn(List.of(firstServiceId, secondServiceId));
        Mockito.when(discoveryClient.getInstances(firstServiceId)).thenReturn(List.of(firstServiceBadVersion));
        Mockito.when(discoveryClient.getInstances(secondServiceId)).thenReturn(List.of(secondServiceGoodVersion));

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

        ServiceInstance k8sPod = Instancio.of(KubernetesServiceInstance.class)
                .set(Select.field("instanceId"), testInstanceId)
                .set(Select.field("serviceId"), testServiceId)
                .set(Select.field("secure"), false)
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
              "serviceVersion" : "3.5.0-SNAPSHOT",
              "commitShortSha" : "a8b0929",
              "jdkVendor" : "BellSoft",
              "softwareVersions" : {
                "springBoot" : "3.5.0",
                "java" : "25",
                "springFramework" : "6.1.2",
                "kotlin" : null
              },
              "healthStatus" : "UP",
              "memoryDetails" : {
                "heap" : 12000
              },
              "vmFeatures": [
                   {
                     "name" : "AppCDS",
                     "description" : "AppCDS Description",
                     "enabled" : false
                   }
              ]
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

        ServiceInstance healthyK8sPod = Instancio.of(KubernetesServiceInstance.class)
                .set(Select.field("instanceId"), healthyInstanceId)
                .set(Select.field("serviceId"), testServiceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), uri.getHost())
                .set(Select.field("port"), uri.getPort())
                .create();

        ServiceInstance timeoutK8sPod = Instancio.of(KubernetesServiceInstance.class)
                .set(Select.field("instanceId"), timeoutInstanceId)
                .set(Select.field("serviceId"), testServiceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), uri.getHost())
                .set(Select.field("port"), uri.getPort())
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
              "serviceVersion" : "3.5.0-SNAPSHOT",
              "commitShortSha" : "a8b0929",
              "jdkVendor" : "BellSoft",
              "softwareVersions" : {
                "springBoot" : "3.5.0",
                "java" : "25",
                "springFramework" : "6.1.2",
                "kotlin" : null
              },
              "healthStatus" : "UP",
              "memoryDetails" : {
                "heap" : 12000
              },
              "vmFeatures": [
                   {
                     "name" : "AppCDS",
                     "description" : "AppCDS Description",
                     "enabled" : false
                   }
              ]
            }
        """;

        mockWebServer.enqueue(new MockResponse().setBody(response).addHeader("Content-Type", "application/json"));

        ServiceInstance healthyK8SPod = Instancio.of(KubernetesServiceInstance.class)
                .set(Select.field("instanceId"), healthyK8SInstanceId)
                .set(Select.field("serviceId"), testServiceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), uri.getHost())
                .set(Select.field("port"), uri.getPort())
                .create();

        ServiceInstance connectionRefusedPod = Instancio.of(KubernetesServiceInstance.class)
                .set(Select.field("instanceId"), connectionRefusedInstanceId)
                .set(Select.field("serviceId"), testServiceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), uri.getHost())
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
