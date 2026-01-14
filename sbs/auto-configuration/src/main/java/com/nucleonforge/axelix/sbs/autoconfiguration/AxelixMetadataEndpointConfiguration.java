/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.autoconfiguration;

import java.util.List;

import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axelix.common.domain.AxelixVersionDiscoverer;
import com.nucleonforge.axelix.common.domain.PropertiesAxelixVersionDiscoverer;
import com.nucleonforge.axelix.sbs.spring.master.AxelixMetadataEndpoint;
import com.nucleonforge.axelix.sbs.spring.master.CommitIdPluginGitInformationProvider;
import com.nucleonforge.axelix.sbs.spring.master.CommitIdPluginShortBuildInfoProvider;
import com.nucleonforge.axelix.sbs.spring.master.DefaultServiceMetadataAssembler;
import com.nucleonforge.axelix.sbs.spring.master.GitInformationProvider;
import com.nucleonforge.axelix.sbs.spring.master.LibraryDiscoverer;
import com.nucleonforge.axelix.sbs.spring.master.ServiceMetadataAssembler;
import com.nucleonforge.axelix.sbs.spring.master.ShortBuildInfoProvider;

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
    ServiceMetadataAssembler serviceMetadataAssembler(
            HealthEndpoint healthEndpoint,
            LibraryDiscoverer libraryDiscoverer,
            AxelixVersionDiscoverer axelixVersionDiscoverer,
            List<GitInformationProvider> gitInformationProviders,
            List<ShortBuildInfoProvider> shortBuildInfoProviders) {
        return new DefaultServiceMetadataAssembler(
                healthEndpoint,
                libraryDiscoverer,
                axelixVersionDiscoverer,
                gitInformationProviders,
                shortBuildInfoProviders);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxelixMetadataEndpoint axelixMetadataEndpoint(ServiceMetadataAssembler serviceMetadataAssembler) {
        return new AxelixMetadataEndpoint(serviceMetadataAssembler);
    }
}
