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
package com.axelixlabs.axelix.sbs.spring.core.master;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.service.DefaultAuthoritiesManager;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtDecoderService;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtEncoderService;
import com.axelixlabs.axelix.common.auth.service.JwtDecoderService;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.config.AuthProperties;
import com.axelixlabs.axelix.sbs.spring.core.config.HeartBeatConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.contract.heartbeatv2.HeartBeatMetadataV2;
import com.axelixlabs.axelix.sbs.spring.core.contract.initialregistrationrequest.InitialSelfRegistrationRequest;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.HealthStatus;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.Insights;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.TransactionAggregatedProfile;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.InsightsInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.sbom.ClasspathDependencySbom;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTaskExecutionHistory;
import com.axelixlabs.axelix.sbs.spring.core.testutils.NoOpLogger;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestInsightsInfoProvider;

import static com.axelixlabs.axelix.sbs.spring.core.master.AxelixInfoPropertiesLoader.AXELIX_INFO_LOCATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Integration test for {@link HeartBeatService}
 *
 * @since 06.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Vyacheslav Yanin
 */
@SpringBootTest(classes = HeartBeatServiceTest.TestApplication.class)
@TestPropertySource(
        properties = {
            "axelix.sbs.discovery.master-url=http://localhost/service/register",
            "axelix.sbs.discovery.instance-name=testApp",
            "axelix.sbs.discovery.instance-actuator-url=http://localhost:8089/actuator",
            "axelix.sbs.discovery.heartbeat-interval=PT1S"
        })
@Import({HeartBeatServiceTest.HeartBeatServiceTestConfiguration.class})
class HeartBeatServiceTest {

    private MockWebServer mockWebServer;

    @SpringBootApplication
    static class TestApplication {}

    @Autowired
    private HeartBeatConfigurationProperties properties;

    @Autowired
    private HeartBeatMetadataAssembler metadataAssembler;

    @Autowired
    private JwtEncoderService jwtEncoderService;

    private HeartBeatService heartBeatService;

    @Autowired
    private JwtDecoderService jwtDecoderService;

    @Autowired
    private ScheduledTaskExecutionHistory scheduledTaskExecutionHistory;

    @TestConfiguration
    static class HeartBeatServiceTestConfiguration {

        @Bean
        @ConfigurationProperties(prefix = HeartBeatConfigurationProperties.CONFIG_PROPS_PREFIX)
        public HeartBeatConfigurationProperties heartBeatConfigurationProperties() {
            return new HeartBeatConfigurationProperties();
        }

        @Bean
        @ConfigurationProperties(prefix = AuthProperties.CONFIG_PROPS_PREFIX)
        public AuthProperties authProperties() {
            return new AuthProperties();
        }

        @Bean
        public JwtEncoderService jwtEncoderService(AuthProperties authProperties) {
            return new DefaultJwtEncoderService(
                    authProperties.getJwt().getAlgorithm(),
                    authProperties.getJwt().getSigningKey(),
                    Duration.ofHours(1));
        }

        @Bean
        public JwtDecoderService jwtDecoderService(AuthProperties authProperties) {
            return new DefaultJwtDecoderService(
                    new DefaultAuthoritiesManager(null),
                    authProperties.getJwt().getAlgorithm(),
                    authProperties.getJwt().getSigningKey());
        }

        @Bean
        public ScheduledTaskExecutionHistory scheduledTaskExecutionHistory() {
            return new ScheduledTaskExecutionHistory();
        }

        @Bean
        public HeartBeatMetadataAssembler heartBeatMetadataAssembler(
                BasicRegistrationMetadataAssembler basicRegistrationMetadataAssembler,
                HealthDetectionFunction healthDetectionFunction,
                InsightsInfoProvider insightsInfoProvider,
                HeartBeatConfigurationProperties heartBeatConfigurationProperties) {

            return new DefaultHeartBeatMetadataAssembler(
                    basicRegistrationMetadataAssembler,
                    healthDetectionFunction,
                    insightsInfoProvider,
                    heartBeatConfigurationProperties);
        }

        @Bean
        HealthDetectionFunction healthDetectionFunction() {
            return () -> HealthStatus.UP;
        }

        @Bean
        public AxelixVersionDiscoverer axelixVersionDiscoverer() {
            return () -> "1.1.3";
        }

        @Bean
        public LibraryInformationProvider libraryInformationProvider() {
            return new TestLibraryInformationProvider();
        }

        @Bean
        InsightsInfoProvider insightsInfoProvider(ScheduledTaskExecutionHistory scheduledTaskExecutionHistory) {
            TestInsightsInfoProvider delegate = new TestInsightsInfoProvider();

            return new InsightsInfoProvider() {
                @Override
                public Insights getInsight() {
                    return delegate.getInsight();
                }

                @Override
                public List<TransactionAggregatedProfile> getTransactions() {
                    return delegate.getTransactions();
                }

                @Override
                public List<ScheduledTaskExecution> getScheduledTaskExecutions() {
                    return scheduledTaskExecutionHistory.getExecutions();
                }
            };
        }

        @Bean
        public AxelixInfoProperties axelixInfoProperties(@Value(AXELIX_INFO_LOCATION) Resource axelixInfoResource)
                throws IOException {

            try (InputStream inputStream = axelixInfoResource.getInputStream()) {
                return AxelixInfoPropertiesLoader.load(inputStream);
            }
        }

        @Bean
        public DefaultBasicRegistrationMetadataAssembler serviceMetadataAssembler(
                HealthDetectionFunction healthDetectionFunction,
                AxelixVersionDiscoverer axelixVersionDiscoverer,
                LibraryInformationProvider libraryInformationProvider,
                InsightsInfoProvider insightsInfoProvider,
                AxelixInfoProperties axelixInfoProperties) {

            return new DefaultBasicRegistrationMetadataAssembler(
                    healthDetectionFunction,
                    axelixVersionDiscoverer,
                    libraryInformationProvider,
                    insightsInfoProvider,
                    axelixInfoProperties,
                    () -> Set.of(),
                    new ClasspathDependencySbom(),
                    new NoOpLogger());
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        heartBeatService.close();
        mockWebServer.shutdown();
    }

    private void startHeartBeating(String masterUrlPath) {
        startHeartBeating(masterUrlPath, HeartBeatServiceTest::serialize);
    }

    private void startHeartBeating(String masterUrlPath, JsonSerializationFunction serializationFunction) {
        properties.setMasterUrl(
                "http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort() + masterUrlPath);
        heartBeatService = new HeartBeatService(
                new NoOpLogger(), serializationFunction, properties, metadataAssembler, jwtEncoderService);
        heartBeatService.scheduleHeartBeat();
    }

    @Test
    void shouldHeartbeatOnApplicationEvent() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        startHeartBeating("/service/register");

        RecordedRequest request = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/service/register");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json");

        String body = request.getBody().readUtf8();
        assertThat(body).contains("testApp");
        assertThat(body).contains("http://localhost:8089/actuator");

        String authHeader = request.getHeader(HeartBeatService.AUTHORIZATION_HEADER);
        assertThat(authHeader).startsWith(AuthenticationSchemes.BEARER.prefix());

        String token =
                authHeader.substring(AuthenticationSchemes.BEARER.prefix().length());

        assertThatCode(() -> jwtDecoderService.decodeTokenToUser(token)).doesNotThrowAnyException();
    }

    @Test
    void shouldDeliverTheWholeHistoryOnEveryHeartbeat() throws Exception {
        scheduledTaskExecutionHistory.record(scheduledTaskExecution("com.example.Delivered#run()"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        startHeartBeating("/service/register");

        RecordedRequest registration = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(registration.getPath()).isEqualTo("/service/register");

        RecordedRequest firstHeartbeat = mockWebServer.takeRequest(3, TimeUnit.SECONDS);
        assertThat(firstHeartbeat.getPath()).isEqualTo("/service/heartbeat/v2");
        assertThat(firstHeartbeat.getBody().readUtf8()).contains("com.example.Delivered#run()");

        RecordedRequest secondHeartbeat = mockWebServer.takeRequest(3, TimeUnit.SECONDS);
        assertThat(secondHeartbeat.getPath()).isEqualTo("/service/heartbeat/v2");
        assertThat(secondHeartbeat.getBody().readUtf8()).contains("com.example.Delivered#run()");
    }

    @Test
    void shouldKeepHeartbeatingWhenHeartbeatThrows() throws Exception {
        scheduledTaskExecutionHistory.record(scheduledTaskExecution("com.example.Retried#run()"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        AtomicBoolean failed = new AtomicBoolean();
        startHeartBeating("/service/register", payload -> {
            if (payload instanceof HeartBeatMetadataV2 && failed.compareAndSet(false, true)) {
                throw new IllegalStateException("boom");
            }
            return serialize(payload);
        });

        assertThat(mockWebServer.takeRequest(2, TimeUnit.SECONDS).getPath()).isEqualTo("/service/register");

        RecordedRequest heartbeat = mockWebServer.takeRequest(4, TimeUnit.SECONDS);
        assertThat(failed).isTrue();
        assertThat(heartbeat.getPath()).isEqualTo("/service/heartbeat/v2");
        assertThat(heartbeat.getBody().readUtf8()).contains("com.example.Retried#run()");
    }

    private static ScheduledTaskExecution scheduledTaskExecution(String taskId) {
        return new ScheduledTaskExecution()
                .taskId(taskId)
                .startedAt("2026-01-01T00:00:00Z")
                .durationMillis(100L)
                .success(true);
    }

    @Test
    void shouldHandleRejectedRegistration() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));
        startHeartBeating("/service/register");

        RecordedRequest request = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void shouldHandleServerError() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        startHeartBeating("/service/register");

        RecordedRequest request = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void shouldHandleTimeout() throws Exception {
        mockWebServer.enqueue(new MockResponse().setHeadersDelay(5, TimeUnit.SECONDS));
        startHeartBeating("/service/register");

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void shouldHandleUnauthorized() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        startHeartBeating("/service/register");

        RecordedRequest firstRequest = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(firstRequest).isNotNull();
        assertThat(firstRequest.getMethod()).isEqualTo("POST");

        RecordedRequest secondRequest = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(secondRequest).isNotNull();
        assertThat(secondRequest.getMethod()).isEqualTo("POST");
    }

    @Test
    void shouldSendHeartbeatV2OnceRegistered() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        startHeartBeating("/service/register");

        RecordedRequest registration = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(registration).isNotNull();
        assertThat(registration.getPath()).isEqualTo("/service/register");

        RecordedRequest heartbeat = mockWebServer.takeRequest(3, TimeUnit.SECONDS);
        assertThat(heartbeat).isNotNull();
        assertThat(heartbeat.getMethod()).isEqualTo("POST");
        assertThat(heartbeat.getPath()).isEqualTo("/service/heartbeat/v2");
        assertThat(heartbeat.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(heartbeat.getHeader(HeartBeatService.AUTHORIZATION_HEADER))
                .startsWith(AuthenticationSchemes.BEARER.prefix());
        assertThat(heartbeat.getBody().readUtf8()).contains("instanceId").doesNotContain("testApp");
    }

    @Test
    void shouldRegisterAgainWhenMasterDoesNotKnowTheInstance() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        mockWebServer.enqueue(new MockResponse().setResponseCode(409));
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        startHeartBeating("/service/register");

        assertThat(mockWebServer.takeRequest(2, TimeUnit.SECONDS).getPath()).isEqualTo("/service/register");
        assertThat(mockWebServer.takeRequest(3, TimeUnit.SECONDS).getPath()).isEqualTo("/service/heartbeat/v2");

        RecordedRequest registration = mockWebServer.takeRequest(3, TimeUnit.SECONDS);
        assertThat(registration).isNotNull();
        assertThat(registration.getPath()).isEqualTo("/service/register");
        assertThat(registration.getBody().readUtf8()).contains("testApp");

        assertThat(mockWebServer.takeRequest(3, TimeUnit.SECONDS).getPath()).isEqualTo("/service/heartbeat/v2");
    }

    @Test
    void shouldKeepTryingToRegisterUntilRegistrationSucceeds() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        startHeartBeating("/service/register");

        assertThat(mockWebServer.takeRequest(2, TimeUnit.SECONDS).getPath()).isEqualTo("/service/register");
        assertThat(mockWebServer.takeRequest(3, TimeUnit.SECONDS).getPath()).isEqualTo("/service/register");
    }

    private static String serialize(Object payload) {
        if (payload instanceof HeartBeatMetadataV2) {
            HeartBeatMetadataV2 heartBeat = (HeartBeatMetadataV2) payload;
            String taskIds = heartBeat.getScheduledTaskExecutions() == null
                    ? ""
                    : heartBeat.getScheduledTaskExecutions().stream()
                            .map(execution -> "\"" + execution.getTaskId() + "\"")
                            .collect(Collectors.joining(","));
            return "{\"instanceId\":\"" + heartBeat.getInstanceId() + "\",\"scheduledTaskExecutions\":[" + taskIds
                    + "]}";
        }

        InitialSelfRegistrationRequest metadata = (InitialSelfRegistrationRequest) payload;
        return "{\"instanceName\":\""
                + metadata.getInstanceName()
                + "\",\"instanceActuatorUrl\":\""
                + metadata.getInstanceActuatorUrl()
                + "\"}";
    }
}
