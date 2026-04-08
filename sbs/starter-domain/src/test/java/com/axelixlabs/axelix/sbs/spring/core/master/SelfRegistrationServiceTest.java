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
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.config.SelfRegistrationConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.log.SLF4JLogger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link SelfRegistrationService}
 *
 * @since 06.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SpringBootTest(classes = SelfRegistrationServiceTest.TestApplication.class)
@TestPropertySource(
        properties = {
            "axelix.sbs.discovery.instance-name=testApp",
            "axelix.sbs.discovery.instance-url=http://localhost:8089/"
        })
@Import({
    DefaultServiceMetadataAssembler.class,
    OptionsParsingVMFeaturesProvider.class,
    SelfRegistrationServiceTest.SelfRegistrationServiceTestConfiguration.class
})
class SelfRegistrationServiceTest {

    private static MockWebServer mockWebServer;

    @SpringBootApplication
    static class TestApplication {}

    @Autowired
    private SelfRegistrationService selfRegistrationService;

    @Autowired
    private JwtDecoderService jwtDecoderService;

    @TestConfiguration
    static class SelfRegistrationServiceTestConfiguration {

        @Bean
        @ConfigurationProperties(prefix = "axelix.sbs.discovery")
        public SelfRegistrationConfigurationProperties selfRegistrationConfigurationProperties() {
            return new SelfRegistrationConfigurationProperties();
        }

        @Bean
        public SelfRegistrationService selfRegistrationService(
                SelfRegistrationConfigurationProperties properties,
                ObjectMapper objectMapper,
                SelfRegistrationMetadataAssembler metadataAssembler,
                JwtEncoderService jwtEncoderService,
                AuthProperties authProperties) {
                SelfRegistrationMetadataAssembler metadataAssembler) {
            return new SelfRegistrationService(
                    new SLF4JLogger(LoggerFactory.getLogger(SelfRegistrationService.class)),
                    objectMapper::writeValueAsString,
                    properties,
                    metadataAssembler,
                    jwtEncoderService,
                    authProperties.getJwt().getDuration());
                    new NoOpLogger(), SelfRegistrationServiceTest::serialize, properties, metadataAssembler);
        }

        @Bean
        public SelfRegistrationMetadataAssembler selfRegistrationMetadataAssembler(
                ServiceMetadataAssembler serviceMetadataAssembler,
                SelfRegistrationConfigurationProperties selfRegistrationConfigurationProperties) {
            return new DefaultSelfRegistrationMetadataAssembler(
                    serviceMetadataAssembler, selfRegistrationConfigurationProperties, "/actuator");
        }

        @Bean
        HealthDetectionFunction healthDetectionFunction() {
            return () -> BasicDiscoveryMetadata.HealthStatus.UP;
        }

        @Bean
        public VMFeaturesProvider vmFeaturesProvider() {
            return new OptionsParsingVMFeaturesProvider(
                    ManagementFactory.getRuntimeMXBean().getInputArguments());
        }

        @Bean
        public AxelixVersionDiscoverer axelixVersionDiscoverer() {
            return () -> "1.1.3";
        }

        @Bean
        GitInformationProvider gitInformationProvider() {
            return () -> Optional.of(
                    new GitInfo("8f4b9f7", "main", "2026-02-06T10:15:30Z", new GitInfo.CommitAuthor("test", "test")));
        }

        @Bean
        ShortBuildInfoProvider shortBuildInfoProvider() {
            return () -> Optional.of(new ShortBuildInfo("2026-02-06T10:15:30Z", "1.1.3"));
        }

        @Bean
        LibraryDiscoverer libraryDiscoverer() {
            return (artifactId, groupId) -> {
                if ("spring-boot".equals(artifactId) && "org.springframework.boot".equals(groupId)) {
                    return Optional.of("2.7.18");
                }
                if ("spring-core".equals(artifactId) && "org.springframework".equals(groupId)) {
                    return Optional.of("5.3.31");
                }
                return Optional.empty();
            };
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

    @Test
    void shouldSendSelfRegistrationRequestSuccessfully() throws Exception {
        // when.
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));
        selfRegistrationService.scheduleSelfRegistration();
        RecordedRequest request = mockWebServer.takeRequest(3, TimeUnit.SECONDS);
        selfRegistrationService.close();

        // then.
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json");
        String body = request.getBody().readUtf8();
        assertThat(body).contains("testApp");
        assertThat(body).contains("http://localhost:8089/actuator");

        String authHeader = request.getHeader("Authorization");
        assertThat(authHeader).startsWith("Bearer ");

        String token = authHeader.substring("Bearer ".length());

        assertThatCode(() -> jwtDecoderService.decodeTokenToUser(token)).doesNotThrowAnyException();
    }

    private static String serialize(Object payload) {
        SelfRegistrationMetadata metadata = (SelfRegistrationMetadata) payload;
        return "{\"instanceName\":\""
                + metadata.getInstanceName()
                + "\",\"instanceActuatorUrl\":\""
                + metadata.getInstanceActuatorUrl()
                + "\"}";
    }

        private static final class NoOpLogger implements Logger {

            @Override
            public void trace(String message, Object... args) {}

            @Override
            public void info(String message, Object... args) {}

            @Override
            public void debug(String message, Object... args) {}
        }

    @Test
    void shouldHandleServerError() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        selfRegistrationService.scheduleSelfRegistration();

        RecordedRequest request = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void shouldHandleTimeout() throws Exception {
        mockWebServer.enqueue(new MockResponse().setHeadersDelay(5, TimeUnit.SECONDS));

        selfRegistrationService.scheduleSelfRegistration();

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
    }
}
