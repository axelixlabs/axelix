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
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.domain.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.config.SelfRegistrationConfigurationProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link SelfRegistrationService}
 *
 * @since 06.02.2026
 * @author Nikita Kirillov
 */
@SpringBootTest
@Import({
    CommitIdPluginGitInformationProvider.class,
    CommitIdPluginShortBuildInfoProvider.class,
    DefaultServiceMetadataAssembler.class,
    OptionsParsingVMFeaturesProvider.class,
    SelfRegistrationServiceTest.SelfRegistrationServiceTestConfiguration.class
})
@TestPropertySource(
        properties = {
            "axelix.sbs.discovery.instance-name=testApp",
            "axelix.sbs.discovery.instance-url=http://localhost:8089/"
        })
@EnableConfigurationProperties({SelfRegistrationConfigurationProperties.class, WebEndpointProperties.class})
class SelfRegistrationServiceTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private SelfRegistrationService selfRegistrationService;

    @TestConfiguration
    static class SelfRegistrationServiceTestConfiguration {

        @Bean
        public SelfRegistrationService selfRegistrationService(
                SelfRegistrationConfigurationProperties properties,
                SelfRegistrationMetadataAssembler metadataAssembler) {
            return new SelfRegistrationService(properties, metadataAssembler);
        }

        @Bean
        public SelfRegistrationMetadataAssembler selfRegistrationMetadataAssembler(
                ServiceMetadataAssembler serviceMetadataAssembler,
                SelfRegistrationConfigurationProperties selfRegistrationConfigurationProperties,
                WebEndpointProperties webEndpointProperties) {
            return new DefaultSelfRegistrationMetadataAssembler(
                    serviceMetadataAssembler,
                    selfRegistrationConfigurationProperties,
                    webEndpointProperties.getBasePath());
        }

        @Bean
        public CycloneDXSBOMLibraryDiscoverer cycloneDXSBOMLibraryDiscoverer() {
            return new CycloneDXSBOMLibraryDiscoverer(new ClassPathResource("other/application.cdx.json"));
        }

        @Bean
        public VMFeaturesProvider vmFeaturesProvider() {
            return new OptionsParsingVMFeaturesProvider();
        }

        @Bean
        public HealthEndpoint healthEndpoint() {
            HealthEndpoint mock = Mockito.mock(HealthEndpoint.class);
            Mockito.when(mock.health()).thenReturn(Health.up().build());
            return mock;
        }

        @Bean
        public AxelixVersionDiscoverer axelixVersionDiscoverer() {
            return () -> "1.1.3";
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
        mockWebServer.shutdown();
        System.clearProperty("axelix.sbs.discovery.master-url");
    }

    @Test
    void shouldRegisterOnApplicationEvent() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        selfRegistrationService.onApplicationEvent(null);

        RecordedRequest request = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/service/register");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json");

        String body = request.getBody().readUtf8();
        assertThat(body).contains("testApp");
        assertThat(body).contains("http://localhost:8089/actuator");
    }

    @Test
    void shouldHandleRejectedRegistration() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));

        selfRegistrationService.onApplicationEvent(null);

        RecordedRequest request = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void shouldHandleServerError() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        selfRegistrationService.onApplicationEvent(null);

        RecordedRequest request = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    void shouldHandleTimeout() throws Exception {
        mockWebServer.enqueue(new MockResponse().setHeadersDelay(5, TimeUnit.SECONDS));

        selfRegistrationService.onApplicationEvent(null);

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("POST");
    }
}
