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

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.api.registration.SelfRegistrationMetadata;
import com.axelixlabs.axelix.common.domain.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.config.SelfRegistrationConfigurationProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link DefaultSelfRegistrationMetadataAssembler}.
 *
 * @since 06.02.2026
 * @author Nikita Kirillov
 */
@SpringBootTest
@Import({
    CommitIdPluginGitInformationProvider.class,
    CommitIdPluginShortBuildInfoProvider.class,
    DefaultServiceMetadataAssembler.class,
    WebEndpointAutoConfiguration.class,
    DefaultSelfRegistrationMetadataAssemblerTest.CurrentConfig.class
})
@TestPropertySource(
        properties = {
            "axelix.sbs.discovery.instance-name=testApp",
            "axelix.sbs.discovery.master-url=http://localhost:8080/",
            "axelix.sbs.discovery.instance-url=http://localhost:8089/"
        })
class DefaultSelfRegistrationMetadataAssemblerTest {

    @Autowired
    private SelfRegistrationMetadataAssembler subject;

    @MockBean
    private HealthEndpoint healthEndpoint;

    @TestConfiguration
    @EnableConfigurationProperties({SelfRegistrationConfigurationProperties.class, WebEndpointProperties.class})
    static class CurrentConfig {

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
    void shouldAssembleTheSelfRegistrationMetadataAboutGivenService() {
        // then.
        Mockito.when(healthEndpoint.health()).thenReturn(Health.up().build());

        // when.
        SelfRegistrationMetadata metadata = subject.assemble();

        // then.
        assertThat(metadata.getInstanceId()).isNotBlank();
        assertThat(metadata.getInstanceName()).isEqualTo("testApp");
        assertThat(metadata.getInstanceActuatorUrl()).isEqualTo("http://localhost:8089/actuator");
        assertThat(metadata.getDeploymentAt()).isNotBlank();
        assertThat(Instant.parse(metadata.getDeploymentAt()).isBefore(Instant.now()))
                .isTrue();
    }
}
