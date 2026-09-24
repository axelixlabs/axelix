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

import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.registration.BasicRegistrationMetadata;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.InsightsInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestInsightsInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;

import static com.axelixlabs.axelix.sbs.spring.core.master.AxelixInfoPropertiesLoader.AXELIX_INFO_LOCATION;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixMetadataEndpoint}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({AxelixMetadataEndpoint.class, AxelixMetadataEndpointTest.CurrentConfig.class, JwtAuthTestConfiguration.class})
class AxelixMetadataEndpointTest {

    @Autowired
    private TestRestTemplateBuilder testRestTemplate;

    @TestConfiguration
    static class CurrentConfig {

        @Bean
        HealthDetectionFunction healthDetectionFunction() {
            return () -> BasicRegistrationMetadata.HealthStatus.UP;
        }

        @Bean
        AxelixVersionDiscoverer axelixVersionDiscoverer() {
            return () -> "1.1.3";
        }

        @Bean
        public LibraryInformationProvider libraryInformationProvider() {
            return new DefaultLibraryInformationProvider();
        }

        @Bean
        public InsightsInfoProvider insightsInfoProvider() {
            return new TestInsightsInfoProvider();
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
                    axelixInfoProperties);
        }

        @Bean
        public AxelixInfoProperties axelixInfoProperties(@Value(AXELIX_INFO_LOCATION) Resource axelixInfoResource)
                throws IOException {

            try (InputStream inputStream = axelixInfoResource.getInputStream()) {
                return AxelixInfoPropertiesLoader.load(inputStream);
            }
        }
    }

    @Test
    void shouldReceiveServiceMetadata() {
        // when.
        ResponseEntity<String> result =
                testRestTemplate.asViewer().getForEntity("/actuator/axelix-metadata", String.class);

        // then.
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        JsonAssertions.assertThatJson(result.getBody())
                // we do not want to know exactly the java version on which the test is going to run
                .whenIgnoringPaths("softwareVersions")
                .isEqualTo("{\n" + "  \"version\": \"1.1.3\",\n"
                        + "  \"serviceVersion\" : \"1.0.0-SNAPSHOT\",\n"
                        + "  \"groupId\" : \"com.axelixlabs\",\n"
                        + "  \"artifactId\" : \"axelix-sbs\",\n"
                        + "  \"commitShortSha\" : \"a8b0929\",\n"
                        + "  \"jdkVendor\" : \"#{json-unit.ignore}\",\n"
                        + "  \"gcInUse\" : \"#{json-unit.ignore}\",\n"
                        + "  \"softwareVersions\" : {\n"
                        + "    \"springBoot\" : \"3.5.0\",\n"
                        + "    \"java\" : \"25\",\n"
                        + "    \"springFramework\" : \"6.1.2\",\n"
                        + "    \"kotlin\" : null\n"
                        + "  },\n"
                        + "  \"healthStatus\" : \"UP\",\n"
                        + "  \"memoryDetails\" : {\n"
                        + "    \"heap\" : \"#{json-unit.ignore}\"\n"
                        + "  },\n"
                        + "  \"insights\" : {\n"
                        + "    \"hotSpot\" : {\n"
                        + "      \"projectLeyden\" : [\n"
                        + "        { \"featureId\" : \"AppCDS\", \"enabled\" : true },\n"
                        + "        { \"featureId\" : \"AotCache\", \"enabled\" : false }\n"
                        + "      ],\n"
                        + "      \"gc\" : [\n"
                        + "        { \"featureId\" : \"GCLoggingEnabled\", \"enabled\" : true },\n"
                        + "        { \"featureId\" : \"GCLogFileSpecified\", \"enabled\" : false }\n"
                        + "      ],\n"
                        + "      \"projectLilliputh\" : [\n"
                        + "        { \"featureId\" : \"CompactObjectHeaders\", \"enabled\" : true }\n"
                        + "      ]\n"
                        + "    },\n"
                        + "    \"springFramework\" : [\n"
                        + "      { \"featureId\" : \"OSIV\", \"enabled\" : false }\n"
                        + "    ],\n"
                        + "    \"persistenceInsights\" : {\n"
                        + "      \"transactions\" : [ ],\n"
                        + "      \"entitiesMap\" : null\n"
                        + "    },\n"
                        + "    \"scheduledTaskExecutions\" : [ ]\n"
                        + "  }\n"
                        + "}");
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-metadata")
    void negativeAuthTests() {}
}
