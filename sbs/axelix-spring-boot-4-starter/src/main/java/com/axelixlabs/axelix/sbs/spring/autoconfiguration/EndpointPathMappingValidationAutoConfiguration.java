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

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.ConfigurableEnvironment;

import com.axelixlabs.axelix.sbs.spring.autoconfiguration.annotations.AxelixAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.env.AxelixEndpointDiscovery;

/**
 * Rejects path mappings for Axelix endpoints during application startup.
 *
 * @author Andrey Kotov
 */
@AxelixAutoConfiguration
@EnableConfigurationProperties(WebEndpointProperties.class)
public class EndpointPathMappingValidationAutoConfiguration {

    @Bean
    public AxelixEndpointDiscovery axelixEndpointDiscovery() {
        return new AxelixEndpointDiscovery();
    }

    @Bean
    @Lazy(false)
    public InitializingBean axelixEndpointPathMappingValidator(
            WebEndpointProperties properties,
            ConfigurableEnvironment environment,
            AxelixEndpointDiscovery endpointDiscovery) {
        return () -> {
            Set<EndpointId> axelixEndpointIds = new HashSet<>(endpointDiscovery.discoverEndpointIds(environment));
            for (String endpointId : properties.getPathMapping().keySet()) {
                if (axelixEndpointIds.contains(EndpointId.fromPropertyValue(endpointId))) {
                    throw new IllegalStateException("Property 'management.endpoints.web.path-mapping." + endpointId
                            + "' is not supported. Remove this property; Axelix endpoint paths must not be remapped.");
                }
            }
        };
    }
}
