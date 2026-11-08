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
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.axelixlabs.axelix.common.auth.core.JwtDefaults;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtEncoderService;
import com.axelixlabs.axelix.common.auth.service.HmacVerificationStrategy;
import com.axelixlabs.axelix.common.auth.service.tech.DefaultTechUserJwtSerializer;
import com.axelixlabs.axelix.common.contract.masterping.MasterPing;
import com.axelixlabs.axelix.common.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.common.utils.SemanticVersion;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.JavaVersion;
import com.axelixlabs.axelix.master.domain.MemoryUsage;
import com.axelixlabs.axelix.master.domain.instance.Instance;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.service.DefaultInstanceFactory;
import com.axelixlabs.axelix.master.service.auth.StarterAuthenticationTokenProvider;
import com.axelixlabs.axelix.master.service.discovery.k8s.KubernetesInstanceDiscoverer;
import com.axelixlabs.axelix.master.service.discovery.k8s.KubernetesServiceInstance;
import com.axelixlabs.axelix.master.service.serde.DefaultJacksonMessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.DefaultEndpointProber;
import com.axelixlabs.axelix.master.service.transport.EndpointProber;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestMetadataFactory;
import com.axelixlabs.axelix.master.utils.auth.StaticTestSecurityContextExecutor;

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

    @MockitoBean
    private InstanceRegistry instanceRegistry;

    @Autowired
    private EndpointProber<BasicRegistrationMetadata> managedServiceMetadataEndpointProber;

    @Autowired
    private EndpointProber<MasterPing> masterPingEndpointProber;

    @Autowired
    private CompatibilityDetectionStrategy compatibilityDetectionStrategy;

    @Autowired
    private AxelixVersionDiscoverer axelixVersionDiscoverer;

    @Autowired
    private DefaultInstanceFactory instanceFactory;

    private KubernetesDiscoveryStubs stubs;

    private KubernetesInstanceDiscoverer subject;

    @TestConfiguration
    static class CurrentConfig {

        @Bean
        public StarterAuthenticationTokenProvider tokenProvider(SecurityContextExecutor securityContextExecutor) {
            return new StarterAuthenticationTokenProvider(
                    securityContextExecutor,
                    new DefaultJwtEncoderService(
                            JwtDefaults.DEFAULT_ALGORITHM, JwtDefaults.DEFAULT_SIGNING_KEY, Duration.ofMinutes(5)),
                    new DefaultTechUserJwtSerializer(JwtDefaults.DEFAULT_ALGORITHM, JwtDefaults.DEFAULT_SIGNING_KEY));
        }

        @Bean
        public SecurityContextExecutor securityContextExecutor() {
            return new StaticTestSecurityContextExecutor();
        }

        @Bean
        public DefaultEndpointProber<BasicRegistrationMetadata> managedServiceMetadataEndpointProber(
                InstanceRegistry instanceRegistry,
                StarterAuthenticationTokenProvider tokenProvider,
                HttpClient httpClient) {
            return new DefaultEndpointProber<>(
                    instanceRegistry,
                    new DefaultJacksonMessageDeserializationStrategy<>(
                            new ObjectMapper(), BasicRegistrationMetadata.class),
                    tokenProvider,
                    ActuatorEndpoints.METADATA,
                    httpClient);
        }

        @Bean
        public HttpClient httpClient() {
            return HttpClient.newHttpClient();
        }

        @Bean
        public DefaultEndpointProber<MasterPing> masterPingEndpointProber(
                InstanceRegistry instanceRegistry,
                StarterAuthenticationTokenProvider tokenProvider,
                HttpClient httpClient) {
            return new DefaultEndpointProber<>(
                    instanceRegistry,
                    new DefaultJacksonMessageDeserializationStrategy<>(new ObjectMapper(), MasterPing.class),
                    tokenProvider,
                    ActuatorEndpoints.PING,
                    httpClient);
        }

        @Bean
        public AxelixVersionDiscoverer axelixVersionDiscoverer() {
            return () -> "1.3.0-SNAPSHOT";
        }

        @Bean
        public CompatibilityDetectionStrategy compatibilityDetectionStrategy(
                AxelixVersionDiscoverer axelixVersionDiscoverer) {
            return new WindowCompatibilityDetectionStrategy(axelixVersionDiscoverer);
        }

        @Bean
        public DefaultInstanceFactory instanceFactory() {
            return new DefaultInstanceFactory();
        }
    }

    @BeforeEach
    void startServer() throws IOException {
        Mockito.doCallRealMethod().when(instanceRegistry).getOrThrow(Mockito.any(InstanceId.class));

        mockWebServer = new MockWebServer();
        mockWebServer.start();
        stubs = new KubernetesDiscoveryStubs(discoveryClient, mockWebServer);

        subject = new KubernetesInstanceDiscoverer(
                discoveryClient,
                managedServiceMetadataEndpointProber,
                masterPingEndpointProber,
                compatibilityDetectionStrategy,
                instanceRegistry,
                instanceFactory);
    }

    @AfterEach
    void shutdownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldDiscoverManagedInstance() {
        String activeInstanceId = UUID.randomUUID().toString();

        String response =
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "petclinic"));

        stubs.respondOn("/actuator/axelix-metadata", response);
        stubs.stubDiscovery(stubs.k8sInstance(activeInstanceId, activeInstanceId));

        Set<DiscoveredInstanceProfile> profiles = subject.discover();

        assertThat(profiles).hasSize(1);
        Instance instance = profiles.iterator().next().instance();
        assertThat(instance).satisfies(it -> {
            assertThat(it.serviceVersion()).isEqualTo("3.5.0-SNAPSHOT");
            assertThat(it.commitShaShort()).isEqualTo("a8b0929");
            assertThat(it.javaVersion()).isEqualTo(new JavaVersion(25, 0, 0, 0));
            assertThat(it.springBootVersion()).isEqualTo("3.5.0");
            assertThat(it.springFrameworkVersion()).isEqualTo("6.1.2");
            assertThat(it.kotlinVersion()).isNull();
            assertThat(it.status()).isEqualTo(Instance.InstanceStatus.UP);
            assertThat(it.actuatorUrl())
                    .isEqualTo(mockWebServer.url("/actuator").toString());
        });
    }

    @Test
    void shouldDiscoverInstanceWhenGroupIdIsEmpty() {
        String activeInstanceId = UUID.randomUUID().toString();

        stubs.enqueueResponses(TestMetadataFactory.toJson(TestMetadataFactory.create("", "petclinic")));
        stubs.stubDiscovery(stubs.k8sInstance(activeInstanceId, activeInstanceId));

        Set<DiscoveredInstanceProfile> profiles = subject.discover();

        // the instance is discovered and identified by its artifactId alone.
        assertThat(profiles).hasSize(1);
        assertThat(profiles.iterator().next().instance().applicationId()).isEqualTo(ApplicationId.of("", "petclinic"));
    }

    @Test
    void shouldNotRegisterInstanceWhenStarterIsNewerThanMaster() {
        String serviceId = UUID.randomUUID().toString();
        String instanceId = UUID.randomUUID().toString();

        stubs.enqueueResponses(
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "petclinic")
                        .version("1.5.0-SNAPSHOT")));
        stubs.stubDiscovery(stubs.k8sInstance(serviceId, instanceId));

        Set<DiscoveredInstanceProfile> profiles = subject.discover();

        assertThat(profiles).isEmpty();
    }

    @Test
    void shouldRegisterOnlyCompatibleInstance() {
        String firstServiceId = UUID.randomUUID().toString();
        String firstServiceInstanceBadVersionId = UUID.randomUUID().toString();

        String secondServiceId = UUID.randomUUID().toString();
        String secondServiceInstanceGoodVersionId = UUID.randomUUID().toString();

        String badVersionResponse =
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "petclinic")
                        .version("2.0.0-BAD-VERSION"));
        String goodVersionResponse =
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "petclinic"));

        stubs.enqueueResponses(badVersionResponse, goodVersionResponse);
        stubs.stubDiscovery(
                stubs.k8sInstance(firstServiceId, firstServiceInstanceBadVersionId),
                stubs.k8sInstance(secondServiceId, secondServiceInstanceGoodVersionId));

        Set<DiscoveredInstanceProfile> profiles = subject.discover();

        assertThat(profiles)
                .extracting(profile -> profile.instance().id().instanceId())
                .containsOnly(secondServiceInstanceGoodVersionId);
    }

    @Test
    void shouldIgnoreWhenDiscoveryClientReturnsEmpty() {
        Mockito.when(discoveryClient.getServices()).thenReturn(List.of());

        Set<DiscoveredInstanceProfile> profiles = subject.discover();

        assertThat(profiles).isEmpty();
    }

    @Test
    void shouldIgnoreInstanceWhen404() {
        String testServiceId = "test-service";
        String testInstanceId = UUID.randomUUID().toString();

        mockWebServer.enqueue(new MockResponse().setResponseCode(404));
        stubs.stubDiscovery(stubs.k8sInstance(testServiceId, testInstanceId));

        Set<DiscoveredInstanceProfile> profiles = subject.discover();

        assertThat(profiles)
                .extracting(profile -> profile.instance().id().instanceId())
                .isEmpty();
    }

    @Test
    void shouldIgnoreInstanceWhenConnectionTimedOut() {
        String testServiceId = "test-service";

        String healthyInstanceId = UUID.randomUUID().toString();
        String timeoutInstanceId = UUID.randomUUID().toString();

        String response =
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "petclinic"));

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

        stubs.stubDiscovery(
                stubs.k8sInstance(testServiceId, healthyInstanceId),
                stubs.k8sInstance(testServiceId, timeoutInstanceId));

        Set<DiscoveredInstanceProfile> profiles = subject.discover();

        assertThat(profiles)
                .extracting(profile -> profile.instance().id().instanceId())
                .containsOnly(healthyInstanceId);
    }

    @Test
    void shouldIgnoreInstanceWhenApplicationIdIsMissing() {
        String testServiceId = "test-service";
        String testInstanceId = UUID.randomUUID().toString();

        stubs.enqueueResponses(TestMetadataFactory.toJson(TestMetadataFactory.create(null, null)));
        stubs.stubDiscovery(stubs.k8sInstance(testServiceId, testInstanceId));

        Set<DiscoveredInstanceProfile> profiles = subject.discover();

        assertThat(profiles).isEmpty();
    }

    @Test
    void shouldRegisterOnlyInstanceWithApplicationId() {
        String testServiceId = "test-service";
        String instanceWithoutApplicationId = UUID.randomUUID().toString();
        String instanceWithApplicationId = UUID.randomUUID().toString();

        String responseWithoutApplicationId = TestMetadataFactory.toJson(TestMetadataFactory.create(null, null));
        String responseWithApplicationId =
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "petclinic"));

        stubs.enqueueResponses(responseWithoutApplicationId, responseWithApplicationId);
        stubs.stubDiscovery(
                stubs.k8sInstance(testServiceId, instanceWithoutApplicationId),
                stubs.k8sInstance(testServiceId, instanceWithApplicationId));

        Set<DiscoveredInstanceProfile> profiles = subject.discover();

        assertThat(profiles)
                .extracting(profile -> profile.instance().id().instanceId())
                .containsOnly(instanceWithApplicationId);
    }

    @Test
    void shouldIgnoreInstanceWhenConnectionRefused() {
        String healthyK8SInstanceId = UUID.randomUUID().toString();
        String connectionRefusedInstanceId = UUID.randomUUID().toString();
        String testServiceId = "test-service";

        String response =
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "petclinic"));

        mockWebServer.enqueue(new MockResponse().setBody(response).addHeader("Content-Type", "application/json"));

        ServiceInstance connectionRefusedPod = Instancio.of(KubernetesServiceInstance.class)
                .set(Select.field("instanceId"), connectionRefusedInstanceId)
                .set(Select.field("serviceId"), testServiceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), mockWebServer.getHostName())
                // the assumption is that ports under 1024 cannot be allocated by mockwebserver (require root
                // privileges)
                .set(Select.field("port"), 10)
                .create();

        stubs.stubDiscovery(stubs.k8sInstance(testServiceId, healthyK8SInstanceId), connectionRefusedPod);

        Set<DiscoveredInstanceProfile> profiles = subject.discover();

        assertThat(profiles)
                .extracting(profile -> profile.instance().id().instanceId())
                .containsOnly(healthyK8SInstanceId);
    }

    @Test
    void shouldRefreshKnownInstanceViaMasterPingEndpoint() throws InterruptedException {
        String serviceId = UUID.randomUUID().toString();
        String instanceId = UUID.randomUUID().toString();
        Instance knownInstance = TestInstanceFactory.withStarterVersion(
                instanceId, "1.3.0", mockWebServer.url("/actuator").toString());

        // language=json
        String response = """
            {
              "healthStatus" : "DOWN",
              "memoryDetails" : {
                "heap" : 500
              },
              "transactions" : [ ]
            }
            """;

        stubs.respondOn("/actuator/axelix-ping", response);
        stubs.stubDiscovery(stubs.k8sInstance(serviceId, instanceId));
        Mockito.when(instanceRegistry.get(InstanceId.of(instanceId))).thenReturn(Optional.of(knownInstance));

        Set<DiscoveredInstanceProfile> profiles = subject.discover();

        assertThat(profiles).singleElement().isInstanceOfSatisfying(DiscoveredInstanceProfile.Refresh.class, it -> {
            assertThat(it.instance()).isEqualTo(knownInstance.copy(Instance.InstanceStatus.DOWN, new MemoryUsage(500)));
            assertThat(it.masterPing().getTransactions()).isEmpty();
        });
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
        RecordedRequest pingRequest = mockWebServer.takeRequest();
        assertThat(pingRequest.getPath()).isEqualTo("/actuator/axelix-ping");
        String token = pingRequest.getHeader("Authorization").substring("Bearer ".length());
        assertThat(new HmacVerificationStrategy()
                        .verifyAndParse(token, JwtDefaults.DEFAULT_SIGNING_KEY)
                        .getPayload())
                .containsOnlyKeys("sub", "iat", "exp");
    }

    @Test
    void shouldSkipKnownInstanceWhenMasterPingFails() throws InterruptedException {
        String serviceId = UUID.randomUUID().toString();
        String instanceId = UUID.randomUUID().toString();

        mockWebServer.enqueue(new MockResponse().setResponseCode(503));

        stubs.stubDiscovery(stubs.k8sInstance(serviceId, instanceId));
        Mockito.when(instanceRegistry.get(InstanceId.of(instanceId)))
                .thenReturn(Optional.of(TestInstanceFactory.withStarterVersion(
                        instanceId, "1.3.0", mockWebServer.url("/actuator").toString())));

        Set<DiscoveredInstanceProfile> profiles = subject.discover();

        assertThat(profiles).isEmpty();
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
        RecordedRequest pingRequest = mockWebServer.takeRequest();
        assertThat(pingRequest.getPath()).isEqualTo("/actuator/axelix-ping");
        String token = pingRequest.getHeader("Authorization").substring("Bearer ".length());
        assertThat(new HmacVerificationStrategy()
                        .verifyAndParse(token, JwtDefaults.DEFAULT_SIGNING_KEY)
                        .getPayload())
                .containsOnlyKeys("sub", "iat", "exp");
    }

    @Test
    void shouldSkipKnownInstanceOutsideCompatibilityWindow() {
        String serviceId = UUID.randomUUID().toString();
        String instanceId = UUID.randomUUID().toString();
        // one minor ahead of the master, so outside its compatibility window
        SemanticVersion masterVersion = SemanticVersion.parse(axelixVersionDiscoverer.getVersion());
        String starterVersion = masterVersion.major() + "." + (masterVersion.minor() + 1) + ".0";

        stubs.stubDiscovery(stubs.k8sInstance(serviceId, instanceId));
        Mockito.when(instanceRegistry.get(InstanceId.of(instanceId)))
                .thenReturn(Optional.of(TestInstanceFactory.withStarterVersion(
                        instanceId,
                        starterVersion,
                        mockWebServer.url("/actuator").toString())));

        Set<DiscoveredInstanceProfile> profiles = subject.discover();

        assertThat(profiles).isEmpty();
        assertThat(mockWebServer.getRequestCount()).isZero();
    }

    @Test
    void shouldFetchFullMetadataOfKnownInstanceWithOldStarter() throws InterruptedException {
        String serviceId = UUID.randomUUID().toString();
        String instanceId = UUID.randomUUID().toString();

        stubs.enqueueResponses(
                TestMetadataFactory.toJson(TestMetadataFactory.create("org.springframework.samples", "petclinic")));
        stubs.stubDiscovery(stubs.k8sInstance(serviceId, instanceId));
        Mockito.when(instanceRegistry.get(InstanceId.of(instanceId)))
                .thenReturn(Optional.of(TestInstanceFactory.withStarterVersion(
                        instanceId, "1.2.0", mockWebServer.url("/actuator").toString())));

        Set<DiscoveredInstanceProfile> profiles = subject.discover();

        assertThat(profiles).singleElement().isInstanceOf(DiscoveredInstanceProfile.Registration.class);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
        assertThat(mockWebServer.takeRequest().getPath()).isEqualTo("/actuator/axelix-metadata");
    }
}
