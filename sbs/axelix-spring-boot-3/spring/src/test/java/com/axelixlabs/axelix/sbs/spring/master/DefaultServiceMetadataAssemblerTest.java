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
package com.axelixlabs.axelix.sbs.spring.master;

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

import com.axelixlabs.axelix.common.api.registration.ServiceMetadata;
import com.axelixlabs.axelix.common.domain.AxelixVersionDiscoverer;

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
        VMFeaturesProvider vmFeaturesProvider() {
            return new OptionsParsingVMFeaturesProvider();
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
