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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.info.InfoContributorAutoConfiguration;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.details.AxelixDetailsEndpoint;
import com.axelixlabs.axelix.sbs.spring.details.DefaultServiceDetailsAssembler;
import com.axelixlabs.axelix.sbs.spring.details.ServiceDetailsAssembler;
import com.axelixlabs.axelix.sbs.spring.master.GitInformationProvider;
import com.axelixlabs.axelix.sbs.spring.master.LibraryDiscoverer;

/**
 * Auto-configuration for the {@link AxelixDetailsEndpoint}.
 *
 * @since 30.10.2025
 * @author Nikita Kirillov, Sergey Cherkasov
 */
@AutoConfiguration(
        after = {
            InfoContributorAutoConfiguration.class,
            LibraryDiscovererAutoConfiguration.class,
            GitInformationProviderAutoConfiguration.class
        })
@ConditionalOnAvailableEndpoint(endpoint = InfoEndpoint.class)
public class AxelixDetailsEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AxelixDetailsEndpoint axelixDetailsEndpoint(ServiceDetailsAssembler serviceDetailsAssembler) {
        return new AxelixDetailsEndpoint(serviceDetailsAssembler);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceDetailsAssembler serviceInfoAssembler(
            GitInformationProvider gitInformationProvider,
            ObjectProvider<BuildProperties> providerBuildProperties,
            LibraryDiscoverer libraryDiscoverer) {
        return new DefaultServiceDetailsAssembler(gitInformationProvider, providerBuildProperties, libraryDiscoverer);
    }
}
