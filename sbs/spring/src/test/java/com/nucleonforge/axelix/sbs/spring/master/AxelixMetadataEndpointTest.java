/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.master;

import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axelix.common.domain.AxelixVersionDiscoverer;

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

    @MockBean
    private HealthEndpoint healthEndpoint;

    @TestConfiguration
    static class CurrentConfig {

        @Bean
        CycloneDXSBOMLibraryDiscoverer cycloneDXSBOMLibraryDiscoverer() {
            return new CycloneDXSBOMLibraryDiscoverer(new ClassPathResource("other/application.cdx.json"));
        }

        @Bean
        AxelixVersionDiscoverer axelixVersionDiscoverer() {
            return () -> "1.1.3";
        }
    }

    @Test
    void shouldReceiveServiceMetadata() {

        Mockito.when(healthEndpoint.health()).thenReturn(Health.up().build());

        // when.
        ResponseEntity<String> result = testRestTemplate.getForEntity("/actuator/axelix-metadata", String.class);

        // then.
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        JsonAssertions.assertThatJson(result.getBody())
                // we do not want to know exactly the java version on which the test is going to run
                .whenIgnoringPaths("versions")
                .isEqualTo(
                        // language=json
                        """
            {
              "version": "1.1.3",
              "serviceVersion" : "3.5.0-SNAPSHOT",
              "commitShortSha" : "a8b0929",
              "jdkVendor" : "#{json-unit.ignore}",
              "versions" : {
                "springBoot" : "3.5.0",
                "java" : "25",
                "springFramework" : "6.1.2",
                "kotlin" : null
              },
              "healthStatus" : "UP",
              "memory" : {
                "heap" : "#{json-unit.ignore}"
              }
            }
            """);
    }
}
