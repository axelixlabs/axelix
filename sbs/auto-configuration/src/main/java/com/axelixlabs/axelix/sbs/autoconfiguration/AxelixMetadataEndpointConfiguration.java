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
package com.axelixlabs.axelix.sbs.autoconfiguration;

import java.util.List;

import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.common.domain.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.common.domain.PropertiesAxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.master.AxelixMetadataEndpoint;
import com.axelixlabs.axelix.sbs.spring.master.CommitIdPluginGitInformationProvider;
import com.axelixlabs.axelix.sbs.spring.master.CommitIdPluginShortBuildInfoProvider;
import com.axelixlabs.axelix.sbs.spring.master.DefaultServiceMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.master.GitInformationProvider;
import com.axelixlabs.axelix.sbs.spring.master.LibraryDiscoverer;
import com.axelixlabs.axelix.sbs.spring.master.OptionsParsingVMFeaturesProvider;
import com.axelixlabs.axelix.sbs.spring.master.ServiceMetadataAssembler;
import com.axelixlabs.axelix.sbs.spring.master.ShortBuildInfoProvider;
import com.axelixlabs.axelix.sbs.spring.master.VMFeaturesProvider;

/**
 * Auto-configuration for the {@link AxelixMetadataEndpoint}.
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration(
        after = {
            HealthEndpointAutoConfiguration.class,
            CommitIdPluginGitInformationProvider.class,
            CommitIdPluginShortBuildInfoProvider.class,
            LibraryDiscovererAutoConfiguration.class
        })
public class AxelixMetadataEndpointConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AxelixVersionDiscoverer axelixVersionDiscoverer() {
        return new PropertiesAxelixVersionDiscoverer("META-INF/axelix.properties");
    }

    @Bean
    @ConditionalOnMissingBean
    public VMFeaturesProvider vmFeaturesProvider() {
        return new OptionsParsingVMFeaturesProvider();
    }

    @Bean
    ServiceMetadataAssembler serviceMetadataAssembler(
            HealthEndpoint healthEndpoint,
            LibraryDiscoverer libraryDiscoverer,
            AxelixVersionDiscoverer axelixVersionDiscoverer,
            List<GitInformationProvider> gitInformationProviders,
            List<ShortBuildInfoProvider> shortBuildInfoProviders,
            VMFeaturesProvider vmFeaturesProvider) {
        return new DefaultServiceMetadataAssembler(
                healthEndpoint,
                libraryDiscoverer,
                axelixVersionDiscoverer,
                gitInformationProviders,
                shortBuildInfoProviders,
                vmFeaturesProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxelixMetadataEndpoint axelixMetadataEndpoint(ServiceMetadataAssembler serviceMetadataAssembler) {
        return new AxelixMetadataEndpoint(serviceMetadataAssembler);
    }
}
