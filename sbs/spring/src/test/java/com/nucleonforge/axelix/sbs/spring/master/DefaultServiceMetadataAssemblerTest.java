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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;

import com.nucleonforge.axelix.common.api.registration.ServiceMetadata;
import com.nucleonforge.axelix.common.domain.AxelixVersionDiscoverer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link DefaultServiceMetadataAssembler}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest
@Import({
    CommitIdPluginGitInformationProvider.class,
    CommitIdPluginShortBuildInfoProvider.class,
    DefaultServiceMetadataAssembler.class,
    DefaultServiceMetadataAssemblerTest.CurrentConfig.class
})
class DefaultServiceMetadataAssemblerTest {

    @Autowired
    private DefaultServiceMetadataAssembler subject;

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
    void shouldAssembleTheMetadataAboutGivenService() {
        // then.
        Mockito.when(healthEndpoint.health()).thenReturn(Health.up().build());

        // when.
        ServiceMetadata serviceMetadata = subject.assemble();

        // then.
        assertThat(serviceMetadata.commitShortSha()).isEqualTo("a8b0929");
        assertThat(serviceMetadata.serviceVersion()).isEqualTo("3.5.0-SNAPSHOT");
        assertThat(serviceMetadata.versions().java()).isEqualTo(System.getProperty("java.version"));
        assertThat(serviceMetadata.version()).isEqualTo("1.1.3");
        assertThat(serviceMetadata.versions().springBoot()).isEqualTo("3.5.0");
        assertThat(serviceMetadata.healthStatus()).isEqualTo(ServiceMetadata.HealthStatus.UP);
        assertThat(serviceMetadata.memoryDetails()).isNotNull();
    }
}
