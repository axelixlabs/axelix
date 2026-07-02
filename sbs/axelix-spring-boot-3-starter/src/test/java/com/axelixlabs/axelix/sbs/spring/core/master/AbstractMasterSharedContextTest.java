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

import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.Main;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.InsightsInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestInsightsInfoProvider;

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
@Import({
    CommitIdPluginGitInformationProvider.class,
    CommitIdPluginShortBuildInfoProvider.class,
    ProjectInfoAutoConfiguration.class,
    AxelixMetadataEndpoint.class,
    JwtAuthTestConfiguration.class,
    AbstractMasterSharedContextTest.SharedConfig.class
})
abstract class AbstractMasterSharedContextTest {

    @MockBean
    private HealthEndpoint healthEndpoint;

    @TestConfiguration
    static class SharedConfig {

        @Bean
        HealthDetectionFunction healthDetectionFunction() {
            return () -> BasicDiscoveryMetadata.HealthStatus.UP;
        }

        @Bean
        AxelixVersionDiscoverer axelixVersionDiscoverer() {
            return () -> "1.1.3";
        }

        @Bean
        public InsightsInfoProvider insightsInfoProvider() {
            return new TestInsightsInfoProvider();
        }

        // TODO: fallback to @Import once https://github.com/axelixlabs/axelix/issues/1305 is done
        @Bean
        public DefaultServiceMetadataAssembler serviceMetadataAssembler(
                HealthDetectionFunction healthDetectionFunction,
                AxelixVersionDiscoverer axelixVersionDiscoverer,
                GitInformationProvider gitInformationProvider,
                ShortBuildInfoProvider shortBuildInfoProvider,
                LibraryInformationProvider libraryInformationProvider,
                InsightsInfoProvider insightsInfoProvider) {
            return new DefaultServiceMetadataAssembler(
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
}
