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

import io.fabric8.kubernetes.client.KubernetesClient;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.axelixlabs.axelix.common.domain.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.service.discovery.k8s.KubernetesServiceInstance;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ShortPollingInstanceDiscoveryScheduler}.
 *
 * @since 29.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(
        properties = {
            "axelix.master.discovery.auto=true",
            "axelix.master.discovery.platform=kubernetes",
            "axelix.master.discovery.polling.fixed-delay=1000",
            "axelix.master.discovery.polling.initial-delay=0"
        })
class ShortPollingInstanceDiscoverySchedulerTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private ShortPollingInstanceDiscoveryScheduler subject;

    @MockBean
    private KubernetesClient kubernetesClient;

    @Autowired
    private InstanceRegistry instanceRegistry;

    @MockBean
    private DiscoveryClient discoveryClient;

    private URI uri;

    @BeforeEach
    void setUp() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.close();
        }
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        instanceRegistry.getAll().forEach(instance -> {
            try {
                instanceRegistry.deRegister(instance.id());
            } catch (InstanceNotFoundException ignored) {
            }
        });
        uri = URI.create("http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort());
    }

    @TestConfiguration
    static class CurrentConfiguration {

        @Bean
        @Primary
        public AxelixVersionDiscoverer testAxelixVersionDiscoverer() {
            return () -> "1.0.0-SNAPSHOT";
        }
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
        String response = """
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

        mockWebServer.enqueue(
                new MockResponse().setBody(response).addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));
        mockWebServer.enqueue(
                new MockResponse().setBody(response).addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));

        ServiceInstance k8sInstance1 = Instancio.of(KubernetesServiceInstance.class)
                .set(Select.field("instanceId"), instance1Id)
                .set(Select.field("serviceId"), service1)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), uri.getHost())
                .set(Select.field("port"), uri.getPort())
                .create();

        ServiceInstance k8sInstance2 = Instancio.of(KubernetesServiceInstance.class)
                .set(Select.field("instanceId"), instance2Id)
                .set(Select.field("serviceId"), service2)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), uri.getHost())
                .set(Select.field("port"), uri.getPort())
                .create();

        Mockito.when(discoveryClient.getServices()).thenReturn(List.of(service1, service2));
        Mockito.when(discoveryClient.getInstances(service1)).thenReturn(List.of(k8sInstance1));
        Mockito.when(discoveryClient.getInstances(service2)).thenReturn(List.of(k8sInstance2));

        subject.performDiscovery();

        Set<Instance> registeredInstances = instanceRegistry.getAll();
        assertThat(registeredInstances).hasSize(2);

        assertThat(registeredInstances).extracting(it -> it.id().instanceId()).containsOnly(instance1Id, instance2Id);
        assertThat(registeredInstances)
                .flatExtracting(Instance::vmFeatures)
                .containsOnly(new Instance.VMFeature("AppCDS", "AppCDS Description", false));
    }

    @Test
    void shouldReplaceInstancesWhenDiscovered() {
        String service = "service-3";
        String instanceId = UUID.randomUUID().toString();

        // language=json
        String firstResponse = """
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

        // language=json
        String secondResponse = """
            {
              "version": "1.0.0-SNAPSHOT",
              "serviceVersion" : "3.5.0-SNAPSHOT",
              "commitShortSha" : "910230",
              "jdkVendor" : "BellSoft",
              "softwareVersions" : {
                "springBoot" : "3.5.2",
                "java" : "25",
                "springFramework" : "6.1.2",
                "kotlin" : null
              },
              "healthStatus" : "DOWN",
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

        mockWebServer.enqueue(
                new MockResponse().setBody(firstResponse).addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));
        mockWebServer.enqueue(
                new MockResponse().setBody(secondResponse).addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));

        ServiceInstance k8sInstance = Instancio.of(KubernetesServiceInstance.class)
                .set(Select.field("instanceId"), instanceId)
                .set(Select.field("serviceId"), service)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), uri.getHost())
                .set(Select.field("port"), uri.getPort())
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
            assertThat(instance.springBootVersion()).isEqualTo("3.5.2");
            assertThat(instance.vmFeatures())
                    .hasSize(1)
                    .containsOnly(new Instance.VMFeature("AppCDS", "AppCDS Description", false));
        });
    }

    @Test
    void shouldDeregisterK8sInstanceWhenNoLongerInDiscovery() {
        String serviceId = "test-service";
        String instanceId = UUID.randomUUID().toString();

        // language=json
        String response = """
            {
              "version": "1.0.0-SNAPSHOT",
              "serviceVersion" : "3.5.0-SNAPSHOT",
              "commitShortSha" : "910230",
              "jdkVendor" : "BellSoft",
              "softwareVersions" : {
                "springBoot" : "3.5.2",
                "java" : "25",
                "springFramework" : "6.1.2",
                "kotlin" : null
              },
              "healthStatus" : "DOWN",
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

        ServiceInstance k8sServiceInstance = Instancio.of(KubernetesServiceInstance.class)
                .set(Select.field("instanceId"), instanceId)
                .set(Select.field("serviceId"), serviceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), uri.getHost())
                .set(Select.field("port"), uri.getPort())
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
