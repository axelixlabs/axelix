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
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.api.registration.GitInfo;
import com.axelixlabs.axelix.common.api.registration.SelfRegistrationMetadata;
import com.axelixlabs.axelix.common.api.registration.ShortBuildInfo;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.config.SelfRegistrationConfigurationProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link DefaultSelfRegistrationMetadataAssembler}.
 *
 * @since 06.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SpringBootTest(classes = DefaultSelfRegistrationMetadataAssemblerTest.TestApplication.class)
@TestPropertySource(
        properties = {
            "axelix.sbs.discovery.instance-name=testApp",
            "axelix.sbs.discovery.master-url=http://localhost:8080/",
            "axelix.sbs.discovery.instance-url=http://localhost:8089/"
        })
class DefaultSelfRegistrationMetadataAssemblerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({DefaultServiceMetadataAssembler.class, CurrentConfig.class})
    static class TestApplication {}

    @Autowired
    private SelfRegistrationMetadataAssembler subject;

    @TestConfiguration
    static class CurrentConfig {

        @Bean
        @ConfigurationProperties(prefix = "axelix.sbs.discovery")
        public SelfRegistrationConfigurationProperties selfRegistrationConfigurationProperties() {
            return new SelfRegistrationConfigurationProperties();
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
        VMFeaturesProvider vmFeaturesProvider() {
            return new OptionsParsingVMFeaturesProvider(
                    ManagementFactory.getRuntimeMXBean().getInputArguments());
        }

        @Bean
        AxelixVersionDiscoverer axelixVersionDiscoverer() {
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

    @Test
    void shouldAssembleTheSelfRegistrationMetadataAboutGivenService() {
        // when.
        SelfRegistrationMetadata metadata = subject.assemble();
        BasicDiscoveryMetadata basicMetadata = metadata.getBasicDiscoveryMetadata();

        // then.
        assertThat(metadata.getInstanceId()).isNotBlank();
        assertThat(metadata.getInstanceName()).isEqualTo("testApp");
        assertThat(metadata.getInstanceActuatorUrl()).isEqualTo("http://localhost:8089/actuator");
        assertThat(metadata.getDeploymentAt()).isNotBlank();
        assertThat(Instant.parse(metadata.getDeploymentAt()).isBefore(Instant.now()))
                .isTrue();
        assertThat(basicMetadata.getVersion()).isEqualTo("1.1.3");
        assertThat(basicMetadata.getServiceVersion()).isEqualTo("1.1.3");
        assertThat(basicMetadata.getCommitShortSha()).isEqualTo("8f4b9f7");
        assertThat(basicMetadata.getHealthStatus()).isEqualTo(BasicDiscoveryMetadata.HealthStatus.UP);
        assertThat(basicMetadata.getSoftwareVersions().getSpringBoot()).isEqualTo("2.7.18");
        assertThat(basicMetadata.getSoftwareVersions().getSpringFramework()).isEqualTo("5.3.31");
    }
}
