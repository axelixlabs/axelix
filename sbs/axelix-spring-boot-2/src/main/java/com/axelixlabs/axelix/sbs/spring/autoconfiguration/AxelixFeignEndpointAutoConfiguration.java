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

import feign.Feign;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.axelixlabs.axelix.sbs.spring.core.integrations.AxelixFeignEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.integrations.FeignClientIntegrationDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.integrations.NoOpDiscoveryClient;

/**
 * Auto-configuration for discovering HTTP integrations based on Spring Cloud OpenFeign.
 * <p>
 * Registers a {@link FeignClientIntegrationDiscoverer} if Feign is present on the classpath.
 * </p>
 *
 * @author Sergey Cherkasov
 */
@AutoConfiguration
@ConditionalOnAvailableEndpoint(endpoint = AxelixFeignEndpoint.class)
@ConditionalOnClass({Feign.class, FeignClient.class})
public class AxelixFeignEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FeignClientIntegrationDiscoverer feignClientIntegrationDiscoverer(
            ApplicationContext applicationContext,
            ObjectProvider<DiscoveryClient> discoveryClientProvider,
            Environment environment) {
        DiscoveryClient discoveryClient = discoveryClientProvider.getIfAvailable(NoOpDiscoveryClient::new);
        return new FeignClientIntegrationDiscoverer(applicationContext, discoveryClient, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxelixFeignEndpoint axelixFeignEndpoint(FeignClientIntegrationDiscoverer discoverer) {
        return new AxelixFeignEndpoint(discoverer);
    }
}
