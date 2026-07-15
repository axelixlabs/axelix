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
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.api.registration.BasicRegistrationMetadata;
import com.axelixlabs.axelix.common.api.registration.GitInfo;
import com.axelixlabs.axelix.common.api.registration.HeartBeatMetadata;
import com.axelixlabs.axelix.common.api.registration.ShortBuildInfo;
import com.axelixlabs.axelix.common.api.registration.insights.HotSpotInsights;
import com.axelixlabs.axelix.common.api.registration.insights.Insights;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtDecoderService;
import com.axelixlabs.axelix.common.auth.service.DefaultJwtEncoderService;
import com.axelixlabs.axelix.common.auth.service.JwtDecoderService;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.config.AuthProperties;
import com.axelixlabs.axelix.sbs.spring.core.config.HeartBeatConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.InsightsInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.testutils.NoOpLogger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Integration test for {@link HeartBeatService}
 *
 * @since 06.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SpringBootTest(classes = HeartBeatServiceTest.TestApplication.class)
@TestPropertySource(
        properties = {
            "axelix.sbs.discovery.instance-name=testApp",
            "axelix.sbs.discovery.instance-actuator-url=http://localhost:8089/actuator",
            "axelix.sbs.discovery.heartbeat-interval=PT1S"
        })
@Import({HeartBeatServiceTest.HeartBeatServiceTestConfiguration.class})
class HeartBeatServiceTest {

    private static MockWebServer mockWebServer;

    @SpringBootApplication
    static class TestApplication {}

    @Autowired
    private HeartBeatService heartBeatService;

    @Autowired
    private JwtDecoderService jwtDecoderService;

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
                    authProperties.getJwt().getAlgorithm(),
                    authProperties.getJwt().getSigningKey());
        }

        @Bean
        public HeartBeatService heartBeatService(
                HeartBeatConfigurationProperties properties,
                HeartBeatMetadataAssembler metadataAssembler,
                JwtEncoderService jwtEncoderService) {
            return new HeartBeatService(
                    new NoOpLogger(),
                    HeartBeatServiceTest::serialize,
                    properties,
                    metadataAssembler,
                    jwtEncoderService);
        }

        @Bean
        public HeartBeatMetadataAssembler heartBeatMetadataAssembler(
                BasicRegistrationMetadataAssembler basicRegistrationMetadataAssembler,
                HeartBeatConfigurationProperties heartBeatConfigurationProperties) {

            return new DefaultHeartBeatMetadataAssembler(
                    basicRegistrationMetadataAssembler, heartBeatConfigurationProperties);
        }

        @Bean
        HealthDetectionFunction healthDetectionFunction() {
            return () -> BasicRegistrationMetadata.HealthStatus.UP;
        }

        @Bean
        public AxelixVersionDiscoverer axelixVersionDiscoverer() {
            return () -> "1.1.3";
        }

        @Bean
        GitInformationProvider gitInformationProvider() {
            return () ->
                    new GitInfo("8f4b9f7", "main", "2026-02-06T10:15:30Z", new GitInfo.CommitAuthor("test", "test"));
        }

        @Bean
        ShortBuildInfoProvider shortBuildInfoProvider() {
            return () -> new ShortBuildInfo("2026-02-06T10:15:30Z", "1.1.3");
        }

        @Bean
        public LibraryInformationProvider libraryInformationProvider() {
            return new TestLibraryInformationProvider();
        }

        @Bean
        InsightsInfoProvider insightsInfoProvider() {
            return () -> new Insights(
                    new HotSpotInsights(List.of(), List.of(), List.of()),
                    List.of(),
                    new PersistenceInsights(List.of()));
        }

        @Bean
        public DefaultBasicRegistrationMetadataAssembler serviceMetadataAssembler(
                HealthDetectionFunction healthDetectionFunction,
                AxelixVersionDiscoverer axelixVersionDiscoverer,
                GitInformationProvider gitInformationProvider,
                ShortBuildInfoProvider shortBuildInfoProvider,
                LibraryInformationProvider libraryInformationProvider,
                InsightsInfoProvider insightsInfoProvider) {

            return new DefaultBasicRegistrationMetadataAssembler(
                    healthDetectionFunction,
                    axelixVersionDiscoverer,
                    gitInformationProvider,
                    shortBuildInfoProvider,
                    libraryInformationProvider,
                    insightsInfoProvider,
                    "com.axelixlabs",
                    "axelix-sbs");
        }
    }

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String masterUrl =
                "http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort() + "/service/register";
        System.setProperty("axelix.sbs.discovery.master-url", masterUrl);
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
        System.clearProperty("axelix.sbs.discovery.master-url");
    }

    @BeforeEach
    void beforeEach() {
        heartBeatService.scheduleHeartBeat();
    }

    @Test
    void shouldHeartbeatOnApplicationEvent() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));

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
    void shouldHandleRejectedRegistration() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));

        RecordedRequest request = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void shouldHandleServerError() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        RecordedRequest request = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void shouldHandleTimeout() throws Exception {
        mockWebServer.enqueue(new MockResponse().setHeadersDelay(5, TimeUnit.SECONDS));

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void shouldHandleUnauthorized() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        RecordedRequest firstRequest = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(firstRequest).isNotNull();
        assertThat(firstRequest.getMethod()).isEqualTo("POST");

        RecordedRequest secondRequest = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(secondRequest).isNotNull();
        assertThat(secondRequest.getMethod()).isEqualTo("POST");
    }

    private static String serialize(Object payload) {
        HeartBeatMetadata metadata = (HeartBeatMetadata) payload;
        return "{\"instanceName\":\""
                + metadata.getInstanceName()
                + "\",\"instanceActuatorUrl\":\""
                + metadata.getInstanceActuatorUrl()
                + "\"}";
    }
}
