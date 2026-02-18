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

import java.util.List;

import feign.Feign;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.core.integrations.http.AxelixFeignEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.integrations.http.FeignClientIntegrationDiscoverer;

/**
 * Auto-configuration for discovering HTTP integrations based on Spring Cloud OpenFeign.
 * <p>
 * Registers a {@link FeignClientIntegrationDiscoverer} if Feign is present on the classpath.
 * </p>
 *
 * @author Sergey Cherkasov
 */
@AutoConfiguration
@ConditionalOnClass({Feign.class, FeignClient.class})
public class AxelixFeignEndpointAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FeignClientIntegrationDiscoverer feignCClientIntegrationDiscoverer(
            ApplicationContext applicationContext, ObjectProvider<DiscoveryClient> discoveryClientProvider) {
        DiscoveryClient discoveryClient = discoveryClientProvider.getIfAvailable(NoOpDiscoveryClient::new);
        return new FeignClientIntegrationDiscoverer(applicationContext, discoveryClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxelixFeignEndpoint axelixFeignEndpoint(FeignClientIntegrationDiscoverer extractor) {
        return new AxelixFeignEndpoint(extractor);
    }

    private static final class NoOpDiscoveryClient implements DiscoveryClient {
        @Override
        public String description() {
            return "No discovery client configured";
        }

        @Override
        public List<ServiceInstance> getInstances(String serviceId) {
            return List.of();
        }

        @Override
        public List<String> getServices() {
            return List.of();
        }
    }
}
