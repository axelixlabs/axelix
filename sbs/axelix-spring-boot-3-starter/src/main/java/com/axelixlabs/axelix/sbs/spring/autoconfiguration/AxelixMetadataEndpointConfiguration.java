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
import java.util.List;

import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.common.domain.version.PropertiesAxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.gclog.GcLogService;
import com.axelixlabs.axelix.sbs.spring.core.master.AxelixMetadataEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.master.CachingAxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.master.CommitIdPluginGitInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.CommitIdPluginShortBuildInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.DefaultLibraryInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.DefaultOpenSessionInViewStateProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.DefaultServiceMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.master.GitInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.LibraryInformationProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.OpenSessionInViewStateProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.ServiceMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.core.master.ShortBuildInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.DefaultInsightsInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.InsightsInfoProvider;
import com.axelixlabs.axelix.sbs.spring.core.master.insights.VmOptionsAccessor;

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
            CommitIdPluginGitInformationProvider.class,
            CommitIdPluginShortBuildInfoProvider.class
        })
public class AxelixMetadataEndpointConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AxelixVersionDiscoverer axelixVersionDiscoverer() {
        return new CachingAxelixVersionDiscoverer(new PropertiesAxelixVersionDiscoverer("META-INF/axelix.properties"));
    }

    @Bean
    @ConditionalOnMissingBean
    public LibraryInformationProvider libraryInformationProvider() {
        return new DefaultLibraryInformationProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenSessionInViewStateProvider openSessionInViewStateProvider(
            ConfigurableApplicationContext applicationContext) {
        return new DefaultOpenSessionInViewStateProvider(applicationContext.getBeanFactory());
    }

    @Bean
    @ConditionalOnMissingBean
    public VmOptionsAccessor vmOptionsAccessor() {
        return new VmOptionsAccessor(ManagementFactory.getRuntimeMXBean().getInputArguments());
    }

    @Bean
    @ConditionalOnMissingBean
    public InsightsInfoProvider insightsInfoProvider(
            OpenSessionInViewStateProvider openSessionInViewStateProvider,
            GcLogService gcLogService,
            VmOptionsAccessor vmOptionsAccessor) {
        return new DefaultInsightsInfoProvider(openSessionInViewStateProvider, gcLogService, vmOptionsAccessor);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceMetadataAssembler serviceMetadataAssembler(
            HealthEndpoint healthEndpoint,
            AxelixVersionDiscoverer axelixVersionDiscoverer,
            List<GitInformationProvider> gitInformationProviders,
            List<ShortBuildInfoProvider> shortBuildInfoProviders,
            LibraryInformationProvider libraryInformationProvider,
            InsightsInfoProvider insightsInfoProvider) {
        return new DefaultServiceMetadataAssembler(
                () -> getCurrentHealth(healthEndpoint),
                axelixVersionDiscoverer,
                gitInformationProviders,
                shortBuildInfoProviders,
                libraryInformationProvider,
                insightsInfoProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxelixMetadataEndpoint axelixMetadataEndpoint(ServiceMetadataAssembler serviceMetadataAssembler) {
        return new AxelixMetadataEndpoint(serviceMetadataAssembler);
    }

    private BasicDiscoveryMetadata.HealthStatus getCurrentHealth(HealthEndpoint healthEndpoint) {
        Status status = healthEndpoint.health().getStatus();

        if (status == Status.UP) {
            return BasicDiscoveryMetadata.HealthStatus.UP;
        }

        if (status == Status.DOWN) {
            return BasicDiscoveryMetadata.HealthStatus.DOWN;
        }

        // defaulting to unknown in case of UNKNOWN, OUT_OF_SERVICE and custom statuses
        return BasicDiscoveryMetadata.HealthStatus.UNKNOWN;
    }
}
