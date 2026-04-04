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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

import com.axelixlabs.axelix.sbs.spring.core.env.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.properties.ContextReloadingPropertyMutator;
import com.axelixlabs.axelix.sbs.spring.core.properties.DefaultPropertyNameDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.properties.PropertyManagementEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.properties.PropertyMutator;
import com.axelixlabs.axelix.sbs.spring.core.properties.PropertyNameDiscoverer;

/**
 * Auto-configuration for property management operations via Spring Boot Actuator.
 *
 * @since 10.07.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration
public class PropertyManagementAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PropertyMutator propertyMutator(ConfigurableEnvironment environment) {
        return new ContextReloadingPropertyMutator(environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public PropertyNameNormalizer propertyNameNormalizer() {
        return new DefaultPropertyNameNormalizer();
    }

    @Bean
    @ConditionalOnMissingBean
    public PropertyNameDiscoverer propertyNameDiscoverer(
            ConfigurableEnvironment environment, PropertyNameNormalizer propertyNameNormalizer) {
        return new DefaultPropertyNameDiscoverer(environment, propertyNameNormalizer);
    }

    @Bean
    @ConditionalOnMissingBean
    public PropertyManagementEndpoint propertyManagementEndpoint(
            PropertyMutator propertyMutator, PropertyNameDiscoverer propertyNameDiscoverer) {
        return new PropertyManagementEndpoint(propertyMutator, propertyNameDiscoverer);
    }
}
