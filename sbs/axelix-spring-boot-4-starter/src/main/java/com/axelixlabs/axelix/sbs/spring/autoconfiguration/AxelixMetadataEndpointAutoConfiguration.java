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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import java.lang.management.ManagementFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointAutoConfiguration;
import org.springframework.boot.health.contributor.Status;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.common.api.registration.BasicRegistrationMetadata;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.common.domain.version.PropertiesAxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.gclog.GcLogService;
import com.axelixlabs.axelix.sbs.spring.core.master.AxelixInfoProperties;
import com.axelixlabs.axelix.sbs.spring.core.master.AxelixMetadataEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.master.BasicRegistrationMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.master.CachingAxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.master.DefaultBasicRegistrationMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.master.DefaultOpenSessionInViewStateProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.LibraryInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.OpenSessionInViewStateProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.DefaultInsightsInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.InsightsInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.JpaEntitiesProfileProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.NoOpJpaEntitiesProfileProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.VmOptionsAccessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAttributesRegistry;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;

/**
 * Auto-configuration for the {@link AxelixMetadataEndpoint}.
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration(
        after = {
            GarbageCollectionAutoConfiguration.class,
            HealthEndpointAutoConfiguration.class,
            AxelixInfoPropertiesAutoConfiguration.class,
            LibraryInformationProviderAutoConfiguration.class,
            TransactionMonitoringAutoConfiguration.class,
        })
public class AxelixMetadataEndpointAutoConfiguration {

    @Bean
    public AxelixVersionDiscoverer axelixVersionDiscoverer() {
        return new CachingAxelixVersionDiscoverer(new PropertiesAxelixVersionDiscoverer("META-INF/axelix.properties"));
    }

    @Bean
    public OpenSessionInViewStateProvider openSessionInViewStateProvider(
            ConfigurableApplicationContext applicationContext) {
        return new DefaultOpenSessionInViewStateProvider(applicationContext.getBeanFactory());
    }

    @Bean
    public VmOptionsAccessor vmOptionsAccessor() {
        return new VmOptionsAccessor(ManagementFactory.getRuntimeMXBean().getInputArguments());
    }

    @Bean
    public InsightsInfoProvider insightsInfoProvider(
            OpenSessionInViewStateProvider openSessionInViewStateProvider,
            GcLogService gcLogService,
            VmOptionsAccessor vmOptionsAccessor,
            TransactionStatsCollector transactionStatsCollector,
            TransactionAttributesRegistry transactionAttributesRegistry,
            ObjectProvider<JpaEntitiesProfileProvider> entitiesMapProvider) {

        return new DefaultInsightsInfoProvider(
                openSessionInViewStateProvider,
                gcLogService,
                vmOptionsAccessor,
                transactionStatsCollector,
                transactionAttributesRegistry,
                entitiesMapProvider.getIfAvailable(NoOpJpaEntitiesProfileProvider::new));
    }

    @Bean
    public BasicRegistrationMetadataAssembler serviceMetadataAssembler(
            HealthEndpoint healthEndpoint,
            AxelixVersionDiscoverer axelixVersionDiscoverer,
            LibraryInformationProvider libraryInformationProvider,
            InsightsInfoProvider insightsInfoProvider,
            AxelixInfoProperties axelixInfoProperties) {

        return new DefaultBasicRegistrationMetadataAssembler(
                () -> getCurrentHealth(healthEndpoint),
                axelixVersionDiscoverer,
                libraryInformationProvider,
                insightsInfoProvider,
                axelixInfoProperties);
    }

    @Bean
    public AxelixMetadataEndpoint axelixMetadataEndpoint(
            BasicRegistrationMetadataAssembler basicRegistrationMetadataAssembler) {
        return new AxelixMetadataEndpoint(basicRegistrationMetadataAssembler);
    }

    private BasicRegistrationMetadata.HealthStatus getCurrentHealth(HealthEndpoint healthEndpoint) {
        Status status = healthEndpoint.health().getStatus();

        if (status == Status.UP) {
            return BasicRegistrationMetadata.HealthStatus.UP;
        }

        if (status == Status.DOWN) {
            return BasicRegistrationMetadata.HealthStatus.DOWN;
        }

        // defaulting to unknown in case of UNKNOWN, OUT_OF_SERVICE and custom statuses
        return BasicRegistrationMetadata.HealthStatus.UNKNOWN;
    }
}
