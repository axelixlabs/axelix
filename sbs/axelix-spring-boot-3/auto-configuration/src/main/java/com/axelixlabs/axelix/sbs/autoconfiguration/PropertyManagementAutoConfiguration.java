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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

import com.axelixlabs.axelix.sbs.spring.context.ContextRestarter;
import com.axelixlabs.axelix.sbs.spring.env.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.env.PropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.properties.ContextReloadingPropertyMutator;
import com.axelixlabs.axelix.sbs.spring.properties.DefaultPropertyNameDiscoverer;
import com.axelixlabs.axelix.sbs.spring.properties.DefaultPropertySourceDescriber;
import com.axelixlabs.axelix.sbs.spring.properties.PropertyManagementEndpoint;
import com.axelixlabs.axelix.sbs.spring.properties.PropertyMutator;
import com.axelixlabs.axelix.sbs.spring.properties.PropertyNameDiscoverer;
import com.axelixlabs.axelix.sbs.spring.properties.PropertySourceDescriber;

/**
 * Auto-configuration for property management operations via Spring Boot Actuator.
 *
 * <p>This configuration provides beans to discover, describe, and mutate application properties at runtime,
 * as well as an actuator endpoint to expose these capabilities.</p>
 *
 * <p>Beans registered by this auto-configuration (if missing) include:</p>
 * <ul>
 *   <li>{@link PropertyMutator} — responsible for updating property values and triggering application context restarts.</li>
 *   <li>{@link PropertyNameDiscoverer} — responsible for locating properties name within the Spring Environment.</li>
 *   <li>{@link PropertyManagementEndpoint} — actuator endpoint exposing property management operations.</li>
 *   <li>{@link PropertySourceDescriber} - Provides metadata descriptions of property sources.</li>
 * </ul>
 *
 * <p>This auto-configuration is applied after {@link ContextRestarterAutoConfiguration}
 * to ensure context restart capabilities are available when mutating properties.</p>
 *
 * @since 10.07.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration(after = ContextRestarterAutoConfiguration.class)
public class PropertyManagementAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PropertyMutator propertyMutator(ConfigurableEnvironment environment, ContextRestarter contextRestarter) {
        return new ContextReloadingPropertyMutator(environment, contextRestarter);
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

    @Bean
    @ConditionalOnMissingBean
    public PropertySourceDescriber propertySourceDescriber() {
        return new DefaultPropertySourceDescriber();
    }
}
