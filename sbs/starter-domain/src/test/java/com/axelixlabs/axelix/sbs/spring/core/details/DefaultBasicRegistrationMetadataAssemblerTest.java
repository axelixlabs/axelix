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

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;

import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.HealthStatus;
import com.axelixlabs.axelix.sbs.spring.core.master.AxelixInfoProperties;
import com.axelixlabs.axelix.sbs.spring.core.master.DefaultBasicRegistrationMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.master.HealthDetectionFunction;
import com.axelixlabs.axelix.sbs.spring.core.master.LibraryInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.DefaultInsightsInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.NoOpJpaEntitiesProfileProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.VmOptionsAccessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.DefaultTransactionStatsCollector;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAttributesRegistry;

import static com.axelixlabs.axelix.sbs.spring.core.utils.TestInsightsInfoProvider.TEST_INSIGHTS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link DefaultBasicRegistrationMetadataAssembler}.
 *
 * @author Mikhail Polivakha
 */
class DefaultBasicRegistrationMetadataAssemblerTest extends AbstractDetailsSharedContextTest {

    @Autowired
    private DefaultBasicRegistrationMetadataAssembler subject;

    @Autowired
    private HealthDetectionFunction healthDetectionFunction;

    @Autowired
    private AxelixVersionDiscoverer axelixVersionDiscoverer;

    @Autowired
    private LibraryInformationProvider libraryInformationProvider;

    @Autowired
    private AxelixInfoProperties axelixInfoProperties;

    @Test
    void shouldAssembleTheMetadataAboutGivenService() {
        // when.
        BasicRegistrationMetadata serviceMetadata = subject.assemble();

        // then.
        assertThat(serviceMetadata.getCommitShortSha()).isEqualTo("a8b0929");
        assertThat(serviceMetadata.getServiceVersion()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(serviceMetadata.getGroupId()).isEqualTo("com.axelixlabs");
        assertThat(serviceMetadata.getArtifactId()).isEqualTo("axelix-sbs");
        assertThat(serviceMetadata.getSoftwareVersions().getJava()).isEqualTo(System.getProperty("java.version"));
        assertThat(serviceMetadata.getVersion()).isEqualTo("1.1.3");
        assertThat(serviceMetadata.getSoftwareVersions().getSpringBoot()).isEqualTo(SpringBootVersion.getVersion());
        assertThat(serviceMetadata.getHealthStatus()).isEqualTo(HealthStatus.UP);
        assertThat(serviceMetadata.getMemoryDetails()).isNotNull();
        assertThat(serviceMetadata.getInsights()).isEqualTo(TEST_INSIGHTS);
    }

    @Test // the assembling must not fail when the scheduled task execution history is absent.
    void shouldReportNoScheduledTaskExecutions_whenHistoryIsAbsent() {
        // given.
        DefaultBasicRegistrationMetadataAssembler assembler = new DefaultBasicRegistrationMetadataAssembler(
                healthDetectionFunction,
                axelixVersionDiscoverer,
                libraryInformationProvider,
                insightsInfoProviderWithoutHistory(),
                axelixInfoProperties);

        // when.
        BasicRegistrationMetadata serviceMetadata = assembler.assemble();

        // then.
        assertThat(serviceMetadata.getInsights().getScheduledTaskExecutions()).isEmpty();
    }

    private static DefaultInsightsInfoProvider insightsInfoProviderWithoutHistory() {
        return new DefaultInsightsInfoProvider(
                () -> false,
                null,
                new VmOptionsAccessor(List.of()),
                new DefaultTransactionStatsCollector(),
                new TransactionAttributesRegistry(),
                new NoOpJpaEntitiesProfileProvider(),
                null);
    }
}
