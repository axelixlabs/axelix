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

import java.lang.management.ManagementFactory;

import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixMetadataEndpoint}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
    AxelixMetadataEndpoint.class,
    AxelixMetadataEndpointTest.CurrentConfig.class,
    DefaultServiceMetadataAssembler.class,
    CommitIdPluginGitInformationProvider.class,
    CommitIdPluginShortBuildInfoProvider.class
})
class AxelixMetadataEndpointTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @TestConfiguration
    static class CurrentConfig {

        @Bean
        VMFeaturesProvider vmFeaturesProvider() {
            return new OptionsParsingVMFeaturesProvider(
                    ManagementFactory.getRuntimeMXBean().getInputArguments());
        }

        @Bean
        HealthDetectionFunction healthDetectionFunction() {
            return () -> BasicDiscoveryMetadata.HealthStatus.UP;
        }

        @Bean
        AxelixVersionDiscoverer axelixVersionDiscoverer() {
            return () -> "1.1.3";
        }

        @Bean
        public LibraryInformationProvider libraryInformationProvider() {
            return new DefaultLibraryInformationProvider();
        }
    }

    @Test
    void shouldReceiveServiceMetadata() {
        // when.
        ResponseEntity<String> result = testRestTemplate.getForEntity("/actuator/axelix-metadata", String.class);

        // then.
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        JsonAssertions.assertThatJson(result.getBody())
                // we do not want to know exactly the java version on which the test is going to run
                .whenIgnoringPaths("softwareVersions", "vmFeatures")
                .isEqualTo("{\n" + "  \"version\": \"1.1.3\",\n"
                        + "  \"serviceVersion\" : \"3.5.0-SNAPSHOT\",\n"
                        + "  \"commitShortSha\" : \"a8b0929\",\n"
                        + "  \"jdkVendor\" : \"#{json-unit.ignore}\",\n"
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
                        + "  \"vmFeatures\": []\n"
                        + "}");
    }
}
