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
package com.axelixlabs.axelix.sbs.spring.core.details;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;

import com.axelixlabs.axelix.common.api.registration.BasicRegistrationMetadata;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.details.AbstractDetailsSharedContextTest.TestApplication;
import com.axelixlabs.axelix.sbs.spring.core.master.AxelixInfoProperties;
import com.axelixlabs.axelix.sbs.spring.core.master.AxelixInfoPropertiesLoader;
import com.axelixlabs.axelix.sbs.spring.core.master.BasicRegistrationMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.master.DefaultBasicRegistrationMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.master.HealthDetectionFunction;
import com.axelixlabs.axelix.sbs.spring.core.master.LibraryInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.InsightsInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestInsightsInfoProvider;

import static com.axelixlabs.axelix.sbs.spring.core.master.AxelixInfoPropertiesLoader.AXELIX_INFO_LOCATION;

/**
 * Shared base for the {@code master} integration tests. By defining a single, unioned context here and having every
 * subclass inherit it unchanged, the Spring TestContext framework resolves the same context cache key for all of them,
 * so they share one cached {@link org.springframework.context.ApplicationContext}.
 *
 *
 * @author Mikhail Polivakha
 * @author Artemiy Degtyarev
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractDetailsSharedContextTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(SharedConfig.class)
    static class TestApplication {}

    @MockBean
    private HealthEndpoint healthEndpoint;

    @TestConfiguration
    static class SharedConfig {

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
            return new LibraryInformationProvider() {
                @Override
                public String getKotlinVersion() {
                    return "1.6.21";
                }

                @Override
                public String getJavaVersion() {
                    return System.getProperty("java.version");
                }

                @Override
                public String getJdkVendorName() {
                    return System.getProperty("java.vendor");
                }

                @Override
                public String getSpringBootVersion() {
                    return "2.7.18";
                }

                @Override
                public String getSpringVersion() {
                    return "5.3.31";
                }

                @Override
                public String getSpringCloudVersion() {
                    return "2022.0.4";
                }
            };
        }

        @Bean
        public InsightsInfoProvider insightsInfoProvider() {
            return new TestInsightsInfoProvider();
        }

        @Bean
        public AxelixInfoProperties axelixInfoProperties(@Value(AXELIX_INFO_LOCATION) Resource axelixInfoResource)
                throws IOException {

            try (InputStream inputStream = axelixInfoResource.getInputStream()) {
                return AxelixInfoPropertiesLoader.load(inputStream);
            }
        }

        @Bean
        public ServiceDetailsAssembler serviceDetailsAssembler(
                AxelixInfoProperties axelixInfoProperties, LibraryInformationProvider libraryInformationProvider) {
            return new DefaultServiceDetailsAssembler(axelixInfoProperties, libraryInformationProvider);
        }

        @Bean
        public BasicRegistrationMetadataAssembler serviceMetadataAssembler(
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
    }
}
