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
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.SpringVersion;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.api.registration.BasicRegistrationMetadata;
import com.axelixlabs.axelix.common.api.registration.GitInfo;
import com.axelixlabs.axelix.common.api.registration.HeartBeatMetadata;
import com.axelixlabs.axelix.common.api.registration.ShortBuildInfo;
import com.axelixlabs.axelix.common.api.registration.insights.HotSpotInsights;
import com.axelixlabs.axelix.common.api.registration.insights.Insights;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.config.HeartBeatConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.InsightsInfoProvider;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link DefaultHeartBeatMetadataAssembler}.
 *
 * @since 06.02.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Ilya Naumov
 */
@SpringBootTest(classes = DefaultHeartBeatMetadataAssemblerTest.TestApplication.class)
@TestPropertySource(
        properties = {
            "axelix.sbs.discovery.instance-name=testApp",
            "axelix.sbs.discovery.master-url=http://localhost:8080/",
            "axelix.sbs.discovery.instance-actuator-url=http://localhost:8089/actuator"
        })
class DefaultHeartBeatMetadataAssemblerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({CurrentConfig.class})
    static class TestApplication {}

    @Autowired
    private HeartBeatMetadataAssembler subject;

    @Autowired
    private BasicRegistrationMetadataAssembler service;

    @Autowired
    private HeartBeatConfigurationProperties properties;

    @TestConfiguration
    static class CurrentConfig {

        @Bean
        @ConfigurationProperties(prefix = HeartBeatConfigurationProperties.CONFIG_PROPS_PREFIX)
        public HeartBeatConfigurationProperties heartBeatConfigurationProperties() {
            return new HeartBeatConfigurationProperties();
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
        AxelixVersionDiscoverer axelixVersionDiscoverer() {
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

    @Test
    void shouldAssembleTheHeartBeatMetadataAboutGivenService() {
        // when.
        HeartBeatMetadata metadata = subject.assemble();
        BasicRegistrationMetadata basicMetadata = metadata.getBasicDiscoveryMetadata();

        // then.
        assertThat(metadata.getInstanceId()).isNotBlank();
        assertThat(metadata.getInstanceName()).startsWith("testApp-").hasSize("testApp-".length() + 8);
        assertThat(metadata.getInstanceActuatorUrl()).isEqualTo("http://localhost:8089/actuator");
        assertThat(metadata.getDeploymentAt()).isNotBlank();
        assertThat(Instant.parse(metadata.getDeploymentAt()).isBefore(Instant.now()))
                .isTrue();
        assertThat(basicMetadata.getVersion()).isEqualTo("1.1.3");
        assertThat(basicMetadata.getServiceVersion()).isEqualTo("1.1.3");
        assertThat(basicMetadata.getCommitShortSha()).isEqualTo("8f4b9f7");
        assertThat(basicMetadata.getHealthStatus()).isEqualTo(BasicRegistrationMetadata.HealthStatus.UP);
        assertThat(basicMetadata.getGcInUse()).isNotNull();
        assertThat(basicMetadata.getSoftwareVersions().getSpringBoot()).isEqualTo(SpringBootVersion.getVersion());
        assertThat(basicMetadata.getSoftwareVersions().getSpringFramework()).isEqualTo(SpringVersion.getVersion());
    }

    @Test // GH-1292
    void shouldReturnStableInstanceNameOnRepeatedCalls() {
        // when.
        HeartBeatMetadata firstMetadata = subject.assemble();
        HeartBeatMetadata secondMetadata = subject.assemble();

        // then.
        assertThat(firstMetadata.getInstanceName()).isEqualTo(secondMetadata.getInstanceName());
    }

    @Test // GH-1292
    void shouldReturnDifferentInstanceNameForDifferentInstances() {
        // given.
        DefaultHeartBeatMetadataAssembler assembler1 = new DefaultHeartBeatMetadataAssembler(service, properties);
        DefaultHeartBeatMetadataAssembler assembler2 = new DefaultHeartBeatMetadataAssembler(service, properties);

        // when.
        HeartBeatMetadata metadata1 = assembler1.assemble();
        HeartBeatMetadata metadata2 = assembler2.assemble();

        // then.
        assertThat(metadata1.getInstanceName()).startsWith("testApp-");
        assertThat(metadata2.getInstanceName()).startsWith("testApp-");
        assertThat(metadata1.getInstanceName()).isNotEqualTo(metadata2.getInstanceName());
    }
}
