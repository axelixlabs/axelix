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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.Main;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.HealthStatus;
import com.axelixlabs.axelix.sbs.spring.core.master.AbstractMasterSharedContextTest.SharedConfig;
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
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({AxelixMetadataEndpoint.class, JwtAuthTestConfiguration.class, SharedConfig.class})
abstract class AbstractMasterSharedContextTest {

    @MockitoBean
    private HealthEndpoint healthEndpoint;

    @TestConfiguration
    static class SharedConfig {

        @Bean
        HealthDetectionFunction healthDetectionFunction() {
            return () -> HealthStatus.UP;
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
        public AxelixInfoProperties axelixInfoProperties(@Value(AXELIX_INFO_LOCATION) Resource axelixInfoResource)
                throws IOException {

            try (InputStream inputStream = axelixInfoResource.getInputStream()) {
                return AxelixInfoPropertiesLoader.load(inputStream);
            }
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
