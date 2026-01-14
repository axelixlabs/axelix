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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

import com.nucleonforge.axelix.sbs.spring.context.ContextRestarter;
import com.nucleonforge.axelix.sbs.spring.env.DefaultPropertyNameNormalizer;
import com.nucleonforge.axelix.sbs.spring.env.PropertyNameNormalizer;
import com.nucleonforge.axelix.sbs.spring.properties.ContextReloadingPropertyMutator;
import com.nucleonforge.axelix.sbs.spring.properties.DefaultPropertyNameDiscoverer;
import com.nucleonforge.axelix.sbs.spring.properties.DefaultPropertySourceDescriber;
import com.nucleonforge.axelix.sbs.spring.properties.PropertyManagementEndpoint;
import com.nucleonforge.axelix.sbs.spring.properties.PropertyMutator;
import com.nucleonforge.axelix.sbs.spring.properties.PropertyNameDiscoverer;
import com.nucleonforge.axelix.sbs.spring.properties.PropertySourceDescriber;

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
